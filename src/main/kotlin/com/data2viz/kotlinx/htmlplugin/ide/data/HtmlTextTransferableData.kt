package com.data2viz.kotlinx.htmlplugin.ide.data

import com.data2viz.kotlinx.htmlplugin.ide.controller.ConvertTextHTMLCopyPasteProcessor
import com.data2viz.kotlinx.htmlplugin.ide.controller.ConvertTextHTMLCopyPasteProcessorKt
import com.intellij.codeInsight.editorActions.TextBlockTransferableData
import com.intellij.openapi.diagnostic.Logger

import java.awt.datatransfer.DataFlavor


class HtmlTextTransferableData(val fileName: String, val fileText: String, val startOffsets: IntArray, val endOffsets: IntArray, val isFromHtmlFile: Boolean) : TextBlockTransferableData {

    override fun getFlavor(): DataFlavor {
        return dataFlavor;
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
        var dataFlavor: DataFlavor = DataFlavor(ConvertTextHTMLCopyPasteProcessor::class.java, "class: com.data2viz.kotlinx.htmlplugin.ide.controller.ConvertTextHTMLCopyPasteProcessor")

    }
}
