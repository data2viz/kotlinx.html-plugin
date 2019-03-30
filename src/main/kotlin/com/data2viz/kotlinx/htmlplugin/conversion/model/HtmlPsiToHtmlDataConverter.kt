package com.data2viz.kotlinx.htmlplugin.conversion.model

import com.data2viz.kotlinx.htmlplugin.conversion.data.HtmlAttribute
import com.data2viz.kotlinx.htmlplugin.conversion.data.HtmlTag

import com.intellij.lang.html.HTMLLanguage
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import com.intellij.psi.*
import com.intellij.psi.impl.source.html.HtmlFileImpl
import com.intellij.psi.xml.XmlAttribute
import com.intellij.psi.xml.XmlDoctype
import com.intellij.psi.xml.XmlText
import com.intellij.psi.xml.XmlTag


object HtmlPsiToHtmlDataConverter {

    private val LOGGER = Logger.getInstance(HtmlPsiToHtmlDataConverter::class.java)


    public fun convertAttribute(source: XmlAttribute): HtmlAttribute {
        return HtmlAttribute(source.name, source.value);
    }

    public fun convertPsiElement(psiElement: PsiElement, parentHtmlTag: HtmlTag? = null): HtmlTag? {

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
                        val childHtmlTag = convertPsiElement(childPsi, htmlTag);
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

        return parentHtmlTag

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

    fun looksLikeHtml(psiFile: PsiFile): Boolean {

        return isStartsWithXmlElement(psiFile)
    }

    fun looksLikeHtml(text: String, project: Project): Boolean {

        val psiFileFactory = PsiFileFactory.getInstance(project)
        val htmlPsiFile = psiFileFactory.createFileFromText("someFile", HTMLLanguage.INSTANCE, text)


        val isHtml: Boolean

        if (htmlPsiFile != null) {
            isHtml = looksLikeHtml(htmlPsiFile)
        } else {
            isHtml = false
        }

        return isHtml
    }


}

fun PsiElement.toHtmlTag(parentHtmlTag: HtmlTag? = null): HtmlTag? =
        HtmlPsiToHtmlDataConverter.convertPsiElement(this, parentHtmlTag)

fun PsiElement.toKotlinX(): String? = this.toHtmlTag()?.toKotlinX()