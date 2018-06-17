package ocrme_backend.servlets.translate.translate;

import ocrme_backend.servlets.translate.supported_languages.SupportedLanguagesRequestManager;
import ocrme_backend.servlets.translate.supported_languages.SupportedLanguagesResponse;
import org.junit.Before;
import org.junit.Test;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpSession;

import java.net.MalformedURLException;

import static ocrme_backend.servlets.translate.supported_languages.SupportedLanguagesRequestManager.SECRET_KEYS_FILE_PATH;
import static ocrme_backend.servlets.translate.supported_languages.SupportedLanguagesRequestManager.SECRET_KEY_PARAMETER;
import static ocrme_backend.utils.FileProvider.getFileAsInputStream;
import static ocrme_backend.utils.FileProvider.pathToSecretKeys;
import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class TranslateRequestManagerTest {

    private TranslateRequestManager manager;
    public static final String BING_KEY_PARAMETER = "bing.key";

    @Before
    public void init() throws Exception {
        HttpSession session = mock(HttpSession.class);
        ServletContext mockServletContext = mock(ServletContext.class);
        when(session.getServletContext()).thenReturn(mockServletContext);

        when(mockServletContext.getResourceAsStream(pathToSecretKeys))
                .thenReturn(getFileAsInputStream(pathToSecretKeys));

        when(mockServletContext.getInitParameter(SECRET_KEYS_FILE_PATH))
                .thenReturn(pathToSecretKeys);

        when(mockServletContext.getInitParameter(SECRET_KEY_PARAMETER))
                .thenReturn(BING_KEY_PARAMETER);

        manager = new TranslateRequestManager(session);
    }

    @Test
    public void translate() throws MalformedURLException {
        TranslateResponse response =
                manager.translate(null, "en", "Приветик");
        assert (response.getTextResult().length() > 0);
        assert (response.getStatus().equals(TranslateResponse.Status.OK));
        assert (response.getTextResult().equals("Hi") || response.getTextResult().equals("Hello"));
        assert (response.getSourceLanguageCode().equals("ru"));
        assert (response.getTargetLanguageCode().equals("en"));

        TranslateResponse response2 =
                manager.translate("ru", "en", "Приветик");
        assert (response2.getTextResult().length() > 0);
        assert (response2.getStatus().equals(TranslateResponse.Status.OK));
        assert (response2.getTextResult().equals("Hi") || response2.getTextResult().equals("Hello"));
        assert (response2.getSourceLanguageCode().equals("ru"));
        assert (response2.getTargetLanguageCode().equals("en"));
    }
}