package ocrme_backend.servlets.translate_deprecated;

import org.junit.Test;

/**
 * Created by iuliia on 8/31/17.
 */
public class SupportedLanguagesRequestManagerDeprecatedTest {

    @Test
    public void getSupportedLanguages() throws Exception {
        SupportedLanguagesResponseDeprecated response = SupportedLanguagesRequestManagerDeprecated.getSupportedLanguages("gd");
        assert (response.getSupportedLanguages().size() > 0);
        assert (response.getStatus().equals(SupportedLanguagesResponseDeprecated.Status.OK));
    }

}