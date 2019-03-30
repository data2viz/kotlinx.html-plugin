package com.data2viz.kotlinx.htmlplugin.conversion.data

class HtmlTag(val name: String) {

    val attributes: MutableList<HtmlAttribute> = mutableListOf()
    val children: MutableList<HtmlTag> = mutableListOf()
    var body: String? = null


}