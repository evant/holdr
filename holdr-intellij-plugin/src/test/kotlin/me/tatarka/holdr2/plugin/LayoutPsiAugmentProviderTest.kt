package me.tatarka.holdr2.plugin

import com.intellij.openapi.fileTypes.StdFileTypes
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFileFactory
import com.intellij.psi.PsiPackage
import com.intellij.psi.augment.PsiAugmentProvider
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.testFramework.fixtures.LightPlatformCodeInsightFixtureTestCase

class LayoutPsiAugmentProviderTest : LightPlatformCodeInsightFixtureTestCase() {
    lateinit var layoutPsiAugmentProvider: LayoutPsiAugmentProvider

    override fun setUp() {
        super.setUp()
        layoutPsiAugmentProvider = LayoutPsiAugmentProvider()
    }

    fun `test layout class is generated`() {
        val file = PsiFileFactory.getInstance(project)
                .createFileFromText(
                        "layout.java",
                        StdFileTypes.JAVA,
                        "package app.holdr.layout; public class layout {}")

        val element = PsiTreeUtil.findElementOfClassAtRange(file, 0, Int.MAX_VALUE, PsiPackage::class.java)!!
        val result = PsiAugmentProvider.collectAugments(element, PsiPackage::class.java)


    }
}

