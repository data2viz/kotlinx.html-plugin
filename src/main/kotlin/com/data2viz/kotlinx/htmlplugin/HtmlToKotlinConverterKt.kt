/*
 * Decompiled with CFR 0.141.
 *
 * Could not load the following classes:
 *  com.intellij.lang.Language
 *  com.intellij.lang.html.HTMLLanguage
 *  com.intellij.openapi.project.Project
 *  com.intellij.psi.PsiElement
 *  com.intellij.psi.PsiFile
 *  com.intellij.psi.PsiFileFactory
 *  com.intellij.psi.PsiWhiteSpace
 *  com.intellij.psi.xml.XmlDoctype
 *  com.intellij.psi.xml.XmlTag
 *  kotlin.CollectionsKt
 *  kotlin.jvm.internal.Intrinsics
 *  kotlin.jvm.internal.KotlinFileFacade
 *  org.jetbrains.annotations.NotNull
 */
package com.data2viz.kotlinx.htmlplugin

import com.intellij.lang.html.HTMLLanguage
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiFileFactory
import com.intellij.psi.PsiWhiteSpace
import com.intellij.psi.impl.source.html.HtmlDocumentImpl
import com.intellij.psi.impl.source.html.HtmlFileImpl
import com.intellij.psi.impl.source.html.HtmlLikeFile
import com.intellij.psi.xml.XmlDoctype
import com.intellij.psi.xml.XmlTag


object HtmlToKotlinConverterKt {
    private val LOGGER = Logger.getInstance(HtmlToKotlinConverterKt::class.java)


    fun isStartsWithXmlElement(psiElement: PsiElement): Boolean {

        LOGGER.warn("isStartsWithXmlElement type $psiElement")

        val isStartsWithXmlElement: Boolean
        when (psiElement) {

            is XmlTag, is XmlDoctype, is HtmlFileImpl-> isStartsWithXmlElement = true

            else -> {
                val destination = mutableListOf<PsiElement>()

                val children = psiElement.children
                for (child in children) {

                    if (child is PsiWhiteSpace) {
                        continue
                    }

                    if (child.textLength < 0) {
                        continue
                    }

                    destination.add(child)
                }

                if (destination.isEmpty()) {
                    isStartsWithXmlElement = false
                } else {
                    isStartsWithXmlElement = isStartsWithXmlElement(destination[0])
                }
            }
        }

        LOGGER.warn("isStartsWithXmlElement result=$isStartsWithXmlElement  class ${psiElement.javaClass.name} \n ${psiElement.text}")

        return isStartsWithXmlElement
    }

    fun looksLikeHtml(psiFile: PsiFile): Boolean {

        return isStartsWithXmlElement(psiFile)
    }

    fun looksLikeHtml(text: String, project: Project): Boolean {

        val psiFile = PsiFileFactory.getInstance(project).createFileFromText("someFile", HTMLLanguage.INSTANCE, text)
        val psiFileNotNull = psiFile != null

        val isHTML: Boolean

        if (psiFileNotNull) {
            isHTML = HtmlToKotlinConverterKt.looksLikeHtml(psiFile)
        } else {
            isHTML = false
        }

        return isHTML
    }
}
