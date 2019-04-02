package com.data2viz.kotlinx.htmlplugin.conversion.model

import com.data2viz.kotlinx.htmlplugin.conversion.data.HtmlAttribute
import com.data2viz.kotlinx.htmlplugin.conversion.data.HtmlTag
import com.data2viz.kotlinx.htmlplugin.conversion.data.HtmlText
import com.data2viz.kotlinx.htmlplugin.conversion.data.HtmlElement
import java.lang.StringBuilder


val INDENT = "    " // 4 spaces

fun StringBuilder.addTabIndent(currentIndent: Int) {
    repeat(currentIndent) {
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
 * Tags with only one text child is inline
 */
fun HtmlTag.isInline(): Boolean = children.size == 1 && children[0] is HtmlText

// Custom attributes, which not supported by KotlinX as fields, for example data-* or aria-*
fun HtmlAttribute.isCustomForTag(tag: HtmlTag): Boolean {
    var custom = attrName.contains("-")

    if (!custom) {
        when (tag.tagName) {
            "link" -> {
                custom = (attrName == "integrity") or (attrName == "crossorigin")
            }
        }
    }

    return custom
}

fun HtmlTag.toKotlinX(currentIndent: Int = 0): String {
    val inline = isInline();
    val sb = StringBuilder();

    sb.addTabIndent(currentIndent)
    sb.append("$tagName")

    val defaultAttributes = attributes.filter { !it.isCustomForTag(this) }

    if (defaultAttributes.isNotEmpty()) {
        sb.append("(")
        val lastIndex = defaultAttributes.size - 1
        for ((index, attribute) in defaultAttributes.withIndex()) {


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


    val customAttributes = attributes.filter { it.isCustomForTag(this) }
    if (customAttributes.isNotEmpty()) {

        for (attribute in customAttributes) {


            sb.addTabIndent(currentIndent + 1)
            sb.append(attribute.toKotlinXCustomAttribute())

            sb.append("\n")
        }
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
    val convertText = convertText(text)
    sb.append("+ \"$convertText\"")

    return sb.toString()
}

fun convertText(text: String): String = text.replace("\"", "\\\"")


fun Collection<HtmlElement>.toKotlinX(currentIndent: Int = 0): String {
    val sb = StringBuilder()

    if (size > 0) {
        val last = last();
        for (htmlTag in this) {

            sb.append("${htmlTag.toKotlinX(currentIndent)}")

            if (htmlTag != last) {
                sb.append("\n")
            }
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
        result = "$attrName = \"true\""
    }

    return result
}


fun HtmlAttribute.toKotlinXCustomAttribute(): String =
        when {
            value != null -> """attributes["$attrName"] = "$value" """
            else -> """attributes["$attrName"] = "true" """
        }
