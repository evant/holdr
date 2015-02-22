package me.tatarka.holdr.intellij.plugin;

import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.psi.augment.PsiAugmentProvider;
import com.intellij.psi.impl.source.PsiExtensibleClass;
import me.tatarka.holdr.intellij.plugin.psi.HoldrLightField;
import me.tatarka.holdr.intellij.plugin.psi.HoldrLightMethodBuilder;
import me.tatarka.holdr.model.Layout;
import me.tatarka.holdr.model.Listener;
import me.tatarka.holdr.model.Ref;
import me.tatarka.holdr.model.View;
import me.tatarka.holdr.util.GeneratorUtils;
import me.tatarka.holdr.util.GeneratorUtils.ListenerType;
import me.tatarka.holdr.util.Pair;
import org.jetbrains.annotations.NotNull;

import java.util.*;

/**
 * Created by evan on 2/13/15.
 */
public class HoldrPsiAugmentProvider extends PsiAugmentProvider {
    @SuppressWarnings("unchecked")
    @NotNull
    @Override
    public <Psi extends PsiElement> List<Psi> getAugments(@NotNull PsiElement element, @NotNull Class<Psi> type) {
        if ((type != PsiClass.class && (type != PsiField.class && type != PsiMethod.class)) ||
                !(element instanceof PsiExtensibleClass)) {
            return Collections.emptyList();
        }

        if (type != PsiField.class && type != PsiMethod.class) {
            return Collections.emptyList();
        }

        PsiExtensibleClass aClass = (PsiExtensibleClass) element;

        HoldrModel holdrModel = HoldrModel.getInstance(aClass);
        if (holdrModel == null) {
            return Collections.emptyList();
        }

        if (!holdrModel.isHoldrClass(aClass)) {
            return Collections.emptyList();
        }

        boolean isListener = aClass.getName().equals("Listener");

        PsiExtensibleClass holdrClass;
        if (isListener) {
            holdrClass = (PsiExtensibleClass) aClass.getContainingClass();
        } else {
            holdrClass = aClass;
        }

        if (holdrClass == null) {
            return Collections.emptyList();
        }

        String layoutName = holdrModel.getLayoutName(holdrClass);
        Layout layout = HoldrLayoutManager.getInstance(holdrClass.getProject()).getLayout(layoutName);
        if (layout == null) {
            return Collections.emptyList();
        }

        final List<Psi> result = new ArrayList<Psi>();

        if (isListener) {
            if (type == PsiMethod.class) {
                final Set<String> existingListenerMethods = getOwnListenerMethods(aClass);
                final List<PsiMethod> newListenerMethods = buildListenerMethods(aClass, layout);
                for (PsiMethod method : newListenerMethods) {
                    if (!existingListenerMethods.contains(method.getName())) {
                        result.add((Psi) method);
                    }
                }
            }
        } else {
            if (type == PsiField.class) {
                final Set<String> existingFields = getOwnFields(aClass);
                final List<PsiField> newFields = buildFields(aClass, layout);
                for (PsiField field : newFields) {
                    if (!existingFields.contains(field.getName())) {
                        result.add((Psi) field);
                    }
                }
            }
        }

        return result;
    }

    @NotNull
    private static Set<String> getOwnFields(@NotNull PsiExtensibleClass aClass) {
        final Set<String> result = new HashSet<String>();

        for (PsiField field : aClass.getOwnFields()) {
            result.add(field.getName());
        }
        return result;
    }

    @NotNull
    private static Set<String> getOwnListenerMethods(@NotNull PsiExtensibleClass listenerClass) {
        final Set<String> result = new HashSet<String>();
        for (PsiMethod method : listenerClass.getOwnMethods()) {
            result.add(method.getName());
        }
        return result;
    }

    @NotNull
    private static List<PsiField> buildFields(@NotNull PsiClass context, @NotNull Layout layout) {
        final List<PsiField> result = new ArrayList<PsiField>();

        for (Ref ref : layout.getRefs()) {
            if (ref instanceof View) {
                View view = (View) ref;
                PsiType type = HoldrPsiUtils.findType(view.type, context.getProject());
                if (type == null) {
                    continue;
                }
                HoldrLightField field = new HoldrLightField(view.fieldName, context, type, view.isNullable, null);
                result.add(field);
            }
        }
        return result;
    }

    private static List<PsiMethod> buildListenerMethods(@NotNull PsiClass context, @NotNull Layout layout) {
        List<PsiMethod> result = new ArrayList<PsiMethod>();

        for (Listener listener : layout.getListeners()) {
            HoldrLightMethodBuilder method = new HoldrLightMethodBuilder(listener.name, context)
                    .setAbstract(true);

            ListenerType listenerType = ListenerType.fromType(listener.type);

            for (Pair<GeneratorUtils.Type, String> param : listenerType.getParams()) {
                if (param.second.equals("view")) {
                    method.addParameter(param.second, HoldrPsiUtils.findType(listener.viewType, context.getProject()));
                } else {
                    method.addParameter(param.second, toPsiType(context.getProject(), listener.viewType, param.first));
                }
            }
            GeneratorUtils.Type returnType = listenerType.getReturnType();
            if (returnType != GeneratorUtils.Type.VOID) {
                method.setMethodReturnType(toPsiType(context.getProject(), listener.viewType, returnType));
            }
            result.add(method);
        }
        return result;
    }

    private static PsiType toPsiType(Project project, String viewType, GeneratorUtils.Type type) {
        switch (type) {
            case VIEW_CLASS: return HoldrPsiUtils.findType(viewType, project);
            case BOOLEAN: return PsiType.BOOLEAN;
            case INT: return PsiType.INT;
            case LONG: return PsiType.LONG;
            default: return HoldrPsiUtils.findType(type.getClassName(), project);
        }
    }
}
