package com.data2viz.kotlinx.htmlplugin.conversion.model

import com.data2viz.kotlinx.htmlplugin.conversion.data.HtmlAttribute
import com.data2viz.kotlinx.htmlplugin.conversion.data.HtmlTag
import org.junit.Assert
import org.junit.Test

import org.junit.Assert.*

class HtmlDataToHtmlKotlinXKtTest {

    @Test
    fun HtmlTagtoKotlinXBase() {
        Assert.assertEquals("div {\n}\n",HtmlTag("div").toKotlinX())
    }

    @Test
    fun HtmlTagtoKotlinXBody() {
        val htmlTag = HtmlTag("div")
        htmlTag.body = "text1"
        Assert.assertEquals("div {\n+ \"text1\"\n}\n", htmlTag.toKotlinX())
    }


    @Test
    fun HtmlTagtoKotlinXNested() {
        val htmlTag = HtmlTag("div")
        htmlTag.children.add(HtmlTag("p"))
        htmlTag.children.add(HtmlTag("span"))
        Assert.assertEquals("div {\np {\n}\n\nspan {\n}\n\n}\n", htmlTag.toKotlinX())
    }


    @Test
    fun HtmlTagtoKotlinXBaseAttributes() {
        val htmlTag = HtmlTag("div")
        htmlTag.attributes.add(HtmlAttribute("attr1"))
        htmlTag.attributes.add(HtmlAttribute("attr2", "value2"))

        Assert.assertEquals("div {\nattr1\nattr2 = \"value2\"\n}\n", htmlTag.toKotlinX())
    }

    @Test
    fun HtmlAttributetoKotlinXWithoutValue() {
        Assert.assertEquals("attr_name",HtmlAttribute("attr_name").toKotlinX())
    }

    @Test
    fun HtmlAttributetoKotlinXWithValue() {
        Assert.assertEquals("attr_name = \"attr_value\"",HtmlAttribute("attr_name", "attr_value").toKotlinX())
    }
}