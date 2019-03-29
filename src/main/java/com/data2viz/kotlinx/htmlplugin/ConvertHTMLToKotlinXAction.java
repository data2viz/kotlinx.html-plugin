package com.data2viz.kotlinx.htmlplugin;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;

public class ConvertHTMLToKotlinXAction extends AnAction {

    private static final ConvertHTMLToKotlinXActionKx actionKx = new ConvertHTMLToKotlinXActionKx();

    public ConvertHTMLToKotlinXAction() {
        super(ConvertHTMLToKotlinXActionKx.Companion.getName());
    }

    @Override
    public void actionPerformed(AnActionEvent e) {

        actionKx.actionPerformed(e);
    }

}
