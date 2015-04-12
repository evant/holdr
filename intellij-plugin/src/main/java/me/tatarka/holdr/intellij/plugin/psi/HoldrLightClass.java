package me.tatarka.holdr.intellij.plugin.psi;

import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiModifierList;
import org.jetbrains.android.augment.AndroidLightClass;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Created by evan on 2/22/15.
 */
public class HoldrLightClass extends AndroidLightClass {
    private PsiMethod[] myMethods = PsiMethod.EMPTY_ARRAY;

    public HoldrLightClass(PsiClass context, String name) {
        super(context, name);
    }

    @Override
    public PsiModifierList getModifierList() {
        return new HoldrLightModifierList(getManager(), getLanguage(), "public", "static");
    }

    @Override
    public boolean isInterface() {
        return true;
    }

    public void setMethods(@NotNull List<PsiMethod> methods) {
        myMethods = new PsiMethod[methods.size()];
        for (int i = 0; i < methods.size(); i++) {
            myMethods[i] = methods.get(i);
        }
    }

    @NotNull
    @Override
    public PsiMethod[] getMethods() {
        return myMethods;
    }
}
