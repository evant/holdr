package me.tatarka.holdr.intellij.plugin;

import com.intellij.codeInsight.navigation.actions.GotoDeclarationHandler;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.editor.Editor;
import com.intellij.psi.*;
import com.intellij.psi.util.PsiTreeUtil;
import me.tatarka.holdr.model.Layout;
import org.jetbrains.android.facet.AndroidFacet;
import org.jetbrains.android.util.AndroidResourceUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

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

        HoldrModel holdrModel = HoldrModel.getInstance(facet.getModule());
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

        String identifierText = identifier.getText();

        List<? extends PsiElement> elements;
        if (identifierText.equals("LAYOUT")) {
            elements = HoldrPsiUtils.findLayoutFiles(holdrModel, referencedClass);
        } else {
            String layoutName = holdrModel.getLayoutName(referencedClass);
            Layout layout = HoldrLayoutManager.getInstance(referencedClass.getProject()).getLayout(layoutName);
            if (layout == null) {
                return null;
            }
            elements = HoldrPsiUtils.findIdReferences(layout, referencedClass, identifierText);
        }

        if (elements == null) {
            return null;
        }

        if (!elements.isEmpty()) {
            // sort to ensure the output is stable, and to prefer the base folders
            Collections.sort(elements, AndroidResourceUtil.RESOURCE_ELEMENT_COMPARATOR);
        }

        return elements.toArray(new PsiElement[elements.size()]);
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

        if (resolvedElement instanceof PsiClass) {
            return (PsiClass) resolvedElement;
        }

        if (!(resolvedElement instanceof PsiField)) {
            return null;
        }

        PsiField field = (PsiField) resolvedElement;
        return HoldrPsiUtils.getClassForField(field);
    }
}
