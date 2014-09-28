package me.tatarka.holdr.intellij.plugin;

import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.psi.search.GlobalSearchScope;
import me.tatarka.holdr.compile.model.Include;
import me.tatarka.holdr.compile.model.Ref;
import me.tatarka.holdr.compile.model.View;

/**
 * Created by evan on 9/27/14.
 */
public class HoldrField extends LightField {
    public HoldrField(PsiClass context, HoldrModel holdrModel, Ref ref) {
        super(ref.fieldName, context, getType(context, holdrModel, ref), /*static*/ false, /*final*/false, /*constantValue*/null);
    }

    private static PsiType getType(PsiClass context, HoldrModel holdrModel, Ref ref) {
        Project project = context.getProject();

        if (ref instanceof View) {
            View view = (View) ref;
            return getType(project, view.type);
        } else if (ref instanceof Include) {
            Include include = (Include) ref;
            return getType(project, holdrModel.getQualifiedClassName(include.layout));
        } else {
            throw new IllegalArgumentException("Unknown ref type: " + ref);
        }
    }

    private static PsiClassType getType(Project project, String className) {
        final PsiElementFactory factory = JavaPsiFacade.getInstance(project).getElementFactory();
        GlobalSearchScope scope = GlobalSearchScope.allScope(project);

        PsiClass psiClass = JavaPsiFacade.getInstance(project).findClass(className, scope);
        if (psiClass == null) {
            throw new IllegalArgumentException("Can't find class: " + className);
        }
        return factory.createType(psiClass, factory.createRawSubstitutor(psiClass));
    }
}
