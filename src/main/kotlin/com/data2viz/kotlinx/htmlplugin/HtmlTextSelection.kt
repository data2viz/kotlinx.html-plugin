package com.data2viz.kotlinx.htmlplugin

import com.intellij.codeInsight.editorActions.TextBlockTransferableData

import java.awt.datatransfer.DataFlavor


class HtmlTextSelection(val fileName: String, val fileText: String, val startOffsets: IntArray, val endOffsets: IntArray, val fromHtmlFile: Boolean) : TextBlockTransferableData {

    override fun getFlavor(): DataFlavor {
        return ConvertTextHTMLCopyPasteProcessorKt.dataFlavor;
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

    fun isFromHtmlFile(): Boolean {
        return this.fromHtmlFile
    }
}
