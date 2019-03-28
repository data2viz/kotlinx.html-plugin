package com.data2viz.kotlinx.htmlplugin;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.ui.Messages;

public class ConvertHTMLToKotlinXAction extends AnAction {

    public ConvertHTMLToKotlinXAction() {
        super("Hello");
    }
    @Override
    public void actionPerformed(AnActionEvent e) {

        Messages.showMessageDialog(e.getProject(), "Hello world!", "Greeting", Messages.getInformationIcon());
    }

    @Override
    public void update(AnActionEvent e) {
        super.update(e);
        e.getPresentation().setIcon(AllIcons.Ide.Info_notifications);
    }

}
