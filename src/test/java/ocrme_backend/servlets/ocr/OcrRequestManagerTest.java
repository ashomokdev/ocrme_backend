package ocrme_backend.servlets.ocr;

import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import ocrme_backend.datastore.gcloud_storage.utils.CloudStorageHelper;
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

    String imgUri;
    String emptyImgUri;
    HttpSession session;


    @Before
    public void init() {
        helper.setUp();
        session = mock(HttpSession.class);
        ServletContext mockServletContext = mock(ServletContext.class);
        when(session.getServletContext()).thenReturn(mockServletContext);

        String defaultFont = FileProvider.getDefaultFont();
        when(mockServletContext.getResourceAsStream(anyString())).thenReturn(getFontAsStream(defaultFont));

        when(mockServletContext.getInitParameter(PdfBuilderSyncTask.BUCKET_FOR_PDFS_PARAMETER)).
                thenReturn("bucket-for-pdf-test");

        when(mockServletContext.getInitParameter(PdfBuilderSyncTask.DIRECTORY_FOR_PDFS_PARAMETER)).
                thenReturn("dir-for-pdf-test");

        imgUri = FileProvider.getImageUri();
        emptyImgUri = FileProvider.getEmptyImageUri();
    }

    @After
    public void tearDown() {
        helper.tearDown();
        new CloudStorageHelper().clearBucket("bucket-for-pdf-test");
    }

    @Test
    public void testOcr1() {
        OcrRequestManager managerImageUri =
                new OcrRequestManager(null, imgUri, new String[]{"ru"}, session);

        OcrResponse response = managerImageUri.process();
        Assert.assertTrue(response.getOcrResult().getTextResult().length() > 0);
        Assert.assertTrue(response.getOcrResult().getPdfResultGsUrl().length() > 0);
        Assert.assertTrue(response.getOcrResult().getPdfResultMediaUrl().length() > 0);
        Assert.assertTrue(response.getOcrResult().getPdfImageResultMediaUrl().length() > 0);
        Assert.assertTrue(response.getOcrResult().getPdfImageResultGsUrl().length() > 0);
        Assert.assertEquals(OcrResponse.Status.OK, response.getStatus());
    }

    @Test
    public void testOcrNoLanguages() {
        OcrRequestManager managerImageUri =
                new OcrRequestManager(null, imgUri, null, session);
        OcrResponse response = managerImageUri.process();
        Assert.assertTrue(response.getOcrResult().getTextResult().length() > 0);
        Assert.assertTrue(response.getOcrResult().getPdfResultGsUrl().length() > 0);
        Assert.assertEquals(OcrResponse.Status.OK, response.getStatus());
    }

    @Test
    public void testOcrEmptyImage() {
        OcrRequestManager managerImageUri =
                new OcrRequestManager(null, emptyImgUri, null, session);
        OcrResponse response = managerImageUri.process();
        Assert.assertNull(response.getOcrResult());
        Assert.assertEquals(response.getStatus(), OcrResponse.Status.TEXT_NOT_FOUND);
    }

    @Test
    public void testOcrInvalidLanguage() {
        OcrRequestManager managerImageUri =
                new OcrRequestManager(null, imgUri, new String[]{"az", "bn", "bg"}, session);
        OcrResponse response = managerImageUri.process();
        Assert.assertNull(response.getOcrResult());
        Assert.assertEquals(response.getStatus(), OcrResponse.Status.INVALID_LANGUAGE_HINTS);
    }
}