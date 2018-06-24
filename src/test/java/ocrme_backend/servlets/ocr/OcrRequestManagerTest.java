package ocrme_backend.servlets.ocr;

import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import ocrme_backend.utils.FileProvider;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpSession;

import static ocrme_backend.utils.FileProvider.getFontAsStream;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created by iuliia on 7/14/17.
 */

//todo clean bucket when finished
public class OcrRequestManagerTest {

    private final LocalServiceTestHelper helper =
            new LocalServiceTestHelper(new LocalDatastoreServiceTestConfig());
    private OcrRequestManager managerImageUri;
    private OcrRequestManager managerNoLanguages;
    private OcrRequestManager managerLanguagesSet;
    private OcrRequestManager managerLanguagesSetEmptyImage;

    @Before
    public void init() {
        helper.setUp();
        HttpSession session = mock(HttpSession.class);
        ServletContext mockServletContext = mock(ServletContext.class);
        when(session.getServletContext()).thenReturn(mockServletContext);

        String defaultFont = FileProvider.getDefaultFont();
        when(mockServletContext.getResourceAsStream(anyString())).thenReturn(getFontAsStream(defaultFont));

        when(mockServletContext.getInitParameter(PdfBuilderSyncTask.BUCKET_FOR_PDFS_PARAMETER)).
                thenReturn("bucket-for-pdf-test");

        String[] languages = new String[]{"ru"};
        String[] languagesSet = new String[]{"az", "bn", "bg"};

        String imgUri = FileProvider.getImageUri();
        String emptyImgUri = FileProvider.getEmptyImageUri();
        managerImageUri = new OcrRequestManager(null, imgUri, languages, session);
        managerNoLanguages = new OcrRequestManager(null, imgUri, null, session);
        managerLanguagesSet = new OcrRequestManager(null, imgUri, languagesSet, session);
        managerLanguagesSetEmptyImage = new OcrRequestManager(null, emptyImgUri, languagesSet, session);
    }

    @After
    public void tearDown() {
        helper.tearDown();
    }

    @Test
    public void processForResult() {
        OcrResponse response2 = managerImageUri.process();
        Assert.assertTrue(response2.getOcrResult().getTextResult().length() > 0);
        Assert.assertTrue(response2.getOcrResult().getPdfResultGsUrl().length() > 0);
        Assert.assertEquals(response2.getStatus(), OcrResponse.Status.OK);

        OcrResponse response3 = managerNoLanguages.process();
        Assert.assertTrue(response3.getOcrResult().getTextResult().length() > 0);
        Assert.assertTrue(response3.getOcrResult().getPdfResultGsUrl().length() > 0);
        Assert.assertEquals(response3.getStatus(), OcrResponse.Status.OK);

        OcrResponse response4 = managerLanguagesSet.process();
        Assert.assertNull(response4.getOcrResult());
        Assert.assertEquals(response4.getStatus(), OcrResponse.Status.INVALID_LANGUAGE_HINTS);

        OcrResponse response5 = managerLanguagesSetEmptyImage.process();
        Assert.assertNull(response5.getOcrResult());
        Assert.assertEquals(response5.getStatus(), OcrResponse.Status.TEXT_NOT_FOUND);
    }
}