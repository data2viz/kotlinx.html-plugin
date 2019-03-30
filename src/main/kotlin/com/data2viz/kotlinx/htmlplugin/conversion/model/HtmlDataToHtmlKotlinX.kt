package com.data2viz.kotlinx.htmlplugin.conversion.model

import com.data2viz.kotlinx.htmlplugin.conversion.data.HtmlAttribute
import com.data2viz.kotlinx.htmlplugin.conversion.data.HtmlTag
import java.lang.StringBuilder


fun HtmlTag.toKotlinX(): String {
    val sb = StringBuilder();

    sb.append("$name {\n")


    for (attribute in attributes) {
        sb.append(attribute.toKotlinX())
        sb.append("\n")
    }

    body?.let {
        sb.append("+ \"$it\"")
        sb.append("\n")
    }

    for (child in children) {
        sb.append(child.toKotlinX())
        sb.append("\n")
    }
    sb.append("}\n")

    return sb.toString()
}


fun HtmlAttribute.toKotlinX(): String {

    val result: String
    if (value != null) {
        result = "$name = \"$value\""
    } else {
        result = name

    }

    return result
}

