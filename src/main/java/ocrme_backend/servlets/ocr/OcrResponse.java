package ocrme_backend.servlets.ocr;

import java.io.Serializable;

/**
 * Created by iuliia on 6/27/17.
 */
public class OcrResponse implements Serializable {

    private Status status;
    private OcrResult ocrResult;

    @Override
    public String toString() {
        return "OcrResponse{" +
                "status=" + status +
                ", ocrResult=" + ocrResult +
                '}';
    }

    public enum Status {
        OK,
        PDF_CAN_NOT_BE_CREATED_LANGUAGE_NOT_SUPPORTED,
        TEXT_NOT_FOUND,
        INVALID_LANGUAGE_HINTS,
        UNKNOWN_ERROR
    }


    public OcrResult getOcrResult() {
        return ocrResult;
    }
    public void setOcrResult(OcrResult ocrResult) {
        this.ocrResult = ocrResult;
    }
    public void setStatus(Status status) {
        this.status = status;
    }
    public Status getStatus() {
        return status;
    }


}
