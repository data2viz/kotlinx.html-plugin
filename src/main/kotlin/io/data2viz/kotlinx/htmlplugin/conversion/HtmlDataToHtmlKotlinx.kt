package io.data2viz.kotlinx.htmlplugin.conversion


const val INDENT = "    " // 4 spaces

fun StringBuilder.addTabIndent(currentIndent: Int) = repeat(currentIndent) { append(INDENT) }

fun HtmlElement.toKotlinx(currentIndent: Int = 0): String =
    when (this) {
        is HtmlTag -> toKotlinx(currentIndent)
        is HtmlText -> toKotlinx(currentIndent)
        else        -> error("${this.javaClass.typeName} not supported")
    }

/**
 * Tags with only one text child is inline
 */
fun HtmlTag.isInline(): Boolean = children.size == 1 && children[0] is HtmlText

/**
 * Custom attributes, which not supported by KotlinX as fields, for example data-* or aria-*
 */
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

fun HtmlTag.toKotlinx(currentIndent: Int = 0): String {
    val inline = isInline()
    val sb = StringBuilder()

    sb.addTabIndent(currentIndent)
    sb.append(tagName)

    val defaultAttributes = attributes.filter { !it.isCustomForTag(this) }

    if (defaultAttributes.isNotEmpty()) {
        sb.append("(")
        val lastIndex = defaultAttributes.size - 1
        for ((index, attribute) in defaultAttributes.withIndex()) {
            sb.append(attribute.toKotlinx())
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

    attributes
            .filter { it.isCustomForTag(this) }
            .forEach { attribute ->
                sb.addTabIndent(currentIndent + 1)
                sb.append(attribute.toKotlinxCustomAttribute())
                sb.append("\n")
            }

    for (child in children) {
        if (!inline) {
            sb.appendln(child.toKotlinx(currentIndent + 1))
        } else {

            // add space before + inline
            sb.append(" ")
            sb.append(child.toKotlinx(0))
        }
    }

    if (!inline) {
        sb.addTabIndent(currentIndent)
    }
    sb.append("}")
    return sb.toString()
}


fun HtmlText.toKotlinx(currentIndent: Int = 0): String =
    StringBuilder().apply {
        addTabIndent(currentIndent)
        append("""+ "${convertText(text)}"""")
    }.toString()

fun convertText(text: String): String = text.replace("\"", "\\\"")


fun Collection<HtmlElement>.toKotlinx(currentIndent: Int = 0): String =
        joinToString("\n") { it.toKotlinx(currentIndent) }

fun HtmlAttribute.toKotlinx(): String {
    // remap for kotlinx
    val attrName = when (attrName) {
        "class" -> "classes"
        else -> attrName
    }

    return when {
        value != null   -> """$attrName = "$value" """.trim()
        else            -> """$attrName = "true" """.trim()
    }
}

fun HtmlAttribute.toKotlinxCustomAttribute(): String =
        when {
            value != null   -> """attributes["$attrName"] = "$value" """.trim()
            else            -> """attributes["$attrName"] = "true" """.trim()
        }
