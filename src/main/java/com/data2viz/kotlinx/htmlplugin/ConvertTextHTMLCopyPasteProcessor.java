package com.data2viz.kotlinx.htmlplugin;

import com.intellij.codeInsight.editorActions.CopyPastePostProcessor;
import com.intellij.codeInsight.editorActions.TextBlockTransferableData;
import com.intellij.lang.Language;
import com.intellij.lang.html.HTMLLanguage;
import com.intellij.openapi.application.Application;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.CaretStateTransferableData;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.RangeMarker;
import com.intellij.openapi.project.DumbService;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Ref;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiFileFactory;
import com.intellij.psi.PsiJavaFile;
import com.intellij.psi.impl.source.html.HtmlFileImpl;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.util.ArrayList;
import java.util.List;

import org.jetbrains.annotations.NotNull;

public class ConvertTextHTMLCopyPasteProcessor extends CopyPastePostProcessor<TextBlockTransferableData> {

    private static final ConvertTextHTMLCopyPasteProcessorKt processorKt = new ConvertTextHTMLCopyPasteProcessorKt();


    @NotNull
    @Override
    public List<TextBlockTransferableData> extractTransferableData(Transferable content) {
        return processorKt.extractTransferableData(content);

    }

    @NotNull
    @Override
    public List<TextBlockTransferableData> collectTransferableData(PsiFile file, Editor editor, int[] startOffsets, int[] endOffsets) {

        return processorKt.collectTransferableData(file, editor, startOffsets, endOffsets);

    }

    @Override
    public void processTransferableData(Project project, Editor editor, RangeMarker bounds, int caretOffset, Ref<Boolean> indented, List<TextBlockTransferableData> values) {

        processorKt.processTransferableData(project, editor, bounds, caretOffset, indented, values);


    }


}
