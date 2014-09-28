package me.tatarka.holdr.intellij.plugin;

import com.intellij.openapi.project.DumbService;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiField;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiJavaFile;
import com.intellij.psi.augment.PsiAugmentProvider;
import com.intellij.psi.impl.source.PsiExtensibleClass;
import me.tatarka.holdr.compile.HoldrCompiler;
import me.tatarka.holdr.compile.HoldrGenerator;
import me.tatarka.holdr.compile.Layout;
import me.tatarka.holdr.compile.model.Ref;
import org.jetbrains.android.facet.AndroidFacet;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static me.tatarka.holdr.intellij.plugin.HoldrUtils.getPackageName;

/**
 * Created by evan on 9/23/14.
 */
public class HoldrAugmentProvider extends PsiAugmentProvider {
    @NotNull
    @Override
    public <Psi extends PsiElement> List<Psi> getAugments(@NotNull PsiElement element, @NotNull Class<Psi> type) {
        if (type != PsiField.class || !(element instanceof PsiExtensibleClass)) {
            return Collections.emptyList();
        }

        final PsiExtensibleClass aClass = (PsiExtensibleClass)element;
        final String className = aClass.getName();
        final boolean holdrClassAugment = className.startsWith(HoldrGenerator.CLASS_PREFIX);

        if (!holdrClassAugment) {
            return Collections.emptyList();
        }

        if (DumbService.isDumb(element.getProject())) {
            return Collections.emptyList();
        }

        final AndroidFacet facet = AndroidFacet.getInstance(element);
        if (facet == null) {
            return Collections.emptyList();
        }
        //TODO: ensure holdr is a project dependency.

        final PsiFile containingFile = element.getContainingFile();
        if (containingFile == null) {
            return Collections.emptyList();
        }

        if (!isHoldrClass(facet, containingFile)) {
            return Collections.emptyList();
        }

        HoldrModel holdrModel = HoldrModel.getInstance(facet);
        Layout layout = holdrModel.getLayoutForClass(className);

        if (layout == null) {
            return Collections.emptyList();
        }

        List<Psi> result = new ArrayList<Psi>();

        for (Ref ref : layout.refs) {
            result.add((Psi) new HoldrField(aClass, holdrModel, ref));
        }

        return result;
    }

    private static boolean isHoldrClass(@NotNull AndroidFacet facet, @NotNull PsiFile file) {
        if (!(file instanceof PsiJavaFile)) {
            return false;
        }
        PsiJavaFile javaFile = (PsiJavaFile) file;

        final String manifestPackage = getPackageName(facet);
        if (manifestPackage == null) {
            return false;
        }

        final String holdrPackage = manifestPackage + "." + HoldrCompiler.PACKAGE;
        return javaFile.getPackageName().equals(holdrPackage);
    }
}
