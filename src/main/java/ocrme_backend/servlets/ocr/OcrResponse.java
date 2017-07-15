package ocrme_backend.servlets.ocr;

import java.io.Serializable;

/**
 * Created by iuliia on 6/27/17.
 */
public class OcrResponse implements Serializable {
    private String textResult;
    private String pdfResultUrl;
    private String errorMessage;
    private boolean containsError;

    public void setTextResult(String textResult) {
        this.textResult = textResult;
    }
    public void setPdfResultUrl(String pdfResultUrl) {
        this.pdfResultUrl = pdfResultUrl;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
        containsError = true;
    }

    public String getTextResult() {
        return textResult;
    }

    public String getPdfResultUrl() {
        return pdfResultUrl;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public boolean isContainsError() {
        return containsError;
    }



}
