package com.data2viz.kotlinx.htmlplugin.conversion.data

class HtmlTag(val tagName: String): HtmlElement {

    val attributes: MutableList<HtmlAttribute> = mutableListOf()
    val children: MutableList<HtmlElement> = mutableListOf()

}