package me.tatarka.holdr.intellij.plugin;

import com.intellij.codeInsight.navigation.actions.GotoDeclarationHandler;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.editor.Editor;
import com.intellij.psi.*;
import com.intellij.psi.util.PsiTreeUtil;
import me.tatarka.holdr.compile.util.FileUtils;
import org.jetbrains.android.facet.AndroidFacet;
import org.jetbrains.android.resourceManagers.ResourceManager;
import org.jetbrains.android.util.AndroidResourceUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * User: evantatarka
 * Date: 10/3/14
 * Time: 1:50 PM
 */
public class HoldrGotoDeclarationHandler implements GotoDeclarationHandler {
    @Nullable
    @Override
    public PsiElement[] getGotoDeclarationTargets(PsiElement sourceItem, int offset, Editor editor) {
        if (!(sourceItem instanceof PsiIdentifier)) {
            return null;
        }

        PsiIdentifier identifier = (PsiIdentifier) sourceItem;

        final PsiFile file = sourceItem.getContainingFile();
        if (file == null) {
            return null;
        }

        AndroidFacet facet = AndroidFacet.getInstance(file);
        if (facet == null) {
            return null;
        }

        HoldrModel holdrModel = HoldrModel.get(facet.getModule());
        if (holdrModel == null) {
            return null;
        }

        final PsiReferenceExpression itemRefExp = PsiTreeUtil.getParentOfType(sourceItem, PsiReferenceExpression.class);
        if (itemRefExp == null) {
            return null;
        }

        PsiClass referencedClass = getReferencedClass(itemRefExp);
        if (referencedClass == null || !holdrModel.isHoldrClass(referencedClass)) {
            return null;
        }

        PsiReferenceExpression idField = findIdField(referencedClass, identifier);
        if (idField == null) {
            return null;
        }

        AndroidResourceUtil.MyReferredResourceFieldInfo info = AndroidResourceUtil.getReferredResourceOrManifestField(facet, idField, false);
        if (info == null) {
            return null;
        }

        final String nestedClassName = info.getClassName();
        final String fieldName = info.getFieldName();
        final List<PsiElement> resourceList = new ArrayList<PsiElement>();

        final ResourceManager manager = info.isSystem()
                ? facet.getSystemResourceManager(false)
                : facet.getLocalResourceManager();
        if (manager == null) {
            return null;
        }
        manager.collectLazyResourceElements(nestedClassName, fieldName, false, idField, resourceList);

        if (!resourceList.isEmpty()) {
            filterByHoldrName(holdrModel, resourceList, referencedClass);
            // Sort to ensure the output is stable, and to prefer the base folders
            Collections.sort(resourceList, AndroidResourceUtil.RESOURCE_ELEMENT_COMPARATOR);
        }

        return resourceList.toArray(new PsiElement[resourceList.size()]);
    }

    @Nullable
    @Override
    public String getActionText(DataContext context) {
        return null;
    }

    @Nullable
    private static PsiClass getReferencedClass(@NotNull PsiReferenceExpression exp) {
        PsiExpression qExp = exp.getQualifierExpression();
        if (!(qExp instanceof PsiReferenceExpression)) {
            return null;
        }
        final PsiReferenceExpression reference = (PsiReferenceExpression) qExp;
        final PsiElement resolvedElement = reference.resolve();

        if (!(resolvedElement instanceof PsiField)) {
            return null;
        }

        PsiField field = (PsiField) resolvedElement;
        PsiTypeElement holdrClassType = field.getTypeElement();
        if (holdrClassType == null) {
            return null;
        }

        PsiJavaCodeReferenceElement elementReference = holdrClassType.getInnermostComponentReferenceElement();
        if (elementReference == null) {
            return null;
        }

        PsiElement element = elementReference.resolve();

        if (!(element instanceof PsiClass)) {
            return null;
        }

        return (PsiClass) element;
    }

    @Nullable
    private static PsiReferenceExpression findIdField(@NotNull PsiClass holdrClass, @NotNull PsiIdentifier holdrIdentifier) {
        PsiMethod[] constructors = holdrClass.getConstructors();
        if (constructors.length == 0) {
            return null;
        }

        PsiCodeBlock body = constructors[0].getBody();
        if (body == null) {
            return null;
        }

        PsiStatement[] statements = body.getStatements();
        if (statements.length == 0) {
            return null;
        }

        for (PsiStatement statement : statements) {
            if (!(statement.getFirstChild() instanceof PsiAssignmentExpression)) {
                continue;
            }

            PsiAssignmentExpression assignment = (PsiAssignmentExpression) statement.getFirstChild();
            PsiExpression leftExpression = assignment.getLExpression();
            if (!leftExpression.getText().equals(holdrIdentifier.getText())) {
                continue;
            }

            PsiMethodCallExpression methodCall = PsiTreeUtil.findChildOfType(assignment, PsiMethodCallExpression.class);
            if (methodCall == null) {
                continue;
            }

            PsiExpression[] arguments = methodCall.getArgumentList().getExpressions();
            if (arguments.length == 0) {
                continue;
            }

            PsiExpression arg = arguments[0];

            if (arg instanceof PsiReferenceExpression) {
                return (PsiReferenceExpression) arg;
            }
        }

        return null;
    }

    private static void filterByHoldrName(HoldrModel holdrModel, Collection<PsiElement> resourceList, PsiClass holdrClass) {
        String holdrLayout = holdrModel.getLayoutName(holdrClass);
        Iterator<PsiElement> iter = resourceList.iterator();
        while (iter.hasNext()) {
            PsiElement resource = iter.next();
            String layoutName = FileUtils.stripExtension(resource.getContainingFile().getName());
            if (!holdrLayout.equals(layoutName)) {
                iter.remove();
            }
        }
    }
}
