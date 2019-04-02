package io.data2viz.kotlinx.htmlplugin.ide.data

import com.intellij.codeInsight.editorActions.TextBlockTransferableData
import io.data2viz.kotlinx.htmlplugin.ide.controller.ConvertTextHTMLCopyPasteProcessor
import java.awt.datatransfer.DataFlavor


val htmlDataFlavor: DataFlavor = DataFlavor(ConvertTextHTMLCopyPasteProcessor::class.java, "class: ConvertTextHTMLCopyPasteProcessor")

open class HtmlTextTransferableData(val fileName: String,
                                    val fileText: String,
                                    val startOffsets: IntArray,
                                    val endOffsets: IntArray,
                                    val isFromHtmlFile: Boolean) : TextBlockTransferableData {

    override fun getFlavor(): DataFlavor = htmlDataFlavor
    override fun getOffsetCount(): Int = 0
    override fun getOffsets(offsets: IntArray?, index: Int): Int = index
    override fun setOffsets(offsets: IntArray?, index: Int): Int = index

}

class ExternalFileHtmlTextTransferableData(
        fileText: String)
    : HtmlTextTransferableData(
        "external", fileText, intArrayOf(0), intArrayOf(fileText.length), false)