package ocrme_backend.servlets.translate.translate;

import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpSession;

import java.net.MalformedURLException;

import static ocrme_backend.servlets.translate.supported_languages.SupportedLanguagesRequestManager.SECRET_KEYS_FILE_PATH;
import static ocrme_backend.servlets.translate.supported_languages.SupportedLanguagesRequestManager.SECRET_KEY_PARAMETER;
import static ocrme_backend.utils.FileProvider.getFileAsInputStream;
import static ocrme_backend.utils.FileProvider.pathToSecretKeys;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class TranslateRequestManagerTest {

    private final LocalServiceTestHelper helper =
            new LocalServiceTestHelper(new LocalDatastoreServiceTestConfig());
    private TranslateRequestManager manager;
    public static final String BING_KEY_PARAMETER = "bing.key";

    @Before
    public void init() throws Exception {
        helper.setUp();
        HttpSession session = mock(HttpSession.class);
        ServletContext mockServletContext = mock(ServletContext.class);
        when(session.getServletContext()).thenReturn(mockServletContext);

        when(mockServletContext.getResourceAsStream(pathToSecretKeys))
                .thenReturn(getFileAsInputStream(pathToSecretKeys));

        when(mockServletContext.getInitParameter(SECRET_KEYS_FILE_PATH))
                .thenReturn(pathToSecretKeys);

        when(mockServletContext.getInitParameter(SECRET_KEY_PARAMETER))
                .thenReturn(BING_KEY_PARAMETER);

        manager = new TranslateRequestManager(session, null);
    }

    @After
    public void tearDown() {
        helper.tearDown();
    }


    @Test
    public void translate() throws MalformedURLException {
        TranslateResponse response =
                manager.translate(null, "en", "Приветик");
        assert (response.getTranslateResult().getTextResult().length() > 0);
        assert (response.getStatus().equals(TranslateResponse.Status.OK));
        assert (response.getTranslateResult().getTextResult().equals("Hi") || response.getTranslateResult().getTextResult().equals("Hello"));
        assert (response.getTranslateResult().getSourceLanguageCode().equals("ru"));
        assert (response.getTranslateResult().getTargetLanguageCode().equals("en"));

        TranslateResponse response2 =
                manager.translate("ru", "en", "Приветик");
        assert (response2.getTranslateResult().getTextResult().length() > 0);
        assert (response2.getStatus().equals(TranslateResponse.Status.OK));
        assert (response2.getTranslateResult().getTextResult().equals("Hi") || response2.getTranslateResult().getTextResult().equals("Hello"));
        assert (response2.getTranslateResult().getSourceLanguageCode().equals("ru"));
        assert (response2.getTranslateResult().getTargetLanguageCode().equals("en"));
    }
}