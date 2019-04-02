package io.data2viz.kotlinx.htmlplugin.ide.view


import com.intellij.CommonBundle
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.uiDesigner.core.GridConstraints
import com.intellij.uiDesigner.core.GridLayoutManager
import com.intellij.uiDesigner.core.Spacer

import java.awt.Component
import java.awt.Insets
import javax.swing.Action
import javax.swing.JLabel
import javax.swing.JPanel

class KotlinPasteFromHtmlDialog(project: Project) : DialogWrapper(project, true) {


    companion object {

        fun isOK(project: Project): Boolean {
            val dialog = KotlinPasteFromHtmlDialog(project)
            dialog.show()

            return dialog.isOK
        }
    }

    private var myPanel: javax.swing.JPanel? = null
    private val buttonOK: javax.swing.JButton? = null

    init {


        setupUI()
        isModal = true
        rootPane.defaultButton = this.buttonOK
        title = "Convert Code From HTML"
        init()


    }

    override fun createCenterPanel(): javax.swing.JComponent? {
        return this.myPanel
    }

    override fun getContentPane(): java.awt.Container? {
        return this.myPanel
    }

    override fun createActions(): Array<Action> {
        setOKButtonText(CommonBundle.getYesButtonText())
        setCancelButtonText(CommonBundle.getNoButtonText())

        return arrayOf(okAction, cancelAction)
    }


    private fun setupUI() {
        val jPanel: JPanel
        jPanel = JPanel()
        this.myPanel = jPanel
        jPanel.layout = GridLayoutManager(2, 1, Insets(0, 0, 0, 0), -1, -1, false, false)
        val jLabel = JLabel()
        jLabel.text = "Clipboard content copied from HTML file. Do you want to convert it to Kotlin code?"
        jPanel.add(jLabel as Component, GridConstraints(0, 0, 1, 1, 8, 0, 0, 0, null, null, null))
        val spacer = Spacer()
        jPanel.add(spacer as Component, GridConstraints(1, 0, 1, 1, 0, 2, 1, 6, null, null, null))
    }

}

