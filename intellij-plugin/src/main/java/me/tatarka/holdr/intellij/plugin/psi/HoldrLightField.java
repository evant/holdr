package me.tatarka.holdr.intellij.plugin.psi;

import com.intellij.lang.java.JavaLanguage;
import com.intellij.navigation.NavigationItem;
import com.intellij.psi.*;
import com.intellij.psi.impl.*;
import com.intellij.psi.impl.light.LightElement;
import com.intellij.psi.impl.light.LightIdentifier;
import com.intellij.psi.impl.light.LightModifierList;
import com.intellij.psi.javadoc.PsiDocComment;
import com.intellij.ui.RowIcon;
import com.intellij.util.ArrayUtil;
import com.intellij.util.IncorrectOperationException;
import com.intellij.util.PlatformIcons;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * @author Eugene.Kudelevsky
 */
public class HoldrLightField extends LightElement implements PsiField, PsiVariableEx, NavigationItem {
    private final PsiClass myContext;
    private final PsiType myType;
    private final Object myConstantValue;

    private volatile PsiExpression myInitializer;
    private volatile String myName;
    private volatile LightModifierList myModifierList;

    public HoldrLightField(@NotNull String name,
                           @NotNull final PsiClass context,
                           @NotNull PsiType type,
                           final boolean isNullable,
                           @Nullable final Object constantValue) {
        super(context.getManager(), JavaLanguage.INSTANCE);
        myName = name;
        myType = type;
        myContext = context;
        myConstantValue = constantValue;

        final List<String> modifiers = new ArrayList<String>();
        modifiers.add(PsiModifier.PUBLIC);

        myModifierList = new LightModifierList(getManager(), getLanguage(), ArrayUtil.toStringArray(modifiers)) {
            @NotNull
            @Override
            public PsiAnnotation[] getAnnotations() {
                if (isNullable) {
                    PsiAnnotation[] annotations = new PsiAnnotation[1];
                    annotations[0] = new PsiElementFactoryImpl((PsiManagerEx) PsiManagerEx.getInstance(context.getProject())).createAnnotationFromText("@android.support.annotation.Nullable", HoldrLightField.this);
                    return annotations;
                } else {
                    return super.getAnnotations();
                }
            }
        };
    }

    @Override
    public boolean isEquivalentTo(PsiElement another) {
        return PsiClassImplUtil.isFieldEquivalentTo(this, another);
    }

    @Override
    public PsiElement getParent() {
        return myContext;
    }

    @Nullable
    @Override
    public PsiFile getContainingFile() {
        return myContext.getContainingFile();
    }

    @Override
    public PsiElement setName(@NonNls @NotNull String name) throws IncorrectOperationException {
        myName = name;
        return this;
    }

    @Override
    public Object computeConstantValue(Set<PsiVariable> visitedVars) {
        return computeConstantValue();
    }

    @Override
    public Object computeConstantValue() {
        return myConstantValue;
    }

    @Override
    public void setInitializer(@Nullable PsiExpression initializer) throws IncorrectOperationException {
        myInitializer = initializer;
    }

    @Override
    public PsiExpression getInitializer() {
        return myInitializer;
    }

    @Override
    public PsiDocComment getDocComment() {
        return null;
    }

    @Override
    public boolean isDeprecated() {
        return false;
    }

    @Override
    public PsiClass getContainingClass() {
        return myContext;
    }

    @Override
    public String toString() {
        return "HoldrLightField:" + getName();
    }

    @NotNull
    @Override
    public PsiType getType() {
        return myType;
    }

    @Override
    @NotNull
    public PsiModifierList getModifierList() {
        return myModifierList;
    }

    @Override
    public boolean hasModifierProperty(@NonNls @NotNull String name) {
        return myModifierList.hasModifierProperty(name);
    }

    @NotNull
    @Override
    public String getName() {
        return myName;
    }

    @Override
    public PsiTypeElement getTypeElement() {
        return null;
    }

    @Override
    public boolean hasInitializer() {
        return false;
    }

    @Override
    public void normalizeDeclaration() throws IncorrectOperationException {
    }

    @NotNull
    @Override
    public PsiIdentifier getNameIdentifier() {
        return new LightIdentifier(getManager(), myName);
    }

    @Override
    protected boolean isVisibilitySupported() {
        return true;
    }

    @Override
    public Icon getElementIcon(final int flags) {
        final RowIcon baseIcon = ElementPresentationUtil.createLayeredIcon(PlatformIcons.FIELD_ICON, this, false);
        return ElementPresentationUtil.addVisibilityIcon(this, flags, baseIcon);
    }
}
