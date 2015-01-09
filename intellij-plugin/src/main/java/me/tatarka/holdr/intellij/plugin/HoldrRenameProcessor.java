package me.tatarka.holdr.intellij.plugin;

import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.xml.XmlAttribute;
import com.intellij.psi.xml.XmlAttributeValue;
import com.intellij.refactoring.listeners.RefactoringElementListener;
import com.intellij.refactoring.rename.RenamePsiElementProcessor;
import com.intellij.usageView.UsageInfo;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.android.facet.AndroidFacet;
import org.jetbrains.android.util.AndroidResourceUtil;
import org.jetbrains.android.util.AndroidUtils;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
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

        HoldrModel holdrModel = HoldrModel.getInstance(androidFacet.getModule());
        if (holdrModel == null) {
            return false;
        }

        PsiElement parent = element.getParent();
        if (!(parent instanceof PsiClass)) {
            return false;
        }

        if (isRLayoutField(element)) {
            return true;
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

        final HoldrModel holdrModel = HoldrModel.getInstance(androidFacet.getModule());
        if (holdrModel == null) {
            return;
        }

        if (isRLayoutField(element) || element instanceof PsiFile) {
            renameHoldrFile(holdrModel, element, newName, allRenames);
        } else if (element instanceof PsiField) {
            renameHoldrField(holdrModel, (PsiField) element, newName, allRenames);
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
                    if (layoutName.equals(FileUtil.getNameWithoutExtension(file.getName()))) {
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

    private void renameHoldrFile(HoldrModel holdrModel, PsiElement element, String newName, Map<PsiElement, String> allRenames) {
        String oldName;
        if (element instanceof PsiFile) {
            oldName = ((PsiFile) element).getVirtualFile().getNameWithoutExtension();
        } else {
            oldName = ((PsiField) element).getName();
        }

        String holdrClassName = holdrModel.getHoldrClassName(oldName);
        JavaPsiFacade javaPsiFacade =  JavaPsiFacade.getInstance(element.getProject());
        PsiClass holdrClass = javaPsiFacade.findClass(holdrClassName, GlobalSearchScope.moduleScope(holdrModel.getModule()));
        if (holdrClass == null) {
            return;
        }

        String newHoldrName = holdrModel.getHoldrShortClassName(FileUtil.getNameWithoutExtension(newName));
        allRenames.put(holdrClass, newHoldrName);
    }

    @NotNull
    @Override
    public Collection<PsiReference> findReferences(PsiElement element) {
        Collection<PsiReference> references = super.findReferences(element);
        return references;
    }

    @NotNull
    @Override
    public Collection<PsiReference> findReferences(PsiElement element, boolean searchInCommentsAndStrings) {
        return super.findReferences(element, searchInCommentsAndStrings);
    }

    @Override
    public void renameElement(PsiElement element, String newName, UsageInfo[] usages, RefactoringElementListener listener) throws IncorrectOperationException {
        HoldrModel holdrModel = HoldrModel.getInstance(element);
        if (holdrModel == null) {
            return;
        }
        super.renameElement(element, newName, filterHoldrUsageInfo(holdrModel, usages), listener);
    }

    private static UsageInfo[] filterHoldrUsageInfo(HoldrModel holdrModel, UsageInfo[] usages) {
        if (usages == null) return null;

        List<UsageInfo> result = new ArrayList<UsageInfo>();
        for (UsageInfo usage : usages) {
            PsiClass parent = PsiTreeUtil.getParentOfType(usage.getElement(), PsiClass.class);
            if (parent != null && holdrModel.isHoldrClass(parent)) {
                continue; // Skip refactors in holdr classes.
            }
            result.add(usage);
        }

        return result.toArray(new UsageInfo[result.size()]);
    }

    private static boolean isRLayoutField(PsiElement element) {
        PsiElement parent = element.getParent();
        if (!(parent instanceof PsiClass)) {
            return false;
        }
        PsiClass parentClass = (PsiClass) parent;
        return parentClass.getName().equals("layout")
                && parentClass.getParent() instanceof PsiClass
                && ((PsiClass) parentClass.getParent()).getName().equals(AndroidUtils.R_CLASS_NAME);
    }
}
