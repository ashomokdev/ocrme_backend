package ocrme_backend.servlets.translate;

import com.google.cloud.translate.Language;
import ocrme_backend.translate.Translator;

import java.util.List;
import java.util.Optional;

/**
 * Created by iuliia on 8/31/17.
 */
public class SupportedLanguagesRequestManager {
    public static SupportedLanguagesResponse getSupportedLanguages(String deviceLanguageCode) {

        SupportedLanguagesResponse response = new SupportedLanguagesResponse();
        try {
            Optional<String> inputLanguage;
            if (deviceLanguageCode == null || deviceLanguageCode.isEmpty()) {
                inputLanguage = Optional.empty();
            } else {
                inputLanguage = Optional.of(deviceLanguageCode);
            }
            List<Language> languages = Translator.getSupportedLanguages(inputLanguage);
            response.setSupportedLanguages(languages);
            response.setStatus(SupportedLanguagesResponse.Status.OK);
        } catch (Exception e) {
            e.printStackTrace();
            response.setStatus(SupportedLanguagesResponse.Status.UNKNOWN_ERROR);
        }
        return response;
    }
}
