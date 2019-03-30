package com.data2viz.kotlinx.htmlplugin.conversion.model

import com.data2viz.kotlinx.htmlplugin.conversion.data.HtmlAttribute
import com.data2viz.kotlinx.htmlplugin.conversion.data.HtmlTag
import com.intellij.psi.PsiElement
import com.intellij.lang.xml.XMLLanguage
import com.intellij.lang.html.HTMLLanguage
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiFileFactory
import com.intellij.psi.PsiWhiteSpace
import com.intellij.psi.impl.source.html.HtmlFileImpl
import com.intellij.psi.xml.XmlAttribute
import com.intellij.psi.xml.XmlDoctype
import com.intellij.psi.xml.XmlText
import com.intellij.psi.xml.XmlTag


object HtmlPsiToHtmlDataConverter {

    private val LOGGER = Logger.getInstance(HtmlPsiToHtmlDataConverter::class.java)


    fun convertElement(elementToConvert: PsiElement?): String? {
        return elementToConvert?.let {
            val convertXmlTagChild = convertXmlTagChild(elementToConvert, null)
            return convertXmlTagChild?.toKotlinX()

        }
    }


    private fun convertXmlTagChild(source: PsiElement, parentTarget: HtmlTag?): HtmlTag? {
        val newTag: HtmlTag?

        if (source.language != HTMLLanguage.INSTANCE && source.language != XMLLanguage.INSTANCE) {
            parentTarget!!.body = source.text.trim();

            newTag = null
        } else {

            when (source) {
                is XmlTag -> {
                    newTag = convertXmlTag(source)
                }

                is XmlAttribute -> {
                    parentTarget!!.attributes.add(convertAttribute(source))

                    newTag = null
                }

                is XmlText -> {
                    parentTarget!!.body = source.text.trim();

                    newTag = null
                }
                else -> {
                    newTag = null
                }
            }
        }

        return newTag
    }

    private fun convertAttribute(source: XmlAttribute): HtmlAttribute {
        return HtmlAttribute(source.name, source.value);
    }

    private fun convertXmlTag(source: XmlTag): HtmlTag {

        val htmlTag = HtmlTag(source.name)

        for (attribute in source.attributes) {
            convertXmlTagChild(attribute, htmlTag);
        }

        for (child in source.children) {
            convertXmlTagChild(child, htmlTag);
        }

        htmlTag.body = source.text

        return htmlTag

    }


    fun isStartsWithXmlElement(psiElement: PsiElement): Boolean {

        LOGGER.warn("isStartsWithXmlElement type $psiElement")

        val isStartsWithXmlElement: Boolean
        when (psiElement) {

            is XmlTag, is XmlDoctype, is HtmlFileImpl -> isStartsWithXmlElement = true

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
            isHTML = looksLikeHtml(psiFile)
        } else {
            isHTML = false
        }

        return isHTML
    }

}