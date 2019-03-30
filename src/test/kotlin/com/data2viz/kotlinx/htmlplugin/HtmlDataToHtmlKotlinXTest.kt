package com.data2viz.kotlinx.htmlplugin

import com.data2viz.kotlinx.htmlplugin.conversion.data.HtmlAttribute
import com.data2viz.kotlinx.htmlplugin.conversion.data.HtmlTag
import com.data2viz.kotlinx.htmlplugin.conversion.model.toKotlinX
import org.junit.Assert
import org.junit.Test

import org.junit.Assert.*

class HtmlDataToHtmlKotlinXTest {

    @Test
    fun HtmlTagtoKotlinXBase() {
        Assert.assertEquals("div {\n}", HtmlTag("div").toKotlinX())
    }

    @Test
    fun HtmlTagtoKotlinXBody() {
        val htmlTag = HtmlTag("div")
        htmlTag.body = "text1"
        Assert.assertEquals("div {\n\t+ \"text1\"\n}", htmlTag.toKotlinX())
    }


    @Test
    fun HtmlTagtoKotlinXNested() {
        val htmlTag = HtmlTag("div")
        htmlTag.children.add(HtmlTag("p"))
        htmlTag.children.add(HtmlTag("span"))
        Assert.assertEquals("div {\n\tp {\n\t}\n\tspan {\n\t}\n}", htmlTag.toKotlinX())
    }


    @Test
    fun HtmlTagtoKotlinXNestedTwice() {
        val htmlTag = HtmlTag("div")
        val inner = HtmlTag("p")
        htmlTag.children.add(inner)
        inner.children.add(HtmlTag("span"))
        Assert.assertEquals("div {\n\tp {\n\t\tspan {\n\t\t}\n\t}\n}", htmlTag.toKotlinX())
    }


    @Test
    fun HtmlTagtoKotlinXBaseAttributes() {
        val htmlTag = HtmlTag("div")
        htmlTag.attributes.add(HtmlAttribute("attr1"))
        htmlTag.attributes.add(HtmlAttribute("attr2", "value2"))

        Assert.assertEquals("div {\n\tattr1\n\tattr2 = \"value2\"\n}", htmlTag.toKotlinX())
    }

    @Test
    fun HtmlAttributetoKotlinXWithoutValue() {
        Assert.assertEquals("attr_name", HtmlAttribute("attr_name").toKotlinX())
    }

    @Test
    fun HtmlAttributetoKotlinXWithValue() {
        Assert.assertEquals("attr_name = \"attr_value\"", HtmlAttribute("attr_name", "attr_value").toKotlinX())
    }
}