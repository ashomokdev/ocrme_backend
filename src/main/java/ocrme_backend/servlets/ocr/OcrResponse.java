package ocrme_backend.servlets.ocr;

import java.io.Serializable;

/**
 * Created by iuliia on 6/27/17.
 */
public class OcrResponse implements Serializable {
    String textResult;
    String pdfResultUrl;

    public String getTextResult() {
        return textResult;
    }

    public void setTextResult(String textResult) {
        this.textResult = textResult;
    }

    public String getPdfResultUrl() {
        return pdfResultUrl;
    }

    public void setPdfResultUrl(String pdfResultUrl) {
        this.pdfResultUrl = pdfResultUrl;
    }

}
