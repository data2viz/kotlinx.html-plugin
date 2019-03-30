package com.data2viz.kotlinx.htmlplugin.ide.controller

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.ui.Messages

open class ConvertHTMLToKotlinXActionKx: AnAction("Convert HTML To KotlinX") {


    override fun actionPerformed(e: AnActionEvent) {

        LOGGER.warn("actionPerformed")
        Messages.showMessageDialog(e.project, "Hello world!", "Greeting", Messages.getInformationIcon())
    }

    companion object {

        private val LOGGER = Logger.getInstance(ConvertHTMLToKotlinXActionKx::class.java)

    }

}
