package ocrme_backend.servlets.translate_deprecated;


import com.google.auth.appengine.AppEngineCredentials;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.storage.StorageOptions;
import com.google.cloud.translate.*;
import com.google.common.collect.ImmutableList;
import ocrme_backend.datastore.gcloud_storage.utils.CloudStorageHelper;

import java.io.IOException;
import java.io.PrintStream;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by iuliia on 5/19/17.
 */

@Deprecated
public class Translator {

    private static final Logger logger = Logger.getLogger(Translator.class.getName());

    /**
     * Translate the source text from source to target language.
     *
     * @param sourceText source text to be translated
     * @param sourceLang source language of the text
     * @param targetLang target language of translated text
     */
    public static String translate(String sourceLang, String targetLang, String sourceText) {
        Translate translate = createTranslateService();
        Translate.TranslateOption srcLang = Translate.TranslateOption.sourceLanguage(sourceLang);
        Translate.TranslateOption tgtLang = Translate.TranslateOption.targetLanguage(targetLang);

        Translation translation = translate.translate(sourceText, srcLang, tgtLang);
        return translation.getTranslatedText();
    }

    /**
     * Detect the language of input text.
     *
     * @param sourceText source text to be detected for language
     * @return code of detected Language or "en" if no results
     */
    public static String detectLanguage(String sourceText) {
        Translate translate = createTranslateService();
        List<Detection> detections = translate.detect(ImmutableList.of(sourceText));
        if (detections.size() > 0) {
            return detections.get(0).getLanguage();
        } else {
            return "en";
        }
    }

    /**
     * Translates the source text in any language to English.
     *
     * @param sourceText source text to be translated
     * @param out        print stream
     */
    public static void translateText(String sourceText, PrintStream out) {
        Translate translate = createTranslateService();
        Translation translation = translate.translate(sourceText);
        out.printf("Source Text:\n\t%s\n", sourceText);
        out.printf("Translated Text:\n\t%s\n", translation.getTranslatedText());
    }

    /**
     * Translate the source text from source to target language.
     * Make sure that your project is whitelisted.
     *
     * @param sourceText source text to be translated
     * @param sourceLang source language of the text
     * @param targetLang target language of translated text
     * @param out        print stream
     */
    public static void translateTextWithOptionsAndModel(
            String sourceText,
            String sourceLang,
            String targetLang,
            PrintStream out) {

        Translate translate = createTranslateService();
        Translate.TranslateOption srcLang = Translate.TranslateOption.sourceLanguage(sourceLang);
        Translate.TranslateOption tgtLang = Translate.TranslateOption.targetLanguage(targetLang);

        // Use translate_deprecated `model` parameter with `base` and `nmt` options.
        Translate.TranslateOption model = Translate.TranslateOption.model("nmt");

        Translation translation = translate.translate(sourceText, srcLang, tgtLang, model);
        out.printf("Source Text:\n\tLang: %s, Text: %s\n", sourceLang, sourceText);
        out.printf("TranslatedText:\n\tLang: %s, Text: %s\n", targetLang,
                translation.getTranslatedText());
    }


    /**
     * Translate the source text from source to target language.
     *
     * @param sourceText source text to be translated
     * @param sourceLang source language of the text
     * @param targetLang target language of translated text
     * @param out        print stream
     */
    public static void translateTextWithOptions(
            String sourceText,
            String sourceLang,
            String targetLang,
            PrintStream out) {

        Translate translate = createTranslateService();
        Translate.TranslateOption srcLang = Translate.TranslateOption.sourceLanguage(sourceLang);
        Translate.TranslateOption tgtLang = Translate.TranslateOption.targetLanguage(targetLang);

        Translation translation = translate.translate(sourceText, srcLang, tgtLang);
        out.printf("Source Text:\n\tLang: %s, Text: %s\n", sourceLang, sourceText);
        out.printf("TranslatedText:\n\tLang: %s, Text: %s\n", targetLang,
                translation.getTranslatedText());
    }

    /**
     * return a list of supported languages and codes.
     *
     * @param tgtLang optional target language
     */
    public static List<Language> getSupportedLanguages(Optional<String> tgtLang) {
        Translate translate = createTranslateService();
        Translate.LanguageListOption target = Translate.LanguageListOption.targetLanguage(tgtLang.orElse("en"));
        return translate.listSupportedLanguages(target);
    }

    /**
     * Create Google Translate API Service.
     *
     * @return Google Translate Service
     */
    private static Translate createTranslateService() {

        GoogleCredentials credentials;
        Translate translate = null;
        try {
            credentials = AppEngineCredentials.getApplicationDefault();
           translate = TranslateOptions.newBuilder().setCredentials(credentials).build().getService();
        } catch (IOException e) {
            e.printStackTrace();
            logger.log(Level.WARNING, "Translator credentials error " + e.getMessage());
        }

        return translate;
    }
}
