package io.data2viz.kotlinx.htmlplugin.conversion


const val INDENT = "    " // 4 spaces

fun StringBuilder.addTabIndent(currentIndent: Int) = repeat(currentIndent) { append(INDENT) }

fun HtmlElement.toKotlinx(currentIndent: Int = 0): String =
        when (this) {
            is HtmlTag -> toKotlinx(currentIndent)
            is HtmlText -> toKotlinxText(currentIndent)
            else -> error("${this.javaClass.typeName} not supported")
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


    sb.append(" {")
    if (!inline) {
        sb.append("\n")
    }

    attributes
            .forEach { attribute ->

                sb.addTabIndent(currentIndent + 1)
                if (attribute.isCustomForTag(this)) {
                    sb.append(attribute.toKotlinxCustomAttribute())
                } else {
                    sb.append(attribute.toKotlinx())
                }
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


fun HtmlText.toKotlinxText(currentIndent: Int = 0): String =
        StringBuilder().apply {
            addTabIndent(currentIndent)
            append("""+ "${escapeChars(text)}"""")
        }.toString()

fun escapeChars(text: String): String = text.replace("\"", "\\\"")


fun Collection<HtmlElement>.toKotlinx(currentIndent: Int = 0): String =
        joinToString("\n") { it.toKotlinx(currentIndent) }

fun HtmlAttribute.toKotlinx(): String {
    // remap for kotlinx
    val attrValue = when (attrName) {
        "class" -> convertClassesStringToClassSetKotlinx(value ?: "")
        else -> """"$value""""
    }
    val attrName = when (attrName) {
        "class" -> "classes"
        else -> attrName
    }


    return when {
        value != null -> """$attrName = $attrValue""".trim()
        else -> """$attrName = "true" """.trim()
    }

}

fun convertClassesStringToClassSetKotlinx(classString: String): String =
        """setOf(${classString.split(' ').joinToString(
                separator = "\", \"",
                prefix = "\"",
                postfix = "\""
        )})"""

fun HtmlAttribute.toKotlinxCustomAttribute(): String =
        when {
            value != null -> """attributes["$attrName"] = "$value" """.trim()
            else -> """attributes["$attrName"] = "true" """.trim()
        }
