package io.data2viz.kotlinx.htmlplugin.ide

import com.intellij.codeInsight.editorActions.CopyPastePostProcessor
import com.intellij.codeInsight.editorActions.TextBlockTransferableData
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.diagnostic.debug
import com.intellij.openapi.editor.CaretStateTransferableData
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.RangeMarker
import com.intellij.openapi.project.DumbService
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Ref
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiFile
import com.intellij.psi.impl.source.html.HtmlFileImpl
import io.data2viz.kotlinx.htmlplugin.conversion.HtmlPsiToHtmlDataConverter
import io.data2viz.kotlinx.htmlplugin.conversion.converToHtmlElements
import io.data2viz.kotlinx.htmlplugin.conversion.toKotlinx
import java.awt.datatransfer.DataFlavor
import java.awt.datatransfer.Transferable

val logger = Logger.getInstance(ConvertTextHTMLCopyPasteProcessor::class.java)


inline fun Logger.debug (lazyMessage: () -> String ) {
    if (isDebugEnabled) {
        debug(lazyMessage())
    }
}


class ConvertTextHTMLCopyPasteProcessor : CopyPastePostProcessor<TextBlockTransferableData>() {

    override fun collectTransferableData(file: PsiFile, editor: Editor, startOffsets: IntArray, endOffsets: IntArray): List<TextBlockTransferableData> =
            when (file) {
                is HtmlFileImpl -> listOf(HtmlTextTransferableData(file.name, file.text, startOffsets, endOffsets, true))
                else -> listOf()
            }

    override fun extractTransferableData(content: Transferable): List<TextBlockTransferableData> {

        val result = mutableListOf<TextBlockTransferableData>()
        val dataFlavor = htmlDataFlavor
        val isCopyFromHtmlFile = content.isDataFlavorSupported(dataFlavor)

        logger.debug {"extractTransferableData isCopyFromHtmlFile=$isCopyFromHtmlFile" }
        if (isCopyFromHtmlFile) {
            val transferableData = content.getTransferData(dataFlavor) as TextBlockTransferableData
            result.add(transferableData)

        } else {

            val isNotCopyPasteFromIdeaFile = !content.isDataFlavorSupported(CaretStateTransferableData.FLAVOR)
            val isSomeStringContent = content.isDataFlavorSupported(DataFlavor.stringFlavor)

            logger.debug { "extractTransferableData isNotCopyPasteFromIdeaFile=$isNotCopyPasteFromIdeaFile" }
            logger.debug { "extractTransferableData isSomeStringContent=$isSomeStringContent"}
            if (isNotCopyPasteFromIdeaFile && isSomeStringContent) {
                val transferableData = content.getTransferData(DataFlavor.stringFlavor)

                val str = transferableData as String

                val external = ExternalFileHtmlTextTransferableData(str)
                result.add(external)
            }
        }

        logger.debug {"extractTransferableData result.size=${result.size}"}
        return result
    }

    override fun processTransferableData(project: Project, editor: Editor,
                                         bounds: RangeMarker,
                                         caretOffset: Int,
                                         indented: Ref<Boolean>,
                                         textValues: MutableList<TextBlockTransferableData>) {

        val isDump = DumbService.getInstance(project).isDumb
        logger.debug { "processTransferableData isDump=$isDump" }
        if (isDump) {
            return
        }


        val psiDocumentManager = PsiDocumentManager.getInstance(project)
        val targetPsiFile = psiDocumentManager.getPsiFile(editor.document)
        logger.debug { "processTransferableData targetPsiFile=$targetPsiFile" }

        if (targetPsiFile == null) {
            return
        }

        val name = targetPsiFile.name
        if (!name.endsWith(".kt")) {
            return
        }



        val textValuesSize = textValues.size

        logger.debug { "processTransferableData textValuesSize=$textValuesSize" }
        if (textValuesSize != 1) {
            return
        }
        val textBlockTransferableData = textValues[0]

        logger.debug { "processTransferableData textBlockTransferableData=$textBlockTransferableData" }

        if (textBlockTransferableData !is HtmlTextTransferableData) {
            return
        }
        val htmlTextTransferableData = textBlockTransferableData


        val fileText = htmlTextTransferableData.fileText
        val fileName = htmlTextTransferableData.fileName

        val selectedText = if (
                htmlTextTransferableData.startOffsets.isNotEmpty() &&
                htmlTextTransferableData.endOffsets.isNotEmpty()) {
            fileText.subSequence(
                    htmlTextTransferableData.startOffsets[0],
                    htmlTextTransferableData.endOffsets[0])
                    .toString()
        } else {
            fileText
        }


        val sourcePsiFileFromText: PsiFile = HtmlPsiToHtmlDataConverter.createHtmlFileFromText(project, fileName, selectedText)

        logger.debug { "processTransferableData sourcePsiFileFromText=$sourcePsiFileFromText" }
        if (sourcePsiFileFromText !is HtmlFileImpl) {
            return
        }

        val isFromHtmlFile = htmlTextTransferableData.isFromHtmlFile
        logger.debug { "processTransferableData isFromHtmlFile=$isFromHtmlFile" }

        if (!isFromHtmlFile) {
            val looksLikeHtml = HtmlPsiToHtmlDataConverter.isLooksLikeHtml(sourcePsiFileFromText)
            logger.debug { "processTransferableData isLooksLikeHtml=$looksLikeHtml" }
            if (!looksLikeHtml) {
                return
            }
        }


        val htmlElements = sourcePsiFileFromText.converToHtmlElements()

        val convertedToKotlinText = htmlElements.toKotlinx()


        val notEmpty = convertedToKotlinText.isNotEmpty()
        logger.debug { "processTransferableData notEmpty = $notEmpty  convertedToKotlinText=$convertedToKotlinText" }
        if (notEmpty) {


            val dialogIsOk = KotlinPasteFromHtmlDialog.isOK(project)
            logger.debug { "processTransferableData dialogIsOk=$dialogIsOk" }
            if (!dialogIsOk) {
                return
            }

            ApplicationManager.getApplication().runWriteAction {

                logger.debug { "processTransferableData runWriteAction" }
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
