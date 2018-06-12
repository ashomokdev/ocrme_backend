package ocrme_backend.servlets.ocr;

import ocrme_backend.file_builder.pdfbuilder.PdfBuilderInputData;

import javax.annotation.Nullable;

/**
 * Created by iuliia on 9/19/17.
 */
public class OcrData {

    private PdfBuilderInputData pdfBuilderInputData;
    private @Nullable String simpleText;
    private Status status;

    public enum Status {
        OK,
        TEXT_NOT_FOUND,
        INVALID_LANGUAGE_HINTS,
        UNKNOWN_ERROR
    }

    public OcrData(PdfBuilderInputData pdfBuilderInputData, String simpleText, Status status) {
        this.pdfBuilderInputData = pdfBuilderInputData;
        this.simpleText = simpleText;
        this.status = status;
    }

    public PdfBuilderInputData getPdfBuilderInputData() {
        return pdfBuilderInputData;
    }
    public @Nullable String getSimpleText() {
        return simpleText;
    }
    public Status getStatus() {
        return status;
    }
}
