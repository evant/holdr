package me.tatarka.holdr.intellij.plugin.psi;

import com.intellij.lang.Language;
import com.intellij.psi.PsiManager;
import com.intellij.psi.impl.light.LightModifierList;

/**
 * Required because by default getText() returns null which causes intellij to puke when overriding a listener method.
 */
public class HoldrLightModifierList extends LightModifierList {
    public HoldrLightModifierList(PsiManager manager) {
        super(manager);
    }

    public HoldrLightModifierList(PsiManager manager, Language language, String... modifiers) {
        super(manager, language, modifiers);
    }

    @Override
    public String getText() {
        return "";
    }
}
