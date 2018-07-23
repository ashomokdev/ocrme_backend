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

    public void setSourceLang(String sourceLang) {
        this.sourceLang = sourceLang;
    }

    public void setTargetLang(String targetLang) {
        this.targetLang = targetLang;
    }

    public void setSourceText(String sourceText) {
        this.sourceText = sourceText;
    }

    public void setIdTokenString(@Nullable String idTokenString) {
        this.idTokenString = idTokenString;
    }

}
