package ocrme_backend.servlets.translate;

/**
 * Created by iuliia on 5/22/17.
 */
public class TranslateRequestBean {
    private String deviceLang;
    private String sourceLang;
    private String targetLang;
    private String sourceText;


    public String getSourceLang() {
        return sourceLang;
    }

    public String getTargetLang() {
        return targetLang;
    }

    public String getSourceText() {
        return sourceText;
    }

    public String getDeviceLang() {
        return deviceLang;
    }
}