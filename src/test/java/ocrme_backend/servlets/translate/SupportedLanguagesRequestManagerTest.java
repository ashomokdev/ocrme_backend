package ocrme_backend.servlets.translate;

import org.junit.Test;

/**
 * Created by iuliia on 8/31/17.
 */
public class SupportedLanguagesRequestManagerTest {

    @Test
    public void getSupportedLanguages() throws Exception {
        SupportedLanguagesResponse response = SupportedLanguagesRequestManager.getSupportedLanguages("de");
        assert (response.getSupportedLanguages().size() > 0);
        assert (response.getStatus().equals(SupportedLanguagesResponse.Status.OK));
    }

}