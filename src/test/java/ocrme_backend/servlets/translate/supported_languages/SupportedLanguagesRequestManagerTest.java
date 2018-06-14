package ocrme_backend.servlets.translate.supported_languages;

import org.junit.Before;
import org.junit.Test;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpSession;

import static ocrme_backend.servlets.translate.supported_languages.SupportedLanguagesRequestManager.SECRET_KEYS_FILE_PATH;
import static ocrme_backend.servlets.translate.supported_languages.SupportedLanguagesRequestManager.SECRET_KEY_PARAMETER;
import static ocrme_backend.utils.FileProvider.getFileAsInputStream;
import static ocrme_backend.utils.FileProvider.pathToSecretKeys;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


public class SupportedLanguagesRequestManagerTest {
    private SupportedLanguagesRequestManager manager;
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

        manager = new SupportedLanguagesRequestManager(session);
    }

    @Test
    public void getSupportedLanguages() {
        SupportedLanguagesResponse response = manager.getSupportedLanguages();
        assert (response.getSupportedLanguages().size() > 0);
        assert (response.getStatus().equals(SupportedLanguagesResponse.Status.OK));
    }

}