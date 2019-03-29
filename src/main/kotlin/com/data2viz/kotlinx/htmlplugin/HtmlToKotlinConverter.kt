package com.data2viz.kotlinx.htmlplugin

import com.intellij.psi.PsiElement
import com.intellij.lang.xml.XMLLanguage
import com.intellij.lang.html.HTMLLanguage
import com.intellij.psi.xml.XmlAttribute
import com.intellij.psi.xml.XmlText
import com.intellij.psi.xml.XmlTag


class HtmlToKotlinConverter {
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

}