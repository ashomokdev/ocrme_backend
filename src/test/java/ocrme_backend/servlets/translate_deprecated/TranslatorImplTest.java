package ocrme_backend.servlets.translate_deprecated;

import com.google.cloud.translate.Language;
import com.google.cloud.translate.TranslateException;
import ocrme_backend.servlets.translate_deprecated.Translator;
import org.junit.Assume;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.junit.runners.model.Statement;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.junit.Assert.assertTrue;

/**
 * Created by iuliia on 5/19/17.
 */
public class TranslatorImplTest {
    /**
     * skip TranslateException which occurs because of limits
     * https://cloud.google.com/vision/docs/limits
     * Requests per second	10
     */
    @Rule
    public TestRule skipRule = (base, desc) -> new Statement() {
        public void evaluate() throws Throwable {
            try {
                base.evaluate();
            } catch (TranslateException ex) {
                if (ex.getMessage().contains("Daily Limit Exceeded")) {
                    ex.printStackTrace();
                    Assume.assumeTrue(true);
                } else {
                    throw ex;
                }
            }
        }
    };

    @Test
    public void detectRuLanguage() throws Exception {
        String language = Translator.detectLanguage("Отличные условия");
        assertTrue((language).contains("ru"));
    }

    @Test
    public void detectDeLanguage() throws Exception {
        String language = Translator.detectLanguage("Mit Macht kommt große Verantwortung.");
        assertTrue((language).contains("de"));
    }

    @Test
    public void testGermanToSpanishTranslation() throws Exception {
        // Arrange
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        PrintStream out = new PrintStream(bout);

        // Act
        Translator.translateTextWithOptions("Mit Macht kommt große Verantwortung.", "de", "es", out);

        // Assert
        String got = bout.toString();
        assertTrue((got).contains("Con el poder viene una gran responsabilidad."));
    }

    @Test
    public void testGermanToSpanishTranslation2() throws Exception {

        // Act
        String result = Translator.translate("de", "es", "Mit Macht kommt große Verantwortung.");

        // Assert
        assertTrue((result).contains("Con el poder viene una gran responsabilidad."));

    }

    @Test
    public void testGetSupportedLanguages() {
        // by default it'll use English
        List<Language> langByDefault = Translator.getSupportedLanguages(Optional.empty());
        assert !langByDefault.isEmpty();

        List<String> defaultSupportCodes = langByDefault.stream()
                .map(Language::getCode)
                .collect(Collectors.toList());

        List<Language> langByChina = Translator.getSupportedLanguages(Optional.of("zh"));
        assert !langByChina.isEmpty();
        List<String> chinaSupportCodes = langByChina.stream()
                .map(Language::getCode)
                .collect(Collectors.toList());

        assert defaultSupportCodes.containsAll(chinaSupportCodes);
        List<String> diffCodeList = defaultSupportCodes.stream()
                .filter(o -> !chinaSupportCodes.contains(o))
                .collect(Collectors.toList());

        assert diffCodeList.isEmpty();
    }

}
