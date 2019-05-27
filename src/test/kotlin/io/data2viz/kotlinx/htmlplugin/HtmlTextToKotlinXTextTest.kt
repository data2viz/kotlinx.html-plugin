package io.data2viz.kotlinx.htmlplugin


import io.data2viz.kotlinx.htmlplugin.conversion.toKotlinx
import org.junit.Assert
import org.junit.Test

class HtmlTextToKotlinXTextTest : ResourcesTest() {


    @Test
    fun FileHtmlToKotlinXBase() {
        assertFiles("base.html", "base.htmlkotlinx")
    }

    @Test
    fun FileHtmlToKotlinXAttrsBase() {
        assertFiles("attrs_base.html", "attrs_base.htmlkotlinx")
    }

    @Test
    fun FileHtmlToKotlinXAttrsCustom() {
        assertFiles("attrs_custom.html", "attrs_custom.htmlkotlinx")
    }

    @Test
    fun FileHtmlToKotlinXAttrsImg() {
        assertFiles("attrs_img.html", "attrs_img.htmlkotlinx")
    }


    @Test
    fun FileHtmlToKotlinXAttrsA() {
        assertFiles("attrs_a.html", "attrs_a.htmlkotlinx")
    }


    @Test
    fun FileHtmlToKotlinXAttrsInline() {
        assertFiles("inline.html", "inline.htmlkotlinx")
    }

    @Test
    fun FileHtmlToKotlinXNested() {
        assertFiles("nested.html", "nested.htmlkotlinx")
    }

    @Test
    fun FileHtmlToKotlinXNestedSeveralTextChilds() {
        assertFiles("nested_several_text_childs.html", "nested_several_text_childs.htmlkotlinx")
    }

    @Test
    fun FileHtmlToKotlinXClasses() {
        assertFiles("attrs_class.html", "attrs_class.htmlkotlinx")
    }

    private fun assertFiles(filenameHtml: String, filenameKotlinX: String) {

        val kotlinXText = loadFileText(filenameKotlinX)

        val htmlTags = loadHtmlData(filenameHtml)

        val convertedToKotlinXText = htmlTags.toKotlinx()

        Assert.assertEquals(kotlinXText, convertedToKotlinXText)

    }


}