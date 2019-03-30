package com.data2viz.kotlinx.htmlplugin.conversion.model

import com.data2viz.kotlinx.htmlplugin.conversion.data.HtmlAttribute
import com.data2viz.kotlinx.htmlplugin.conversion.data.HtmlTag
import com.data2viz.kotlinx.htmlplugin.conversion.data.HtmlText
import com.data2viz.kotlinx.htmlplugin.conversion.data.HtmlElement
import java.lang.StringBuilder


val INDENT = "    " // 4 spaces

fun StringBuilder.addTabIndent(currentIndent: Int) {
    for (i in 1..currentIndent) {
        append(INDENT)
    }
}

fun HtmlElement.toKotlinX(currentIndent: Int = 0): String {

    return when (this) {
        is HtmlTag -> toKotlinX(currentIndent)
        is HtmlText -> toKotlinX(currentIndent)
        else -> {
            throw AssertionError("${this.javaClass.typeName} not supported")
        }
    }
}


fun HtmlTag.toKotlinX(currentIndent: Int = 0): String {
    val sb = StringBuilder();

    sb.addTabIndent(currentIndent)
    sb.append("$name {\n")


    for (attribute in attributes) {
        sb.addTabIndent(currentIndent + 1)
        sb.append(attribute.toKotlinX())
        sb.append("\n")
    }

    for (child in children) {
        sb.append(child.toKotlinX(currentIndent + 1))
        sb.append("\n")
    }
    sb.addTabIndent(currentIndent)
    sb.append("}")

    return sb.toString()
}


fun HtmlText.toKotlinX(currentIndent: Int = 0): String {
    val sb = StringBuilder();


    sb.addTabIndent(currentIndent)
    sb.append("+ \"$text\"")
    sb.append("\n")

    return sb.toString()
}


fun Collection<HtmlElement>.toKotlinX(currentIndent: Int = 0): String {
    val sb = StringBuilder()

    val last = last();
    for (htmlTag in this) {

        sb.append("${htmlTag.toKotlinX()}")

        if (htmlTag != last) {
            sb.append("\n}")
        }
    }

    return sb.toString()
}


fun HtmlAttribute.toKotlinX(currentIndent: Int = 0): String {

    val result: String
    if (value != null) {
        result = "$name = \"$value\""
    } else {
        result = name

    }

    return result
}

