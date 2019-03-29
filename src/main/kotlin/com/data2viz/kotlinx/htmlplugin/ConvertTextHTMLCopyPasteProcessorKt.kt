package com.data2viz.kotlinx.htmlplugin

import com.intellij.codeInsight.editorActions.TextBlockTransferableData
import com.intellij.lang.Language
import com.intellij.lang.html.HTMLLanguage
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.editor.CaretStateTransferableData
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.RangeMarker
import com.intellij.openapi.project.DumbService
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Ref
import com.intellij.openapi.util.TextRange
import com.intellij.openapi.util.text.StringUtil
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiFileFactory
import com.intellij.psi.PsiJavaFile
import com.intellij.openapi.diagnostic.Logger
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
//        var parent: PsiElement
//        //        boolean value = range.contains(base.getTextRange());
//        //        if (PreconditionsKt.getASSERTIONS_ENABLED() && !value) {
//        //            String message = "Base element out of range. Range: " + (Object) range + ", element: " + base.getText() + ", element's range: " + (Object) base.getTextRange() + ".";
//        //            throw (Throwable) ((Object) new AssertionError((Object) message));
//        //        }
        var elem = base
//        while ((parent = elem.parent) != null && parent !is PsiJavaFile && !(range.contains(parent.textRange) xor true)) {
//            elem = parent
//        }
        return elem
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
