package ocrme_backend.servlets.translate.translate;

/**
 * Created by iuliia on 8/31/17.
 */
public class TranslateResponse {

    private String sourceLanguageCode;
    private String targetLanguageCode;
    private Status status;
    private String textResult;

    public String getSourceLanguageCode() {
        return sourceLanguageCode;
    }

    public void setSourceLanguageCode(String sourceLanguageCode) {
        this.sourceLanguageCode = sourceLanguageCode;
    }

    public String getTargetLanguageCode() {
        return targetLanguageCode;
    }

    public void setTargetLanguageCode(String targetLanguageCode) {
        this.targetLanguageCode = targetLanguageCode;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public String getTextResult() {
        return textResult;
    }

    public void setTextResult(String textResult) {
        this.textResult = textResult;
    }

    public enum Status {
        OK,
        UNKNOWN_ERROR
    }
}
