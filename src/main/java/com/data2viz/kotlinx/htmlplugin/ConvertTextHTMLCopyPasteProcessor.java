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
    public List<TextBlockTransferableData> extractTransferableData(Transferable content) {
        return processorKt.extractTransferableData(content);

    }

    @NotNull
    public List<TextBlockTransferableData> collectTransferableData(PsiFile file, Editor editor, int[] startOffsets, int[] endOffsets) {

        return processorKt.collectTransferableData(file, editor, startOffsets, endOffsets);

    }

    public void processTransferableData(Project project, Editor editor, RangeMarker bounds, int caretOffset, Ref<Boolean> indented, List<TextBlockTransferableData> values) {

        processorKt.processTransferableData(project, editor, bounds, caretOffset, indented, values);

        if (DumbService.getInstance(project).isDumb()) {
            return;
        }
        PsiFile targetFile = PsiDocumentManager.getInstance(project).getPsiFile(editor.getDocument());
        if (targetFile == null) {
            return;
        }
//        if (Intrinsics.areEqual((Object) "jet", (Object) targetFile.getLanguage().getID()) ^ true) {
//            return;
//        }
        if (values.size() != 1) {
            throw new RuntimeException("");
        }

//        PreconditionsKt. assert ((boolean) (values.size() == 1));
        TextBlockTransferableData value = values.get(0);
        if (!(value instanceof HtmlTextSelection)) {
            return;
        }
        PsiFile psiFile = PsiFileFactory.getInstance((Project) project).createFileFromText(((HtmlTextSelection) value).getFileName(), (Language) HTMLLanguage.INSTANCE, (CharSequence) ((HtmlTextSelection) value).getFileText());
        if (!(psiFile instanceof HtmlFileImpl)) {
            psiFile = null;
        }
        if ((HtmlFileImpl) psiFile == null) {
            return;
        }
        HtmlFileImpl sourceFile = (HtmlFileImpl) psiFile;
        if (!((HtmlTextSelection) value).isFromHtmlFile() && !HtmlToKotlinConverterKt.INSTANCE.looksLikeHtml((PsiFile) sourceFile)) {
            return;
        }
        if (!KotlinPasteFromHtmlDialog.Companion.isOK(project)) {
            return;
        }
        String text = this.convertCopiedCodeToKotlin((HtmlTextSelection) value, sourceFile);
        if (text != null && !text.isEmpty()) {
            Application application = ApplicationManager.getApplication();

            application.runWriteAction(new Run(bounds, editor, text, project, targetFile));
        }
    }

    /*
     * WARNING - void declaration
     */
    private final String convertCopiedCodeToKotlin(HtmlTextSelection code, HtmlFileImpl fileCopiedFrom) {
//        void value;
        int n;
        HtmlToKotlinConverter converter = new HtmlToKotlinConverter();
        int[] startOffsets = code.getStartOffsets();
        int[] endOffsets = code.getEndOffsets();
//        boolean bl = startOffsets.length == (endOffsets = code.getEndOffsets()).length;
//        if (PreconditionsKt.getASSERTIONS_ENABLED() && value == false) {
//            String message = "Must have the same size";
//            throw (Throwable) ((Object) new AssertionError((Object) message));
//        }
        StringBuilder result = new StringBuilder();
//        IntRange intRange = ArraysKt.getIndices((int[]) startOffsets);

        int startIndex = 0;
        int lastIndex = startOffsets.length - 1;

        int lastIndexMessage = startOffsets[lastIndex];

        int message = startOffsets[startIndex];
        int i = 0;
        if (message <= (n = lastIndexMessage)) {
            // ???
            do {
                int startOffset = startOffsets[++i];
                int endOffset = endOffsets[i];
                result.append(this.convertRangeToKotlin(fileCopiedFrom, new TextRange(startOffset, endOffset), converter));
            } while (i != n);
        }
        String string = StringUtil.convertLineSeparators(result.toString());
//        Intrinsics.checkExpressionValueIsNotNull((Object) string, (String) "StringUtil.convertLineSe\u2026rators(result.toString())");
        return string;
    }

    private final String convertRangeToKotlin(HtmlFileImpl file, TextRange range, HtmlToKotlinConverter converter) {
        StringBuilder result = new StringBuilder();
        TextRange currentRange = range;
        String string = file.getText();

        String fileText = string;
        while (!currentRange.isEmpty()) {
            PsiElement leafElement = this.findFirstLeafElementWhollyInRange(file, currentRange);
            if (leafElement == null) {
//                String unconvertedSuffix = StringsKt.substring((String) fileText, (int) currentRange.getStartOffset(), (int) currentRange.getEndOffset());
                String unconvertedSuffix = fileText.substring((int) currentRange.getStartOffset(), (int) currentRange.getEndOffset());

                result.append(unconvertedSuffix);
                break;
            }
            PsiElement elementToConvert = this.findTopMostParentWhollyInRange(currentRange, leafElement);
            String unconvertedPrefix = substring((String) fileText, (int) currentRange.getStartOffset(), (int) elementToConvert.getTextRange().getStartOffset());
            result.append(unconvertedPrefix);
            String converted = converter.convertElement(elementToConvert);
            if (isNotEmpty((String) converted)) {
                result.append(converted);
            } else {
                result.append(elementToConvert.getText());
            }
            int endOfConverted = elementToConvert.getTextRange().getEndOffset();
            currentRange = new TextRange(endOfConverted, currentRange.getEndOffset());
        }
        String string2 = result.toString();

        return string2;
    }

    private String substring(String fileText, int startOffset, int endOffest) {
        return fileText.substring(startOffset, endOffest);
    }

    private boolean isNotEmpty(String converted) {
        return converted != null && !converted.isEmpty();
    }

    private final PsiElement findFirstLeafElementWhollyInRange(HtmlFileImpl file, TextRange range) {
        int i = range.getStartOffset();
        while (i < range.getEndOffset()) {
            PsiElement element = file.findElementAt(i);
            if (element == null) {
                ++i;
                continue;
            }
            TextRange elemRange = element.getTextRange();
            if (!range.contains(elemRange)) {
                i = elemRange.getEndOffset();
                continue;
            }
            return element;
        }
        return null;
    }

    private final PsiElement findTopMostParentWhollyInRange(TextRange range, PsiElement base) {
        PsiElement parent;
//        boolean value = range.contains(base.getTextRange());
//        if (PreconditionsKt.getASSERTIONS_ENABLED() && !value) {
//            String message = "Base element out of range. Range: " + (Object) range + ", element: " + base.getText() + ", element's range: " + (Object) base.getTextRange() + ".";
//            throw (Throwable) ((Object) new AssertionError((Object) message));
//        }
        PsiElement elem = base;
        while ((parent = elem.getParent()) != null && !(parent instanceof PsiJavaFile) && !(range.contains(parent.getTextRange()) ^ true)) {
            elem = parent;
        }
        return elem;
    }


    public class Run implements Runnable {


        final /* synthetic */ RangeMarker $bounds;
        final /* synthetic */ Editor $editor;
        final /* synthetic */ String $text;
        final /* synthetic */ Project $project;
        final /* synthetic */ PsiFile $targetFile;


        public Run(RangeMarker $bounds, Editor $editor, String $text, Project $project, PsiFile $targetFile) {
            this.$bounds = $bounds;
            this.$editor = $editor;
            this.$text = $text;
            this.$project = $project;
            this.$targetFile = $targetFile;
        }

        public final void run() {
            int startOffset = this.$bounds.getStartOffset();
            this.$editor.getDocument().replaceString(this.$bounds.getStartOffset(), this.$bounds.getEndOffset(), (CharSequence) this.$text);
            int endOffsetAfterCopy = startOffset + this.$text.length();
            this.$editor.getCaretModel().moveToOffset(endOffsetAfterCopy);
            com.intellij.psi.codeStyle.CodeStyleManager codeStyleManager = com.intellij.psi.codeStyle.CodeStyleManager.getInstance((Project) this.$project);

            codeStyleManager.reformatText(this.$targetFile, startOffset, endOffsetAfterCopy);
            PsiDocumentManager.getInstance((Project) this.$targetFile.getProject()).commitDocument(this.$editor.getDocument());
        }

    }
}
