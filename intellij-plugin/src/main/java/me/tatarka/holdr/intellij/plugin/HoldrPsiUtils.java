package me.tatarka.holdr.intellij.plugin;

import com.intellij.psi.*;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * User: evantatarka
 * Date: 10/3/14
 * Time: 5:15 PM
 */
public class HoldrPsiUtils {
    @Nullable
    public static PsiClass getClassForField(@NotNull PsiField field) {
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
    public static PsiReferenceExpression findIdForField(@NotNull PsiClass holdrClass, @NotNull String fieldName) {
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
            if (!leftExpression.getText().equals(fieldName)) {
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

    @Nullable
    public static PsiReferenceExpression findIdForLayout(@NotNull PsiClass holdrClass) {
        for (PsiField field : holdrClass.getFields()) {
            if (!field.getName().equals("LAYOUT")) {
                continue;
            }

            PsiExpression initializer = field.getInitializer();
            if (initializer instanceof PsiReferenceExpression) {
                return (PsiReferenceExpression) initializer;
            }
        }

        return null;
    }
}
