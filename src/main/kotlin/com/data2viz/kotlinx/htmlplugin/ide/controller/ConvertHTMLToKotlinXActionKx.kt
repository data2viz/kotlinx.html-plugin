package com.data2viz.kotlinx.htmlplugin.ide.controller

import com.data2viz.kotlinx.htmlplugin.conversion.model.HtmlPsiToHtmlDataConverter
import com.data2viz.kotlinx.htmlplugin.conversion.model.converToHtmlElements
import com.data2viz.kotlinx.htmlplugin.conversion.model.toKotlinX
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.psi.PsiFile
import com.intellij.psi.impl.source.html.HtmlFileImpl


open class ConvertHTMLToKotlinXActionKx : AnAction("Convert HTML To KotlinX") {


    override fun actionPerformed(e: AnActionEvent) {

        LOGGER.warn("actionPerformed")

        val editor = e.getRequiredData(CommonDataKeys.EDITOR);
        val project = e.project

        if(project == null) {
            return
        }

        val document = editor.document
        val selectionModel = editor.selectionModel
        val start = selectionModel.selectionStart
        val end = selectionModel.selectionEnd


        val text = selectionModel.selectedText;

        if(text.isNullOrEmpty()) {
            return
        }

        val sourcePsiFileFromText: PsiFile = HtmlPsiToHtmlDataConverter.createHtmlFileFromText(project, text)

        if (sourcePsiFileFromText !is HtmlFileImpl) {
            return
        }

        val htmlTags = sourcePsiFileFromText.converToHtmlElements()

        val convertedToKotlinText = htmlTags.toKotlinX()

        if(convertedToKotlinText.isNullOrEmpty()) {
            return
        }

        //Making the replacement
        WriteCommandAction.runWriteCommandAction(project) {
            document.replaceString(start, end, convertedToKotlinText)
        }
        selectionModel.removeSelection()
    }

    companion object {

        private val LOGGER = Logger.getInstance(ConvertHTMLToKotlinXActionKx::class.java)

    }

}
