package com.data2viz.kotlinx.htmlplugin.conversion.data

class HtmlTag(val name: String): HtmlElement {

    val attributes: MutableList<HtmlAttribute> = mutableListOf()
    val children: MutableList<HtmlElement> = mutableListOf()

}