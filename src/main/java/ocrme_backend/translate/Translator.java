package ocrme_backend.translate;

/**
 * Created by iuliia on 5/19/17.
 */
public interface Translator {
    String translate(String sourceLang, String targetLang, String sourceText);
}
