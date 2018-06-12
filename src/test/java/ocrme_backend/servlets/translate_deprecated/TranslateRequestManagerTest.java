package ocrme_backend.servlets.translate_deprecated;

import org.junit.Test;

/**
 * Created by iuliia on 8/31/17.
 */
public class TranslateRequestManagerTest {
    @Test
    public void translate() throws Exception {
        TranslateResponse response = TranslateRequestManager.translate(
                null, "ru", "My mom is kind");

        assert (! response.getTextResult().isEmpty());
    }

}