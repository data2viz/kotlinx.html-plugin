package com.data2viz.kotlinx.htmlplugin

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
import java.util.ArrayList

class ConvertTextHTMLCopyPasteProcessorKt {
    fun collectTransferableData(file: PsiFile, editor: Editor, startOffsets: IntArray, endOffsets: IntArray): MutableList<TextBlockTransferableData> {
        LOGGER.warn("collectTransferableData");

        val resultList = ArrayList<TextBlockTransferableData>()

        if (file is HtmlFileImpl) {

            val fileName = file.name

            val fileText = file.text

            val htmlTextSelection = HtmlTextSelection(fileName, fileText, startOffsets, endOffsets, true)


            resultList.add(htmlTextSelection)
        }


        return resultList

    }

    fun extractTransferableData(content: Transferable): MutableList<TextBlockTransferableData>? {

        val result = ArrayList<TextBlockTransferableData>()

        try {
            val dataFlavor = ConvertTextHTMLCopyPasteProcessorKt.dataFlavor
            val isCopyFromHtmlFile = content.isDataFlavorSupported(dataFlavor)
            if (isCopyFromHtmlFile) {
                val `object` = content.getTransferData(dataFlavor) as TextBlockTransferableData


                result.add(`object`)

            } else {
                val isNotCopyPasteFromIDeaFile = !content.isDataFlavorSupported(CaretStateTransferableData.FLAVOR)
                val isSomeStringContent = content.isDataFlavorSupported(DataFlavor.stringFlavor)
                if (isNotCopyPasteFromIDeaFile && isSomeStringContent) {
                    val `object` = content.getTransferData(DataFlavor.stringFlavor)

                    val str = `object` as String
                    val external = HtmlTextSelection("external", str, intArrayOf(0), intArrayOf(str.length), false)
                    result.add(external)

                }
            }

        } catch (e: Throwable) {
            // empty catch block

        }

        return result
    }

    fun processTransferableData(project: Project, editor: Editor, bounds: RangeMarker, caretOffset: Int, indented: Ref<Boolean>, values: MutableList<TextBlockTransferableData>) {
        if (DumbService.getInstance(project).isDumb) {
            return
        }
        val targetFile = PsiDocumentManager.getInstance(project).getPsiFile(editor.document) ?: return

        if (values.size != 1) {
            throw RuntimeException("")
        }

        val value = values[0] as? HtmlTextSelection ?: return
        var psiFile: PsiFile? = PsiFileFactory.getInstance(project).createFileFromText(value.fileName, HTMLLanguage.INSTANCE, value.fileText)
        if (psiFile !is HtmlFileImpl) {
            psiFile = null
        }
        if (psiFile == null) {
            return
        }
        val sourceFile = psiFile as HtmlFileImpl
        if (!value.isFromHtmlFile() && !HtmlToKotlinConverterKt.looksLikeHtml(sourceFile)) {
            return
        }
        if (!KotlinPasteFromHtmlDialog.isOK(project)) {
            return
        }
        val text = this.convertCopiedCodeToKotlin(value, sourceFile)
        if (text != null && !text.isEmpty()) {
            val application = ApplicationManager.getApplication()

            application.runWriteAction(Run(bounds, editor, text, project, targetFile))
        }
    }


    private fun convertRangeToKotlin(file: HtmlFileImpl, range: TextRange, converter: HtmlToKotlinConverter): String {
        val result = StringBuilder()
        var currentRange = range
        val string = file.text

        while (!currentRange.isEmpty) {
            val leafElement = findFirstLeafElementWhollyInRange(file, currentRange)
            if (leafElement == null) {
                //                String unconvertedSuffix = StringsKt.substring((String) fileText, (int) currentRange.getStartOffset(), (int) currentRange.getEndOffset());
                val unconvertedSuffix = string.substring(currentRange.startOffset, currentRange.endOffset)

                result.append(unconvertedSuffix)
                break
            }
            val elementToConvert = findTopMostParentWhollyInRange(currentRange, leafElement)
            val unconvertedPrefix = substring(string as String, currentRange.startOffset, elementToConvert.textRange.startOffset)
            result.append(unconvertedPrefix)
            val converted = converter.convertElement(elementToConvert)
            if (isNotEmpty(converted)) {
                result.append(converted)
            } else {
                result.append(elementToConvert.text)
            }
            val endOfConverted = elementToConvert.textRange.endOffset
            currentRange = TextRange(endOfConverted, currentRange.endOffset)
        }

        return result.toString()
    }

    private fun substring(fileText: String, startOffset: Int, endOffest: Int): String {
        return fileText.substring(startOffset, endOffest)
    }

    private fun isNotEmpty(converted: String?): Boolean {
        return converted != null && !converted.isEmpty()
    }

    private fun findFirstLeafElementWhollyInRange(file: HtmlFileImpl, range: TextRange): PsiElement? {
        var i = range.startOffset
        while (i < range.endOffset) {
            val element = file.findElementAt(i)
            if (element == null) {
                ++i
                continue
            }
            val elemRange = element.textRange
            if (!range.contains(elemRange)) {
                i = elemRange.endOffset
                continue
            }
            return element
        }
        return null
    }

    private fun findTopMostParentWhollyInRange(range: TextRange, base: PsiElement): PsiElement {


        var needContinue = true
        do {

            val parent = base.parent
            if (parent is PsiJavaFile) {
                needContinue = range.contains(parent.getTextRange())
            }

        } while (needContinue)


        return base
    }


    private fun convertCopiedCodeToKotlin(code: HtmlTextSelection, fileCopiedFrom: HtmlFileImpl): String {

        val n: Int
        val converter = HtmlToKotlinConverter()
        val startOffsets = code.startOffsets
        val endOffsets = code.endOffsets

        val result = StringBuilder()

        val startIndex = 0
        val lastIndex = startOffsets.size - 1

        val lastIndexMessage = startOffsets[lastIndex]

        val message = startOffsets[startIndex]
        var i = 0
        n = lastIndexMessage
        if (message <= n) {
            // ???
            do {
                val startOffset = startOffsets[++i]
                val endOffset = endOffsets[i]
                result.append(this.convertRangeToKotlin(fileCopiedFrom, TextRange(startOffset, endOffset), converter))
            } while (i != n)
        }

        return StringUtil.convertLineSeparators(result.toString())
    }

    inner class Run(internal val `$bounds`: RangeMarker, internal val `$editor`: Editor, internal val `$text`: String, internal val `$project`: Project, internal val `$targetFile`: PsiFile) : Runnable {

        override fun run() {
            val startOffset = `$bounds`.startOffset
            `$editor`.document.replaceString(`$bounds`.startOffset, `$bounds`.endOffset, `$text` as CharSequence)
            val endOffsetAfterCopy = startOffset + `$text`.length
            `$editor`.caretModel.moveToOffset(endOffsetAfterCopy)
            val codeStyleManager = com.intellij.psi.codeStyle.CodeStyleManager.getInstance(`$project`)

            codeStyleManager.reformatText(`$targetFile`, startOffset, endOffsetAfterCopy)
            PsiDocumentManager.getInstance(`$targetFile`.project).commitDocument(`$editor`.document)
        }

    }

    companion object {
        var dataFlavor: DataFlavor = DataFlavor(ConvertTextHTMLCopyPasteProcessor::class.java, "class: com.data2viz.kotlinx.htmlplugin.ConvertTextHTMLCopyPasteProcessor");
        private val LOGGER = Logger.getInstance(ConvertTextHTMLCopyPasteProcessorKt::class.java)
    }
}
