package ocrme_backend.servlets.translate_deprecated;

import com.google.cloud.translate.Language;

import java.util.List;
import java.util.Optional;

/**
 * Created by iuliia on 8/31/17.
 */

@Deprecated
public class SupportedLanguagesRequestManagerDeprecated {
    public static SupportedLanguagesResponseDeprecated getSupportedLanguages(String deviceLanguageCode) {

        SupportedLanguagesResponseDeprecated response = new SupportedLanguagesResponseDeprecated();
        try {
            Optional<String> inputLanguage;
            if (deviceLanguageCode == null || deviceLanguageCode.isEmpty()) {
                inputLanguage = Optional.empty();
            } else {
                inputLanguage = Optional.of(deviceLanguageCode);
            }
            List<Language> languages = Translator.getSupportedLanguages(inputLanguage);
            response.setSupportedLanguages(languages);
            response.setStatus(SupportedLanguagesResponseDeprecated.Status.OK);
        } catch (Exception e) {
            e.printStackTrace();
            response.setStatus(SupportedLanguagesResponseDeprecated.Status.UNKNOWN_ERROR);
        }
        return response;
    }
}
