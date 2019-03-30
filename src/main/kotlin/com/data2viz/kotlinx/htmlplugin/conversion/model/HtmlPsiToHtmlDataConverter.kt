package com.data2viz.kotlinx.htmlplugin.conversion.model

import com.data2viz.kotlinx.htmlplugin.conversion.data.HtmlAttribute
import com.data2viz.kotlinx.htmlplugin.conversion.data.HtmlTag

import com.intellij.lang.html.HTMLLanguage
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import com.intellij.psi.*
import com.intellij.psi.impl.source.PsiFileImpl
import com.intellij.psi.impl.source.html.HtmlDocumentImpl
import com.intellij.psi.impl.source.html.HtmlFileImpl
import com.intellij.psi.xml.XmlAttribute
import com.intellij.psi.xml.XmlDoctype
import com.intellij.psi.xml.XmlText
import com.intellij.psi.xml.XmlTag


object HtmlPsiToHtmlDataConverter {

    private val LOGGER = Logger.getInstance(HtmlPsiToHtmlDataConverter::class.java)


     fun convertAttribute(source: XmlAttribute): HtmlAttribute {
        return HtmlAttribute(source.name, source.value);
    }

    fun convertPsiFileToHtmlTag(psiFile: PsiFile): List<HtmlTag> {
        val result = mutableListOf<HtmlTag>()
        for (child in psiFile.children) {
            when(child) {
                is HtmlDocumentImpl -> {
                    for (docChild in child.children) {
                        val htmlTag = convertPsiElementToHtmlTag(docChild);
                        if(htmlTag != null) {
                            result.add(htmlTag)
                        }
                    }
                }
                else -> {
                    val htmlTag = convertPsiElementToHtmlTag(child);
                    if(htmlTag != null) {
                        result.add(htmlTag)
                    }
                }
            }

        }

        return result
    }

     fun convertPsiElementToHtmlTag(psiElement: PsiElement, parentHtmlTag: HtmlTag? = null): HtmlTag? {

        var htmlTag: HtmlTag? = null;

        when (psiElement) {
            is XmlTag -> {

                htmlTag = HtmlTag(psiElement.name)

                for (attribute in psiElement.attributes) {
                    val htmlAttribute = convertAttribute(attribute);
                    htmlTag.attributes.add(htmlAttribute)

                }

                parentHtmlTag?.let {
                    for (childPsi in psiElement.children) {
                        val childHtmlTag = convertPsiElementToHtmlTag(childPsi, htmlTag);
                        if (childHtmlTag != null) {
                            it.children.add(childHtmlTag)
                        }
                    }
                }
            }

            is XmlAttribute -> {

                parentHtmlTag?.attributes?.add(convertAttribute(psiElement))
            }

            is XmlText -> {
                parentHtmlTag?.body = psiElement.text.trim();
            }

            else -> {
                // if (source.language != HTMLLanguage.INSTANCE && source.language != XMLLanguage.INSTANCE) {
                //any not handled text
                parentHtmlTag?.body = psiElement.text
            }
        }

        return htmlTag

    }


    fun isStartsWithXmlElement(psiElement: PsiElement): Boolean {

        LOGGER.warn("isStartsWithXmlElement type $psiElement")

        val isStartsWithXmlElement: Boolean
        when (psiElement) {

            is XmlTag, is XmlDoctype, is HtmlFileImpl
            ->
                isStartsWithXmlElement = true

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

    fun isLooksLikeHtml(psiFile: PsiFile): Boolean {

        return isStartsWithXmlElement(psiFile)
    }

    fun createHtmlFileFromText(project: Project, fileName: String, text: String): PsiFile {

        val psiFileFactory = PsiFileFactory.getInstance(project)
        return psiFileFactory.createFileFromText(fileName, HTMLLanguage.INSTANCE, text)
    }
}


fun HtmlFileImpl.toHtmlTags(): List<HtmlTag> =
        HtmlPsiToHtmlDataConverter.convertPsiFileToHtmlTag(this)
