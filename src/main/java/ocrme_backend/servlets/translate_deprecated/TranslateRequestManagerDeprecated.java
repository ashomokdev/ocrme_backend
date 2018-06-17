package ocrme_backend.servlets.translate_deprecated;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Created by iuliia on 8/31/17.
 */

@Deprecated
public class TranslateRequestManagerDeprecated {
    public static TranslateResponseDeprecated translate(
            @Nullable String sourceLanguageCode,
            @Nonnull String targetLanguageCode,
            String sourceText) {

        TranslateResponseDeprecated response = new TranslateResponseDeprecated();
        try {
            if (sourceLanguageCode == null || sourceLanguageCode.isEmpty()) {
                sourceLanguageCode = detectSourceLanguage(sourceText);
            }

            String targetText;
            if (sourceLanguageCode.equals(targetLanguageCode)) {
                targetText = sourceText;

            } else {
                targetText = Translator.translate(sourceLanguageCode, targetLanguageCode, sourceText);
            }

            response.setSourceLanguageCode(sourceLanguageCode);
            response.setTargetLanguageCode(targetLanguageCode);
            response.setTextResult(targetText);
            response.setStatus(TranslateResponseDeprecated.Status.OK);

        } catch (Exception e) {
            e.printStackTrace();
            response.setStatus(TranslateResponseDeprecated.Status.UNKNOWN_ERROR);
        }
        return response;
    }

    private static String detectSourceLanguage(String sourceText) {
        return Translator.detectLanguage(sourceText);
    }
}
