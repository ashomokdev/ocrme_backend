package ocrme_backend.translate;

import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.security.GeneralSecurityException;

import static com.google.common.truth.Truth.assertThat;

/**
 * Created by iuliia on 5/19/17.
 */
public class TranslatorImplTest {

    private Translator translator;

    @Before
    public void init() throws IOException, GeneralSecurityException {
        translator = new TranslatorImpl();
    }

    @Test
    public void testGermanToSpanishTranslation() throws Exception {

        // Act
        String result = translator.translate("Mit Macht kommt große Verantwortung.", "de", "es");

        // Assert
        assertThat(result).contains("Con el poder viene una gran responsabilidad.");

    }
}
