package com.data2viz.kotlinx.htmlplugin

import com.data2viz.kotlinx.htmlplugin.conversion.data.HtmlTag
import com.data2viz.kotlinx.htmlplugin.conversion.data.HtmlText
import org.junit.Assert
import org.junit.Test

class HtmlTextToHtmlDataTest : ResourcesTest() {


    @Test
    fun fileHtmlToHtmlDataBase() {
        val htmlTags = loadHtmlData("base.html")

        Assert.assertEquals(1, htmlTags.size)


        (htmlTags[0] as HtmlTag).apply {
            Assert.assertEquals("div", tagName)
        }

    }

    @Test
    fun fileHtmlToHtmlDataAttrsBase() {
        val htmlTags = loadHtmlData("attrs_base.html")

        Assert.assertEquals(1, htmlTags.size)

        (htmlTags[0] as HtmlTag).apply {
            Assert.assertEquals("div", tagName)
            Assert.assertEquals(2, attributes.size)
            attributes[0].apply {
                Assert.assertEquals("attr1", attrName)
                Assert.assertEquals(null, value)
            }
            attributes[1].apply {
                Assert.assertEquals("attr2", attrName)
                Assert.assertEquals("value2", value)
            }


        }

    }

    @Test
    fun fileHtmlToHtmlDataNested() {
        val htmlTags = loadHtmlData("nested.html")

        Assert.assertEquals(1, htmlTags.size)


        (htmlTags[0] as HtmlTag).apply {
            Assert.assertEquals("div", tagName)
            Assert.assertEquals(1, children.size)
            (children[0] as HtmlTag).apply {
                Assert.assertEquals("div", tagName)
            }

        }

    }

    @Test
    fun fileHtmlToHtmlDataNestedSeveralChilds() {
        val htmlTags = loadHtmlData("nested_several_text_childs.html")

        Assert.assertEquals(1, htmlTags.size)


        (htmlTags[0] as HtmlTag).apply {
            Assert.assertEquals("div", tagName)
            Assert.assertEquals(4, children.size)
            (children[0] as HtmlText).apply {
                Assert.assertEquals("Text", text)
            }
            (children[1] as HtmlTag).apply {
                Assert.assertEquals("b", tagName)
            }

            (children[2] as HtmlText).apply {
                Assert.assertEquals("and", text)
            }

            (children[3] as HtmlTag).apply {
                Assert.assertEquals("i", tagName)
            }

        }


    }


}