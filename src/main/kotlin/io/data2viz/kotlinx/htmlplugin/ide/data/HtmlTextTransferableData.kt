package io.data2viz.kotlinx.htmlplugin.ide.data

import com.intellij.codeInsight.editorActions.TextBlockTransferableData
import io.data2viz.kotlinx.htmlplugin.ide.controller.ConvertTextHTMLCopyPasteProcessor
import java.awt.datatransfer.DataFlavor


open class HtmlTextTransferableData(val fileName: String, val fileText: String, val startOffsets: IntArray, val endOffsets: IntArray, val isFromHtmlFile: Boolean) : TextBlockTransferableData {

    override fun getFlavor(): DataFlavor {
        return dataFlavor
    }

    override fun getOffsetCount(): Int {
        return 0
    }

    override fun getOffsets(offsets: IntArray?, index: Int): Int {
        return index
    }

    override fun setOffsets(offsets: IntArray?, index: Int): Int {
        return index
    }

    companion object {
        var dataFlavor: DataFlavor = DataFlavor(ConvertTextHTMLCopyPasteProcessor::class.java, "class: ConvertTextHTMLCopyPasteProcessor")

    }
}

class ExternalFileHtmlTextTransferableData(
        fileText: String)
    : HtmlTextTransferableData(
        "external", fileText, intArrayOf(0), intArrayOf(fileText.length), false)