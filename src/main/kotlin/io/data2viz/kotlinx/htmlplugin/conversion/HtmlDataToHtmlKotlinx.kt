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

fun Collection<HtmlAttribute>.filterConstructorAttributes(): List<HtmlAttribute> =
        filter { it.isConstructorAttribute() }
fun Collection<HtmlAttribute>.filterBodyAttributes(): List<HtmlAttribute> =
        filter { !it.isConstructorAttribute() }


fun HtmlAttribute.isConstructorAttribute(): Boolean = when (attrName) {
    "class" -> true
    else -> false
}


fun HtmlTag.toKotlinx(currentIndent: Int = 0): String {
    val inline = isInline()
    val sb = StringBuilder()

    sb.addTabIndent(currentIndent)
    val tagNameLowerCase = tagName.toLowerCase()

    val kotlinXTagName = when (tagNameLowerCase) {
        "textarea" -> "textArea"
        else -> tagNameLowerCase
    }

    sb.append(kotlinXTagName)


    val constructorAttributes = attributes.filterConstructorAttributes()

    if (!constructorAttributes.isEmpty()) {
        sb.append("(")

        sb.append(constructorAttributes.joinToString(", ") { attribute ->
            attribute.toKotlinx()
        })

        sb.append(")")
    }


    sb.append(" {")
    if (!inline) {
        sb.append("\n")
    }

    attributes.filterBodyAttributes().forEach { attribute ->
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
            append("+ \"\"\"$text\"\"\"")
        }.toString()


fun Collection<HtmlElement>.toKotlinx(currentIndent: Int = 0): String =
        joinToString("\n") { it.toKotlinx(currentIndent) }

fun HtmlAttribute.toKotlinx(): String {
    // remap for kotlinx
    val attrNameLowerCase = attrName.toLowerCase()
    val attrValue = """"$value""""
    val attrName = when (attrNameLowerCase) {
        "class" -> "classes"
        else -> attrNameLowerCase
    }


    return when {
        value != null -> """$attrName = $attrValue""".trim()
        else -> """$attrName = "true" """.trim()
    }

}

fun HtmlAttribute.toKotlinxCustomAttribute(): String =
        when {
            value != null -> """attributes["$attrName"] = "$value" """.trim()
            else -> """attributes["$attrName"] = "true" """.trim()
        }



