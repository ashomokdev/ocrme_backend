package ocrme_backend.translate;

import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.security.GeneralSecurityException;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.assertTrue;

/**
 * Created by iuliia on 5/19/17.
 */
public class TranslatorImplTest {

    private Translator translator;

    @Before
    public void init() throws IOException, GeneralSecurityException {
        translator = new TranslatorImpl();
    }

    @Test public void testGermanToSpanishTranslation() throws Exception {
        // Arrange
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        PrintStream out = new PrintStream(bout);

        // Act
        TranslatorImpl.translateTextWithOptions("Mit Macht kommt große Verantwortung.", "de", "es", out);

        // Assert
        String got = bout.toString();
        assertTrue((got).contains("Con el poder viene una gran responsabilidad."));
    }

    @Test
    public void testGermanToSpanishTranslation2() throws Exception {

        // Act
        String result = translator.translate("de", "es", "Mit Macht kommt große Verantwortung.");

        // Assert
        assertTrue((result).contains("Con el poder viene una gran responsabilidad."));

    }

    @Test public void testGermanLangDetection() throws Exception {
        // Arrange
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        PrintStream out = new PrintStream(bout);

        // Act
        TranslatorImpl.detectLanguage("Mit Macht kommt große Verantwortung.", out);

        // Assert
        String got = bout.toString();
        assertTrue((got).contains("language=de"));


        assertTrue((got).contains("language=de"));

        // Assert
        Double confidence = Double.parseDouble(
                got.split("confidence=")[1].split("}")[0]
        );
        assertTrue((confidence)>=0.9);
    }
}
