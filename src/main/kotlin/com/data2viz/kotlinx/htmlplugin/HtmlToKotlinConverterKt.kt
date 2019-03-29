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

import com.intellij.lang.Language
import com.intellij.lang.html.HTMLLanguage
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiFileFactory
import com.intellij.psi.PsiWhiteSpace
import com.intellij.psi.xml.XmlDoctype
import com.intellij.psi.xml.XmlTag

import java.util.ArrayList


object HtmlToKotlinConverterKt {
    /*
     * WARNING - void declaration
     */
    fun startsWithXmlElement(psiElement: PsiElement): Boolean {

        if (psiElement is XmlTag) {
            return true
        }
        if (psiElement is XmlDoctype) {
            return true
        }
        val children = psiElement.children

        val destination = ArrayList<PsiElement>()
        for (i in children.indices) {
            val element = children[i]

            if (element is PsiWhiteSpace) {
                continue
            }

            if (element.textLength < 0) {
                continue
            }

            destination.add(element)

        }
        return if (destination.isEmpty()) {
            false
        } else HtmlToKotlinConverterKt.startsWithXmlElement(destination[0])
    }

    fun looksLikeHtml(psiFile: PsiFile): Boolean {

        return HtmlToKotlinConverterKt.startsWithXmlElement(psiFile)
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
