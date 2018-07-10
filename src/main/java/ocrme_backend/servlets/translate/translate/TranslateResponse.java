package ocrme_backend.servlets.translate.translate;

import java.io.Serializable;

/**
 * Created by iuliia on 8/31/17.
 */
public class TranslateResponse implements Serializable {
    private Status status;
    private TranslateResult translateResult;

    public TranslateResult getTranslateResult() {
        return translateResult;
    }

    public void setTranslateResult(TranslateResult translateResult) {
        this.translateResult = translateResult;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "TranslateResponse{" +
                "status=" + status +
                ", translateResult=" + translateResult +
                '}';
    }

    public enum Status {
        OK,
        UNKNOWN_ERROR
    }
}
