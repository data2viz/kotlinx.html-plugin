package com.data2viz.kotlinx.htmlplugin.ide.controller;

import com.data2viz.kotlinx.htmlplugin.ide.controller.ConvertTextHTMLCopyPasteProcessorKt;
import com.intellij.codeInsight.editorActions.CopyPastePostProcessor;
import com.intellij.codeInsight.editorActions.TextBlockTransferableData;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.RangeMarker;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Ref;
import com.intellij.psi.PsiFile;

import java.awt.datatransfer.Transferable;
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
