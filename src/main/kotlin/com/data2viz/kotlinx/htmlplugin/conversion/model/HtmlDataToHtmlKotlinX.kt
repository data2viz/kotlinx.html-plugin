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

    if (children.size == 1) {
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
    sb.append("$tagName")


    val attributesSize = attributes.size

    if (attributesSize > 0) {
        sb.append("(")
        val lastIndex = attributesSize - 1
        for ((index, attribute) in attributes.withIndex()) {
            sb.append(attribute.toKotlinX())
            if (index != lastIndex) {
                sb.append(", ")
            }
        }
        sb.append(")")
    }

    sb.append(" {")
    if (!inline) {
        sb.append("\n")
    }

    for (child in children) {
        if (!inline) {
            sb.append(child.toKotlinX(currentIndent + 1))
            sb.append("\n")
        } else {

            // add space before + inline
            sb.append(" ")
            sb.append(child.toKotlinX(0))
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

    // remap for kotlinx
    val attrName = when (attrName) {
        "class" -> "classes"
        else -> attrName
    }

    if (value != null) {
        result = "$attrName = \"$value\""
    } else {
        // empty attrs it is boolean attrs
        result = "$attrName = true"
    }

    return result
}

