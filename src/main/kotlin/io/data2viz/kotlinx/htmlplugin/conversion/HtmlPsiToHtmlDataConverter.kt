package io.data2viz.kotlinx.htmlplugin.conversion

import com.intellij.lang.html.HTMLLanguage
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiFileFactory
import com.intellij.psi.impl.source.html.HtmlDocumentImpl
import com.intellij.psi.impl.source.html.HtmlFileImpl
import com.intellij.psi.xml.*
import io.data2viz.kotlinx.htmlplugin.ide.debug
import io.data2viz.kotlinx.htmlplugin.ide.logger


object HtmlPsiToHtmlDataConverter {

    fun convertPsiFileToHtmlTag(psiFile: PsiFile): List<HtmlElement> {
        val result = mutableListOf<HtmlElement>()
        for (child in psiFile.children) {
            when (child) {
                is HtmlDocumentImpl -> child
                        .children
                        .forEach {
                            convertPsiElementToHtmlElement(it)?.let {
                                result.add(it)
                            }
                        }
                else -> {
                    val htmlTag = convertPsiElementToHtmlElement(child)
                    if (htmlTag != null) {
                        result.add(htmlTag)
                    }
                }
            }

        }

        return result
    }


    private fun convertPsiElementToHtmlElement(psiElement: PsiElement, parentHtmlTag: HtmlTag? = null): HtmlElement? =
            when (psiElement) {
                is XmlTag -> {

                    val htmlElement = HtmlTag(psiElement.name)

                    psiElement.children
                            .mapNotNull { convertPsiElementToHtmlElement(it, htmlElement) }
                            .forEach { htmlElement.children += it }

                    htmlElement

                }

                is XmlAttribute -> {
                    parentHtmlTag?.attributes?.add(psiElement.toHtmlAttribute())
                    null
                }

                is XmlText  -> psiElement.text.toHtmlText()
                else        -> null
            }

    private fun XmlAttribute.toHtmlAttribute(): HtmlAttribute = HtmlAttribute(name, value)

    private fun String.toHtmlText(): HtmlText? = if (trim().isEmpty()) null else HtmlText(trim())

    private fun isStartsWithXmlElement(psiElement: PsiElement): Boolean {

        logger.debug {"isStartsWithXmlElement type $psiElement"}

        var isStartsWithXmlElement: Boolean
        when (psiElement) {

            is HtmlDocumentImpl, is HtmlFileImpl -> {
                val children = psiElement.children
                if (children.isNotEmpty()) {

                    isStartsWithXmlElement = false
                    for (child in children) {
                        isStartsWithXmlElement = isStartsWithXmlElement(child)
                        if (isStartsWithXmlElement) {
                            break
                        } else {
                            if(child is XmlToken) {
                                // it looks like "client.request<String> {" which is not valid html code
                                isStartsWithXmlElement = false
                                break
                            }
                        }
                    }


                } else {
                    isStartsWithXmlElement = false
                }
            }

            is XmlTag, is XmlDoctype -> isStartsWithXmlElement = true

            else -> isStartsWithXmlElement = false
        }

        logger.debug { "isStartsWithXmlElement result=$isStartsWithXmlElement  class ${psiElement.javaClass.name} \n ${psiElement.text}" }

        return isStartsWithXmlElement
    }

    fun isLooksLikeHtml(psiFile: PsiFile): Boolean = isStartsWithXmlElement(psiFile)

    fun createHtmlFileFromText(project: Project, fileName: String, text: String): PsiFile =
            PsiFileFactory
                    .getInstance(project)
                    .createFileFromText(fileName, HTMLLanguage.INSTANCE, text)

    fun createHtmlFileFromText(project: Project, text: String): PsiFile =
            PsiFileFactory
                    .getInstance(project)
                    .createFileFromText(HTMLLanguage.INSTANCE, text)
}


fun HtmlFileImpl.converToHtmlElements(): List<HtmlElement> =
        HtmlPsiToHtmlDataConverter.convertPsiFileToHtmlTag(this)
