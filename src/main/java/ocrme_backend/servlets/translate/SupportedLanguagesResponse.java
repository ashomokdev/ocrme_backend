package ocrme_backend.servlets.translate;

import com.google.cloud.translate.Language;

import java.io.Serializable;
import java.util.List;

/**
 * Created by iuliia on 6/27/17.
 */
public class SupportedLanguagesResponse implements Serializable {

    private List<Language> supportedLanguages;
    private Status status;

    public enum Status {
        OK,
        UNKNOWN_ERROR
    }

    public void setSupportedLanguages(List<Language> supportedLanguages) {
        this.supportedLanguages = supportedLanguages;
    }
    public void setStatus(Status status) {
        this.status = status;
    }

    public List<Language> getSupportedLanguages() {
        return supportedLanguages;
    }
    public Status getStatus() {
        return status;
    }

}
