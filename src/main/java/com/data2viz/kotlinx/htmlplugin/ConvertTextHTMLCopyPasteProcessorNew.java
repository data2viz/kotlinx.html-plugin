package com.data2viz.kotlinx.htmlplugin;

import com.intellij.codeInsight.editorActions.CopyPastePostProcessor;
import com.intellij.codeInsight.editorActions.TextBlockTransferableData;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.RangeMarker;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Ref;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.NotNull;

import java.awt.datatransfer.Transferable;
import java.util.List;

public class ConvertTextHTMLCopyPasteProcessorNew extends CopyPastePostProcessor<TextBlockTransferableData> {

    private static final ConvertTextHTMLCopyPasteProcessorKt processorKt = new ConvertTextHTMLCopyPasteProcessorKt();


    @NotNull
    public List<TextBlockTransferableData> extractTransferableData(Transferable content) {
        return processorKt.extractTransferableData(content);

    }

    @NotNull
    public List<TextBlockTransferableData> collectTransferableData(PsiFile file, Editor editor, int[] startOffsets, int[] endOffsets) {

        return processorKt.collectTransferableData(file, editor, startOffsets, endOffsets);

    }

    public void processTransferableData(Project project, Editor editor, RangeMarker bounds, int caretOffset, Ref<Boolean> indented, List<TextBlockTransferableData> values) {

        processorKt.processTransferableData(project, editor, bounds, caretOffset, indented, values);

    }

}
