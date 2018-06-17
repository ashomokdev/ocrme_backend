package ocrme_backend.servlets.translate_deprecated;

/**
 * Created by iuliia on 5/22/17.
 */
@Deprecated
public class TranslateRequestBeanDeprecated {
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
}
