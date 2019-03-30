package com.data2viz.kotlinx.htmlplugin.ide.controller

import com.data2viz.kotlinx.htmlplugin.conversion.model.HtmlPsiToHtmlDataConverter
import com.data2viz.kotlinx.htmlplugin.conversion.model.converToHtmlElements
import com.data2viz.kotlinx.htmlplugin.conversion.model.toKotlinX
import com.data2viz.kotlinx.htmlplugin.ide.data.ExternalFileHtmlTextTransferableData
import com.data2viz.kotlinx.htmlplugin.ide.data.HtmlTextTransferableData
import com.data2viz.kotlinx.htmlplugin.ide.view.KotlinPasteFromHtmlDialog
import com.intellij.codeInsight.editorActions.CopyPastePostProcessor
import com.intellij.codeInsight.editorActions.TextBlockTransferableData
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.editor.CaretStateTransferableData
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.RangeMarker
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Ref
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.DumbService
import com.intellij.psi.*
import com.intellij.psi.impl.source.html.HtmlFileImpl

import java.awt.datatransfer.DataFlavor
import java.awt.datatransfer.Transferable


open class ConvertTextHTMLCopyPasteProcessorKt: CopyPastePostProcessor<TextBlockTransferableData>() {


    companion object {

        private val LOGGER = Logger.getInstance(ConvertTextHTMLCopyPasteProcessorKt::class.java)
    }

    override fun collectTransferableData(file: PsiFile, editor: Editor, startOffsets: IntArray, endOffsets: IntArray): MutableList<TextBlockTransferableData> {


        val resultList = mutableListOf<TextBlockTransferableData>()

        val isHtmlFile = file is HtmlFileImpl

        LOGGER.debug("collectTransferableData isHtmlFile=$isHtmlFile");
        if (isHtmlFile) {
            val htmlTextSelection = HtmlTextTransferableData(file.name, file.text, startOffsets, endOffsets, true)
            resultList.add(htmlTextSelection)
        }

        return resultList

    }

    override fun extractTransferableData(content: Transferable): List<TextBlockTransferableData> {


        val result = mutableListOf<TextBlockTransferableData>()

        val dataFlavor = HtmlTextTransferableData.dataFlavor
        val isCopyFromHtmlFile = content.isDataFlavorSupported(dataFlavor)

        LOGGER.debug("extractTransferableData isCopyFromHtmlFile=$isCopyFromHtmlFile")
        if (isCopyFromHtmlFile) {
            val transferableData = content.getTransferData(dataFlavor) as TextBlockTransferableData
            result.add(transferableData)

        } else {

            val isNotCopyPasteFromIdeaFile = !content.isDataFlavorSupported(CaretStateTransferableData.FLAVOR)
            val isSomeStringContent = content.isDataFlavorSupported(DataFlavor.stringFlavor)

            LOGGER.debug("extractTransferableData isNotCopyPasteFromIdeaFile=$isNotCopyPasteFromIdeaFile");
            LOGGER.debug("extractTransferableData isSomeStringContent=$isSomeStringContent");
            if (isNotCopyPasteFromIdeaFile && isSomeStringContent) {
                val transferableData = content.getTransferData(DataFlavor.stringFlavor)

                val str = transferableData as String

                val external = ExternalFileHtmlTextTransferableData(str)
                result.add(external)

            }
        }

        LOGGER.debug("extractTransferableData result.size=${result.size}");
        return result
    }

    override fun processTransferableData(project: Project, editor: Editor, bounds: RangeMarker, caretOffset: Int, indented: Ref<Boolean>, textValues: MutableList<TextBlockTransferableData>) {

        val isDump = DumbService.getInstance(project).isDumb
        LOGGER.debug("processTransferableData isDump=$isDump");
        if (isDump) {
            return
        }


        val psiDocumentManager = PsiDocumentManager.getInstance(project)
        val targetPsiFile = psiDocumentManager.getPsiFile(editor.document)
        LOGGER.debug("processTransferableData targetPsiFile=$targetPsiFile");

        if (targetPsiFile == null) {
            return
        }


        val textValuesSize = textValues.size

        LOGGER.debug("processTransferableData textValuesSize=$textValuesSize");
        if (textValuesSize != 1) {
            return
        }
        val textBlockTransferableData = textValues[0]

        LOGGER.debug("processTransferableData textBlockTransferableData=$textBlockTransferableData");

        if (textBlockTransferableData !is HtmlTextTransferableData) {
            return
        }
        val htmlTextTransferableData = textBlockTransferableData



        val text = htmlTextTransferableData.fileText
        val fileName = htmlTextTransferableData.fileName
        val sourcePsiFileFromText: PsiFile = HtmlPsiToHtmlDataConverter.createHtmlFileFromText(project, fileName, text)
        LOGGER.debug("processTransferableData sourcePsiFileFromText=$sourcePsiFileFromText")
        if (sourcePsiFileFromText !is HtmlFileImpl) {
            return
        }

        val isFromHtmlFile = htmlTextTransferableData.isFromHtmlFile
        LOGGER.debug("processTransferableData isFromHtmlFile=$isFromHtmlFile")

        if (!isFromHtmlFile) {
            val looksLikeHtml = HtmlPsiToHtmlDataConverter.isLooksLikeHtml(sourcePsiFileFromText)
            LOGGER.debug("processTransferableData isLooksLikeHtml=$looksLikeHtml")
            if (!looksLikeHtml) {
                return
            }
        }


        val htmlElements = sourcePsiFileFromText.converToHtmlElements()

        val convertedToKotlinText = htmlElements.toKotlinX()


        val notEmpty = convertedToKotlinText.isNotEmpty()
        LOGGER.debug("processTransferableData notEmpty = $notEmpty  convertedToKotlinText=$convertedToKotlinText");
        if (notEmpty) {


            val dialogIsOk = KotlinPasteFromHtmlDialog.isOK(project)
            LOGGER.debug("processTransferableData dialogIsOk=$dialogIsOk");
            if (!dialogIsOk) {
                return
            }


            ApplicationManager.getApplication().runWriteAction {

                LOGGER.debug("processTransferableData runWriteAction")
                val startOffset = bounds.startOffset
                editor.document.replaceString(bounds.startOffset, bounds.endOffset, convertedToKotlinText)
                val endOffsetAfterCopy = startOffset + convertedToKotlinText.length
                editor.caretModel.moveToOffset(endOffsetAfterCopy)
                val codeStyleManager = com.intellij.psi.codeStyle.CodeStyleManager.getInstance(project)

                codeStyleManager.reformatText(targetPsiFile, startOffset, endOffsetAfterCopy)
                PsiDocumentManager.getInstance(targetPsiFile.project).commitDocument(editor.document)
            }

        }
    }

}
