package me.tatarka.holdr.intellij.plugin.psi;

import com.intellij.lang.Language;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.*;
import com.intellij.psi.impl.light.LightIdentifier;
import com.intellij.psi.impl.light.LightModifierList;
import com.intellij.psi.impl.light.LightParameter;
import org.jetbrains.annotations.NotNull;

/**
 * @author Plushnikov Michail
 */
public class HoldrLightParameter extends LightParameter {
    private String myName;
    private final LightIdentifier myNameIdentifier;
    private volatile LightModifierList myModifierList;

    public HoldrLightParameter(@NotNull String name, @NotNull PsiType type, PsiElement declarationScope, Language language) {
        super(name, type, declarationScope, language);
        myName = name;
        PsiManager manager = declarationScope.getManager();
        myNameIdentifier = new LightIdentifier(manager, name);
        myModifierList = new HoldrLightModifierList(manager);
    }

    @NotNull
    @Override
    public String getName() {
        return myName;
    }

    @Override
    public PsiElement setName(@NotNull String name) {
        myName = name;
        return this;
    }

    @Override
    public PsiIdentifier getNameIdentifier() {
        return myNameIdentifier;
    }

    @Override
    public TextRange getTextRange() {
        TextRange r = super.getTextRange();
        return r == null ? TextRange.EMPTY_RANGE : r;
    }

    @Override
    public HoldrLightParameter setModifiers(String... modifiers) {
        myModifierList = new HoldrLightModifierList(getManager(), getLanguage(), modifiers);
        return this;
    }

    @NotNull
    @Override
    public PsiModifierList getModifierList() {
        return myModifierList;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        HoldrLightParameter that = (HoldrLightParameter) o;

        return getType().equals(that.getType());
    }

    @Override
    public int hashCode() {
        return getType().hashCode();
    }
}
