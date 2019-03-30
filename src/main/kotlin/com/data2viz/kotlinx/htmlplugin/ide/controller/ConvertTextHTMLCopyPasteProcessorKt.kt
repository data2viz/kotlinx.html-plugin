package com.data2viz.kotlinx.htmlplugin.ide.controller

import com.data2viz.kotlinx.htmlplugin.conversion.model.HtmlPsiToHtmlDataConverter
import com.data2viz.kotlinx.htmlplugin.conversion.model.toKotlinX
import com.data2viz.kotlinx.htmlplugin.ide.data.ExternalFileHtmlTextTransferableData
import com.data2viz.kotlinx.htmlplugin.ide.data.HtmlTextTransferableData
import com.data2viz.kotlinx.htmlplugin.ide.view.KotlinPasteFromHtmlDialog
import com.intellij.codeInsight.editorActions.TextBlockTransferableData
import com.intellij.lang.html.HTMLLanguage
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.editor.CaretStateTransferableData
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.RangeMarker
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Ref
import com.intellij.openapi.util.TextRange
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.DumbService
import com.intellij.openapi.util.text.StringUtil
import com.intellij.psi.*
import com.intellij.psi.impl.source.html.HtmlFileImpl

import java.awt.datatransfer.DataFlavor
import java.awt.datatransfer.Transferable


class ConvertTextHTMLCopyPasteProcessorKt {


    companion object {

        private val LOGGER = Logger.getInstance(ConvertTextHTMLCopyPasteProcessorKt::class.java)
    }

    fun collectTransferableData(file: PsiFile, editor: Editor, startOffsets: IntArray, endOffsets: IntArray): MutableList<TextBlockTransferableData> {


        val resultList = mutableListOf<TextBlockTransferableData>()

        val isHtmlFile = file is HtmlFileImpl

        LOGGER.warn("collectTransferableData isHtmlFile=$isHtmlFile");
        if (isHtmlFile) {
            val htmlTextSelection = HtmlTextTransferableData(file.name, file.text, startOffsets, endOffsets, true)
            resultList.add(htmlTextSelection)
        }

        return resultList

    }

    fun extractTransferableData(content: Transferable): MutableList<TextBlockTransferableData>? {


        val result = mutableListOf<TextBlockTransferableData>()

        val dataFlavor = HtmlTextTransferableData.dataFlavor
        val isCopyFromHtmlFile = content.isDataFlavorSupported(dataFlavor)

        LOGGER.warn("extractTransferableData isCopyFromHtmlFile=$isCopyFromHtmlFile")
        if (isCopyFromHtmlFile) {
            val transferableData = content.getTransferData(dataFlavor) as TextBlockTransferableData
            result.add(transferableData)

        } else {

            val isNotCopyPasteFromIdeaFile = !content.isDataFlavorSupported(CaretStateTransferableData.FLAVOR)
            val isSomeStringContent = content.isDataFlavorSupported(DataFlavor.stringFlavor)

            LOGGER.warn("extractTransferableData isNotCopyPasteFromIdeaFile=$isNotCopyPasteFromIdeaFile");
            LOGGER.warn("extractTransferableData isSomeStringContent=$isSomeStringContent");
            if (isNotCopyPasteFromIdeaFile && isSomeStringContent) {
                val transferableData = content.getTransferData(DataFlavor.stringFlavor)

                val str = transferableData as String

                val external = ExternalFileHtmlTextTransferableData(str)
                result.add(external)

            }
        }

        LOGGER.warn("extractTransferableData result.size=${result.size}");
        return result
    }

