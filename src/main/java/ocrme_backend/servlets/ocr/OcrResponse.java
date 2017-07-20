package ocrme_backend.servlets.ocr;

import java.io.Serializable;

/**
 * Created by iuliia on 6/27/17.
 */
public class OcrResponse implements Serializable {

    private String textResult;
    private String pdfResultUrl;



    private Status status;

    public enum Status {
        OK,
        PDF_CAN_NOT_BE_CREATED_LANGUAGE_NOT_SUPPORTED,
        TEXT_NOT_FOUND,
        UNKNOWN_ERROR
    }


    public void setTextResult(String textResult) {
        this.textResult = textResult;
    }
    public void setPdfResultUrl(String pdfResultUrl) {
        this.pdfResultUrl = pdfResultUrl;
    }
    public void setStatus(Status status) {
        this.status = status;
    }

    public String getTextResult() {
        return textResult;
    }
    public String getPdfResultUrl() {
        return pdfResultUrl;
    }
    public Status getStatus() {
        return status;
    }
}
