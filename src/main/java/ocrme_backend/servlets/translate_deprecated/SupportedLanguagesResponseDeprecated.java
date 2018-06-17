package ocrme_backend.servlets.translate_deprecated;

import com.google.cloud.translate.Language;

import java.io.Serializable;
import java.util.List;

/**
 * Created by iuliia on 6/27/17.
 */
@Deprecated
public class SupportedLanguagesResponseDeprecated implements Serializable {

    private List<Language> supportedLanguages;
    private Status status;

    public List<Language> getSupportedLanguages() {
        return supportedLanguages;
    }

    public void setSupportedLanguages(List<Language> supportedLanguages) {
        this.supportedLanguages = supportedLanguages;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public enum Status {
        OK,
        UNKNOWN_ERROR
    }

}
