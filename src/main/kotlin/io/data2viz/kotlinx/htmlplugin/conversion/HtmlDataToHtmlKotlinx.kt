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
fun HtmlTag.isInline(): Boolean =
        children.size == 1 &&
        children[0] is HtmlText &&
        attributes.filterBodyAttributes().isEmpty()

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

    if (constructorAttributes.isNotEmpty()) {
        sb.append("(")


        if (tagNameLowerCase in tagsWithOnlyClasses
                && constructorAttributes.size == 1
                && constructorAttributes[0].attrName.toLowerCase() == "class") {
            sb.append(""""${constructorAttributes[0].value}"""")

        } else {
            sb.append(constructorAttributes.joinToString(", ") { attribute ->
                attribute.toKotlinx(this)
            })
        }


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
            sb.append(attribute.toKotlinx(this))
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

fun HtmlAttribute.toKotlinx(owner: HtmlTag? = null): String {
    // remap for kotlinx
    val attrNameLowerCase = attrName.toLowerCase()
    val attrValue = """"$value""""
    var attrName = when (attrNameLowerCase) {
        "class" -> "classes"
        else -> attrNameLowerCase
    }

    // remap for by owner tag name
    if(owner != null) {
        when(owner.tagName.toLowerCase()) {
            "label" -> {
                when(attrNameLowerCase) {
                    "for" -> attrName = "htmlFor"
                }
            }
        }
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



val tagsWithOnlyClasses = """
abbr
address
article
aside
audio
b
base
bdi
bdo
blockQuote
body
br
canvas
canvas
caption
cite
code
col
colGroup
dataList
dd
del
details
dfn
dialog
div
dl
dt
em
embed
fieldSet
figcaption
figure
footer
h1
h2
h3
h4
h5
h6
header
hGroup
hr
i
ins
kbd
label
legend
li
main
mark
math
mathml
mathml
meter
nav
noScript
htmlObject
ol
option
option
output
p
pre
progress
q
rp
rt
ruby
samp
section
select
small
source
span
strong
sub
sup
svg
svg
table
tbody
td
tfoot
thead
time
tr
ul
htmlVar
video
""".trimIndent().lines().toSet()