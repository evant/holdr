package me.tatarka.holdr2.plugin

import com.intellij.psi.PsiElement
import com.intellij.psi.augment.PsiAugmentProvider

class LayoutPsiAugmentProvider : PsiAugmentProvider() {

    override fun <Psi : PsiElement?> getAugments(element: PsiElement, type: Class<Psi>): MutableList<Psi> {
        return super.getAugments(element, type)
    }
}

