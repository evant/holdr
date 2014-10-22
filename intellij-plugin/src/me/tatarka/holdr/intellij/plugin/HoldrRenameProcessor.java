package me.tatarka.holdr.intellij.plugin;

import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.xml.XmlAttribute;
import com.intellij.psi.xml.XmlAttributeValue;
import com.intellij.refactoring.listeners.RefactoringElementListener;
import com.intellij.refactoring.rename.RenamePsiElementProcessor;
import com.intellij.refactoring.rename.RenameUtil;
import com.intellij.usageView.UsageInfo;
import com.intellij.util.IncorrectOperationException;
import me.tatarka.holdr.compile.util.FileUtils;
import org.jetbrains.android.facet.AndroidFacet;
import org.jetbrains.android.util.AndroidResourceUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * User: evantatarka
 * Date: 10/3/14
 * Time: 5:10 PM
 */
public class HoldrRenameProcessor extends RenamePsiElementProcessor {
    @Override
    public boolean canProcessElement(PsiElement element) {
        if (element instanceof PsiFile) {
            PsiFile file = (PsiFile) element;
            return HoldrAndroidUtils.isUserLayoutFile(element.getProject(), file.getVirtualFile());
        }

        if (!(element instanceof PsiField)) {
            return false;
        }

        AndroidFacet androidFacet = AndroidFacet.getInstance(element);
        if (androidFacet == null) {
            return false;
        }

        HoldrModel holdrModel = HoldrModel.get(androidFacet.getModule());
        if (holdrModel == null) {
            return false;
        }

        PsiElement parent = element.getParent();
        if (!(parent instanceof PsiClass)) {
            return false;
        }

        return holdrModel.isHoldrClass((PsiClass) parent);
    }

    @Override
    public void prepareRenaming(PsiElement element, final String newName, final Map<PsiElement, String> allRenames) {
        allRenames.put(element, newName);

        AndroidFacet androidFacet = AndroidFacet.getInstance(element);
        if (androidFacet == null) {
            return;
        }

        final HoldrModel holdrModel = HoldrModel.get(androidFacet.getModule());
        if (holdrModel == null) {
            return;
        }

        if (element instanceof PsiField) {
            renameHoldrField(holdrModel, (PsiField) element, newName, allRenames);
        } else if (element instanceof PsiFile) {
            renameHoldrFile(holdrModel, (PsiFile) element, newName, allRenames);
        }
    }

    private void renameHoldrField(final HoldrModel holdrModel, PsiField holdrField, final String newName, final Map<PsiElement, String> allRenames) {
        final PsiClass holdrClass = (PsiClass) holdrField.getParent();

        String layoutName = holdrModel.getLayoutName(holdrClass);
        PsiManager manager = PsiManager.getInstance(holdrField.getProject());
        List<PsiFile> layoutFiles = new ArrayList<PsiFile>();

        for (VirtualFile resDirs : holdrModel.getAndroidFacet().getAllResourceDirectories()) {
            for (VirtualFile resDir : resDirs.getChildren()) {
                if (!HoldrAndroidUtils.isUserLayoutDir(holdrField.getProject(), resDir)) {
                    continue;
                }

                PsiDirectory dir = manager.findDirectory(resDir);
                for (PsiFile file : dir.getFiles()) {
                    if (layoutName.equals(FileUtils.stripExtension(file.getName()))) {
                        layoutFiles.add(file);
                    }
                }
            }
        }

        if (!layoutFiles.isEmpty()) {
            final PsiReferenceExpression fieldId = HoldrPsiUtils.findIdForField(holdrClass, holdrField.getName());
            if (fieldId == null) {
                return;
            }

            for (PsiFile file : layoutFiles) {
                file.accept(new XmlRecursiveElementVisitor() {
                    @Override
                    public void visitXmlAttribute(XmlAttribute attribute) {
                        super.visitXmlAttribute(attribute);

                        XmlAttributeValue attributeValue = attribute.getValueElement();
                        if (attributeValue == null) {
                            return;
                        }

                        if (!AndroidResourceUtil.isIdDeclaration(attributeValue)) {
                            return;
                        }

                        if (HoldrAndroidUtils.areIdsEquivalent(fieldId.getText(), attributeValue.getValue())) {
                            XmlAttribute holdrFieldName = attribute.getParent().getAttribute("holdr_field_name", "http://schemas.android.com/apk/res-auto");

                            String fieldName = holdrModel.getFieldIdName(newName);

                            if (holdrFieldName != null) {
                                allRenames.put(holdrFieldName.getValueElement(), newName);
                            } else {
                                String idName = fieldName;

                                if (attributeValue.getValue().startsWith("@android:id/")) {
                                    idName = "@android:id/" + idName;
                                } else {
                                    idName = "@+id/" + idName;
                                }

                                allRenames.put(attributeValue, idName);

//                                PsiField[] rFields = AndroidResourceUtil.findIdFields(attributeValue);
//                                for (PsiField field : rFields) {
//                                    allRenames.put(field, fieldName);
//                                }
                            }
                        }
                    }
                });
            }
        }
    }

    private void renameHoldrFile(HoldrModel holderModel, PsiFile layoutFile, String newName, Map<PsiElement, String> allRenames) {
        String holdrClassName = holderModel.getHoldrClassName(layoutFile.getVirtualFile().getNameWithoutExtension());
        JavaPsiFacade javaPsiFacade =  JavaPsiFacade.getInstance(layoutFile.getProject());
        PsiClass holdrClass = javaPsiFacade.findClass(holdrClassName, GlobalSearchScope.moduleScope(holderModel.getModule()));
        if (holdrClass == null) {
            return;
        }

        String newHoldrName = holderModel.getHoldrShortClassName(FileUtils.stripExtension(newName));
        allRenames.put(holdrClass, newHoldrName);
    }

    @Override
    public void renameElement(PsiElement element, String newName, UsageInfo[] usages, RefactoringElementListener listener) throws IncorrectOperationException {
        if (element instanceof PsiField) {
            super.renameElement(element, newName, usages, listener);
        } else {
            RenameUtil.doRename(element, newName, usages, element.getProject(), listener);
        }
    }
}
