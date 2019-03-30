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


/**
 * Tags with only one text is inline
 */
fun HtmlTag.isInline(): Boolean {
    val isInline: Boolean

    if (children.size > 1) {
        isInline = children[0] is HtmlText
    } else {
        isInline = false
    }

    return isInline
}

fun HtmlTag.toKotlinX(currentIndent: Int = 0): String {
    val inline = isInline();
    val sb = StringBuilder();

    sb.addTabIndent(currentIndent)
    sb.append("$name (")


    val lastIndex = attributes.size - 1
    for ((index, attribute) in attributes.withIndex()) {
        sb.append(attribute.toKotlinX())
        if (index != lastIndex) {
            sb.append(", ")
        }
    }

    sb.append(") {")
    if (!inline) {
        sb.append("\n")
    }

    for (child in children) {
        if (inline) {
            sb.append(child.toKotlinX(currentIndent + 1))
            sb.append("\n")
        } else {

            sb.append(child.toKotlinX())
        }
    }

    if (!inline) {
        sb.addTabIndent(currentIndent)
    }

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

        sb.append("${htmlTag.toKotlinX(currentIndent)}")

        if (htmlTag != last) {
            sb.append("\n}")
        }
    }

    return sb.toString()
}


fun HtmlAttribute.toKotlinX(): String {

    val result: String
    if (value != null) {
        result = "$name = \"$value\""
    } else {
        // empty attrs it is boolean attrs
        result = "$name = true"
    }

    return result
}