    fun processTransferableData(project: Project, editor: Editor, bounds: RangeMarker, caretOffset: Int, indented: Ref<Boolean>, textValues: MutableList<TextBlockTransferableData>) {

        val isDump = DumbService.getInstance(project).isDumb
        LOGGER.warn("processTransferableData isDump=$isDump");
        if (isDump) {
            return
        }


        val psiDocumentManager = PsiDocumentManager.getInstance(project)
        val targetPsiFile = psiDocumentManager.getPsiFile(editor.document)
        LOGGER.warn("processTransferableData targetPsiFile=$targetPsiFile");

        if (targetPsiFile == null) {
            return
        }


        val textValuesSize = textValues.size

        LOGGER.warn("processTransferableData textValuesSize=$textValuesSize");
        if (textValuesSize != 1) {
            return
        }
        val textBlockTransferableData = textValues[0]

        LOGGER.warn("processTransferableData textBlockTransferableData=$textBlockTransferableData");

        if (textBlockTransferableData !is HtmlTextTransferableData) {
            return
        }
        val htmlTextTransferableData = textBlockTransferableData


        val psiFileFactory = PsiFileFactory.getInstance(project)
        val sourcePsiFileFromText: PsiFile = psiFileFactory.createFileFromText(htmlTextTransferableData.fileName, HTMLLanguage.INSTANCE, htmlTextTransferableData.fileText)
        LOGGER.warn("processTransferableData sourcePsiFileFromText=$sourcePsiFileFromText")
        if (sourcePsiFileFromText !is HtmlFileImpl) {
            return
        }

        val isFromHtmlFile = htmlTextTransferableData.isFromHtmlFile
        LOGGER.warn("processTransferableData isFromHtmlFile=$isFromHtmlFile")

        if (!isFromHtmlFile) {
            val looksLikeHtml = HtmlPsiToHtmlDataConverter.looksLikeHtml(sourcePsiFileFromText)
            LOGGER.warn("processTransferableData looksLikeHtml=$looksLikeHtml")
            if (!looksLikeHtml) {
                return
            }
        }


        val dialogIsOk = KotlinPasteFromHtmlDialog.isOK(project)
        LOGGER.warn("processTransferableData dialogIsOk=$dialogIsOk");
        if (!dialogIsOk) {
            return
        }
        val convertedToKotlinText = convertCopiedCodeToKotlin(htmlTextTransferableData, sourcePsiFileFromText)

        val notEmpty = convertedToKotlinText.isNotEmpty()
        LOGGER.warn("processTransferableData notEmpty = $notEmpty  convertedToKotlinText=$convertedToKotlinText");
        if (notEmpty) {

            ApplicationManager.getApplication().runWriteAction {

                LOGGER.warn("processTransferableData runWriteAction")
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

    public fun convertCopiedCodeToKotlin(htmlTextTransferableData: HtmlTextTransferableData, sourceFile: HtmlFileImpl): String {

        val sb = StringBuilder()

        val startOffsets = htmlTextTransferableData.startOffsets
        val endOffsets = htmlTextTransferableData.endOffsets

        LOGGER.warn("convertCopiedCodeToKotlin startOffsets.size = ${startOffsets.size} endOffsets.size = ${endOffsets.size}")
        if (startOffsets.size != endOffsets.size) {
            throw AssertionError("startOffsets & endOffsets should be same size")
        }


        LOGGER.warn("convertCopiedCodeToKotlin startOffsets.first()= ${startOffsets.first()} startOffsets.last() = ${startOffsets.last()}")
        if (startOffsets.first() < startOffsets.last()) {
            throw  AssertionError("start offset first should be lower than last")
        }

        for (i in startOffsets.indices) {
            val startOffset = startOffsets[i]
            val endOffset = endOffsets[i]
            val textRange = TextRange(startOffset, endOffset)
            val convertRangeToKotlin = convertRangeToKotlin(sourceFile, textRange)
            sb.append(convertRangeToKotlin)
        }

        val sbResult = sb.toString()
        val result = StringUtil.convertLineSeparators(sbResult)
        LOGGER.warn("convertCopiedCodeToKotlin result=$result")
        return result
    }


    private fun convertRangeToKotlin(file: HtmlFileImpl, range: TextRange): String {
        val result = StringBuilder()
        var currentRange = range
        val string = file.text

        LOGGER.warn("convertRangeToKotlin currentRange.isEmpty=${currentRange.isEmpty}")

        while (!currentRange.isEmpty) {
            LOGGER.warn("convertRangeToKotlin currentRange.isEmpty=${currentRange.isEmpty}")
            val leafElement = findFirstLeafElementWhollyInRange(file, currentRange)

            LOGGER.warn("convertRangeToKotlin leafElement=${leafElement}")
            if (leafElement == null) {

                val unconvertedSuffix = string.substring(currentRange.startOffset, currentRange.endOffset)

                result.append(unconvertedSuffix)
                break
            }
            val elementToConvert = findTopMostParentWhollyInRange(currentRange, leafElement)
            val unconvertedPrefix = string.substring(currentRange.startOffset, elementToConvert!!.textRange.startOffset)
            result.append(unconvertedPrefix)
            val converted = elementToConvert.toKotlinX()
            if (converted.isNullOrEmpty()) {
                result.append(converted)
            } else {
                result.append(elementToConvert.text)
            }
            val endOfConverted = elementToConvert.textRange.endOffset
            currentRange = TextRange(endOfConverted, currentRange.endOffset)
        }

        return result.toString()
    }


    private fun findFirstLeafElementWhollyInRange(file: HtmlFileImpl, range: TextRange): PsiElement? {

        var result: PsiElement? = null


        val startOffset = range.startOffset
        val endOffset = range.endOffset

        LOGGER.warn("findFirstLeafElementWhollyInRange startOffset=${startOffset} endOffset=${endOffset}")
        var currentOffset = range.startOffset
        while (startOffset < endOffset) {
            LOGGER.warn("findFirstLeafElementWhollyInRange currentOffset=${currentOffset}")

            val element = file.findElementAt(currentOffset)
            if (element == null) {
                currentOffset++
                continue
            }

            val elemRange = element.textRange
            if (!range.contains(elemRange)) {
                currentOffset = elemRange.endOffset
                continue
            }

            result = element
            break
        }
        return result
    }

    private fun findTopMostParentWhollyInRange(range: TextRange, base: PsiElement): PsiElement? {


        if (!range.contains(base.textRange)) {

            throw AssertionError("Base element out of range. Range: " + range + ", element: " + base.text + ", element's range: " + base.textRange + ".")
        }


        var current = base
        var result: PsiElement? = current


        while (current.parent != null) {
            if (current is PsiJavaFile) {
                val fileTextRange = current.getTextRange()
                val contains = range.contains(fileTextRange)
                if (contains) {
                    result = current
                    break
                }
            }
            current = current.parent
        }

        LOGGER.warn("findTopMostParentWhollyInRange result=$result")

        return result
    }


}
