package io.data2viz.kotlinx.htmlplugin

import io.data2viz.kotlinx.htmlplugin.conversion.data.HtmlAttribute
import io.data2viz.kotlinx.htmlplugin.conversion.data.HtmlTag
import io.data2viz.kotlinx.htmlplugin.conversion.data.HtmlText
import io.data2viz.kotlinx.htmlplugin.conversion.model.INDENT
import io.data2viz.kotlinx.htmlplugin.conversion.model.isCustomForTag
import io.data2viz.kotlinx.htmlplugin.conversion.model.isInline
import io.data2viz.kotlinx.htmlplugin.conversion.model.toKotlinX
import org.junit.Assert
import org.junit.Test

class HtmlDataToHtmlKotlinXTest {

    @Test
    fun HtmlTagtoKotlinXBase() {
        Assert.assertEquals("div {\n}", HtmlTag("div").toKotlinX())
    }

    @Test
    fun HtmlAttributeIsCustom() {

        var htmlTag = HtmlTag("link")

        var htmlAttribute = HtmlAttribute("data-label")
        Assert.assertEquals(true, htmlAttribute.isCustomForTag(htmlTag))
        htmlAttribute = HtmlAttribute("data")
        Assert.assertEquals(false, htmlAttribute.isCustomForTag(htmlTag))

        htmlAttribute = HtmlAttribute("link")
        Assert.assertEquals(false, htmlAttribute.isCustomForTag(htmlTag))

        htmlAttribute = HtmlAttribute("integrity", "integrity")
        Assert.assertEquals(true, htmlAttribute.isCustomForTag(htmlTag))

        htmlAttribute = HtmlAttribute("crossorigin", "crossorigin")
        Assert.assertEquals(true, htmlAttribute.isCustomForTag(htmlTag))

        htmlAttribute = HtmlAttribute("link", "src")
        Assert.assertEquals(false, htmlAttribute.isCustomForTag(htmlTag))

    }

    @Test
    fun HtmlTagIsInline() {
        var htmlTag = HtmlTag("div")
        Assert.assertEquals(false, htmlTag.isInline())
        htmlTag.children.add(HtmlText("text"))

        Assert.assertEquals(true, htmlTag.isInline())
        htmlTag.children.add(HtmlText("text"))
        Assert.assertEquals(false, htmlTag.isInline())

        htmlTag = HtmlTag("div")
        htmlTag.children.add(HtmlTag("div"))
        Assert.assertEquals(false, htmlTag.isInline())
        htmlTag.children.add(HtmlTag("div"))
        Assert.assertEquals(false, htmlTag.isInline())

    }

    @Test
    fun HtmlTagtoKotlinXInline() {
        val htmlTag = HtmlTag("div")
        htmlTag.children.add(HtmlText("text"))
        Assert.assertEquals("div { + \"text\"}", htmlTag.toKotlinX())
    }


    @Test
    fun HtmlTagtoKotlinXNested() {
        val htmlTag = HtmlTag("div")
        htmlTag.children.add(HtmlTag("p"))
        htmlTag.children.add(HtmlTag("span"))
        Assert.assertEquals("div {\n${INDENT}p {\n$INDENT}\n${INDENT}span {\n$INDENT}\n}", htmlTag.toKotlinX())
    }


    @Test
    fun HtmlTagtoKotlinXNestedTwice() {
        val htmlTag = HtmlTag("div")
        val inner = HtmlTag("p")
        htmlTag.children.add(inner)
        inner.children.add(HtmlTag("span"))
        Assert.assertEquals("div {\n${INDENT}p {\n$INDENT${INDENT}span {\n$INDENT$INDENT}\n$INDENT}\n}", htmlTag.toKotlinX())
    }


    @Test
    fun HtmlTagtoKotlinXBaseAttributes() {
        val htmlTag = HtmlTag("div")
        htmlTag.attributes.add(HtmlAttribute("attr1"))
        htmlTag.attributes.add(HtmlAttribute("attr2", "value2"))

        Assert.assertEquals("div(attr1 = \"true\", attr2 = \"value2\") {\n}", htmlTag.toKotlinX())
    }


    @Test
    fun HtmlTagtoKotlinXCustomAttributes() {
        val htmlTag = HtmlTag("div")

        htmlTag.attributes.add(HtmlAttribute("aria-label1"))
        htmlTag.attributes.add(HtmlAttribute("aria-label2", "value2"))

        Assert.assertEquals("div {\n${INDENT}attributes[\"aria-label1\"] = \"true\"\n${INDENT}attributes[\"aria-label2\"] = \"value2\"\n}", htmlTag.toKotlinX())
    }

    @Test
    fun HtmlAttributetoKotlinXWithoutValue() {
        Assert.assertEquals("attr_name = \"true\"", HtmlAttribute("attr_name").toKotlinX())
    }

    @Test
    fun HtmlAttributetoKotlinXWithValue() {
        Assert.assertEquals("attr_name = \"attr_value\"", HtmlAttribute("attr_name", "attr_value").toKotlinX())
    }
}