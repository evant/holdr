package me.tatarka.holdr.intellij.plugin.psi;

import com.intellij.lang.ASTNode;
import com.intellij.lang.java.JavaLanguage;
import com.intellij.psi.*;
import com.intellij.psi.impl.CheckUtil;
import com.intellij.psi.impl.ElementPresentationUtil;
import com.intellij.psi.impl.light.LightIdentifier;
import com.intellij.psi.impl.light.LightMethodBuilder;
import com.intellij.psi.impl.light.LightModifierList;
import com.intellij.ui.RowIcon;
import com.intellij.util.IncorrectOperationException;
import com.intellij.util.PlatformIcons;
import com.intellij.util.StringBuilderSpinAllocator;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

/**
 * Created by evan on 2/15/15.
 */
public class HoldrLightMethodBuilder extends LightMethodBuilder {
    private ASTNode myASTNode;

    public HoldrLightMethodBuilder(@NotNull String name, @NotNull PsiClass context) {
        super(context.getManager(), JavaLanguage.INSTANCE, name);
        setMethodKind("HoldrLight");
        setAbstract(false);
        setContainingClass(context);
        setMethodReturnType(PsiType.VOID);
    }

    public HoldrLightMethodBuilder setAbstract(boolean value) {
        if (value) {
            setModifiers(PsiModifier.PUBLIC, PsiModifier.ABSTRACT);
        } else {
            setModifiers(PsiModifier.PUBLIC);
        }
        setIcon(value);
        return this;
    }

    @Override
    public LightMethodBuilder addParameter(@NotNull String name, @NotNull PsiType type) {
        return super.addParameter(new HoldrLightParameter(name, type, this, JavaLanguage.INSTANCE));
    }

    private void setIcon(boolean isAbstract) {
        Icon icon = isAbstract ? PlatformIcons.ABSTRACT_METHOD_ICON : PlatformIcons.METHOD_ICON;
        final RowIcon baseIcon = ElementPresentationUtil.createLayeredIcon(icon, this, false);
        setBaseIcon(baseIcon);
    }


    @Override
    public PsiElement replace(@NotNull PsiElement newElement) throws IncorrectOperationException {
        // just add new element to the containing class
        final PsiClass containingClass = getContainingClass();
        if (null != containingClass) {
            CheckUtil.checkWritable(containingClass);
            return containingClass.add(newElement);
        }
        return null;
    }

    @Override
    public ASTNode getNode() {
        if (null == myASTNode) {
            myASTNode = rebuildMethodFromString().getNode();
        }
        return myASTNode;
    }

    @Override
    public void delete() throws IncorrectOperationException {
        // simple do nothing
    }

    @Override
    public void checkDelete() throws IncorrectOperationException {
        // simple do nothing
    }

    @Override
    public PsiIdentifier getNameIdentifier() {
        return new LightIdentifier(myManager, getName());
    }

    private PsiMethod rebuildMethodFromString() {
        final StringBuilder builder = StringBuilderSpinAllocator.alloc();
        try {
            builder.append(getAllModifierProperties((LightModifierList) getModifierList()));
            PsiType returnType = getReturnType();
            if (null != returnType) {
                builder.append(returnType.getCanonicalText()).append(' ');
            }
            builder.append(getName());
            builder.append('(');
            if (getParameterList().getParametersCount() > 0) {
                for (PsiParameter parameter : getParameterList().getParameters()) {
                    builder.append(parameter.getType().getCanonicalText()).append(' ').append(parameter.getName()).append(',');
                }
                builder.deleteCharAt(builder.length() - 1);
            }
            builder.append(')');
            builder.append('{').append(" ").append('}');
            PsiElementFactory elementFactory = JavaPsiFacade.getInstance(getManager().getProject()).getElementFactory();
            return elementFactory.createMethodFromText(builder.toString(), getContainingClass());
        } finally {
            StringBuilderSpinAllocator.dispose(builder);
        }
    }

    public String getAllModifierProperties(LightModifierList modifierList) {
        final StringBuilder builder = StringBuilderSpinAllocator.alloc();
        try {
            for (String modifier : modifierList.getModifiers()) {
                if (!PsiModifier.PACKAGE_LOCAL.equals(modifier)) {
                    builder.append(modifier).append(' ');
                }
            }
            return builder.toString();
        } finally {
            StringBuilderSpinAllocator.dispose(builder);
        }
    }
}
