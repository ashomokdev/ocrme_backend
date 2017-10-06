package ocrme_backend.servlets.ocr;

import java.io.Serializable;

/**
 * Created by iuliia on 6/27/17.
 */
public class OcrResponse implements Serializable {

    private String textResult;
    private String pdfResultGsUrl;
    private String pdfResultMediaUrl;
    private Status status;

    public enum Status {
        OK,
        PDF_CAN_NOT_BE_CREATED_LANGUAGE_NOT_SUPPORTED,
        TEXT_NOT_FOUND,
        INVALID_LANGUAGE_HINTS,
        UNKNOWN_ERROR
    }

    public void setTextResult(String textResult) {
        this.textResult = textResult;
    }
    public void setPdfResultGsUrl(String pdfResultGsUrl) {
        this.pdfResultGsUrl = pdfResultGsUrl;
    }
    public void setStatus(Status status) {
        this.status = status;
    }

    public String getTextResult() {
        return textResult;
    }
    public String getPdfResultGsUrl() {
        return pdfResultGsUrl;
    }
    public Status getStatus() {
        return status;
    }
    public String getPdfResultMediaUrl() {
        return pdfResultMediaUrl;
    }
    public void setPdfResultMediaUrl(String pdfResultMediaUrl) {
        this.pdfResultMediaUrl = pdfResultMediaUrl;
    }


    @Override
    public String toString() {
        return "OcrResponse{" +
                "textResult='" + textResult + '\'' +
                ", pdfResultGsUrl='" + pdfResultGsUrl + '\'' +
                ", pdfResultMediaUrl='" + pdfResultMediaUrl + '\'' +
                ", status=" + status +
                '}';
    }

}
