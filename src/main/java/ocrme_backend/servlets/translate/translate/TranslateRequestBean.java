package ocrme_backend.servlets.translate.translate;

import javax.annotation.Nullable;

/**
 * Created by iuliia on 5/22/17.
 */
public class TranslateRequestBean {
    private String sourceLang;
    private String targetLang;
    private String sourceText;
    private @Nullable String idTokenString;

    public @Nullable String getIdTokenString() {
        return idTokenString;
    }

    public String getSourceLang() {
        return sourceLang;
    }

    public String getTargetLang() {
        return targetLang;
    }

    public String getSourceText() {
        return sourceText;
    }
}
