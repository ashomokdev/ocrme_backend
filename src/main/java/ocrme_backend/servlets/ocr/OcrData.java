package ocrme_backend.servlets.ocr;

import ocrme_backend.file_builder.pdfbuilder.PdfBuilderInputData;

import javax.annotation.Nullable;

/**
 * Created by iuliia on 9/19/17.
 */
public class OcrData {
    private PdfBuilderInputData pdfBuilderInputData;
    private @Nullable String simpleText;

    public OcrData(PdfBuilderInputData pdfBuilderInputData, String simpleText) {
        this.pdfBuilderInputData = pdfBuilderInputData;
        this.simpleText = simpleText;
    }

    public PdfBuilderInputData getPdfBuilderInputData() {
        return pdfBuilderInputData;
    }
    public @Nullable String getSimpleText() {
        return simpleText;
    }
}
