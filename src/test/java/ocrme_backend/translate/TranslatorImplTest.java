package ocrme_backend.translate;

import com.google.api.gax.grpc.ApiException;
import com.google.cloud.translate.TranslateException;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.security.GeneralSecurityException;

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

    /**
     * skip TranslateException which occurs because of limits
     https://cloud.google.com/vision/docs/limits
     Requests per second	10
     */
    @Rule
    public TestRule skipRule = new TestRule() {
        public Statement apply(final Statement base, Description desc) {

            return new Statement() {
                public void evaluate() throws Throwable {
                    try {
                        base.evaluate();
                    } catch (TranslateException ex) {
                        Assume.assumeTrue(true);
                    }
                }
            };
        }
    };

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
