package ocrme_backend.servlets.translate.supported_languages;

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

    public List<Language> getSupportedLanguages() {
        return supportedLanguages;
    }
    public Status getStatus() {
        return status;
    }


    public void setSupportedLanguages(List<Language> supportedLanguages) {
        this.supportedLanguages = supportedLanguages;
    }
    public void setStatus(SupportedLanguagesResponse.Status status) {
        this.status = status;
    }

    public static class Language implements Serializable{
        private final String code;
        private final String name;

        public Language(String code, String name) {
            this.code = code;
            this.name = name;
        }

        public String getCode() {
            return this.code;
        }

        public String getName() {
            return this.name;
        }
    }
}
