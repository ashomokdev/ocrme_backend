package ocrme_backend.servlets.translate;

import ocrme_backend.translate.Translator;

import javax.annotation.Nullable;

/**
 * Created by iuliia on 8/31/17.
 */
public class TranslateRequestManager {
    public static TranslateResponse translate(
            String deviceLanguageCode,
            @Nullable String sourceLanguageCode,
            @Nullable String targetLanguageCode,
            String sourceText) {

        TranslateResponse response = new TranslateResponse();
        try {
            if (sourceLanguageCode == null || sourceLanguageCode.isEmpty()) {
                sourceLanguageCode = detectSourceLanguage(sourceText);
            }
            if (targetLanguageCode == null || targetLanguageCode.isEmpty()) {
                targetLanguageCode = deviceLanguageCode;
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
            response.setStatus(TranslateResponse.Status.OK);

        } catch (Exception e) {
            e.printStackTrace();
            response.setStatus(TranslateResponse.Status.UNKNOWN_ERROR);
        }
        return response;
    }

    private static String detectSourceLanguage(String sourceText) {
        return Translator.detectLanguage(sourceText);
    }
}
