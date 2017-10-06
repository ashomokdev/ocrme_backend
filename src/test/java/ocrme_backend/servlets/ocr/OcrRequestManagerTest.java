package ocrme_backend.servlets.ocr;

import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import ocrme_backend.datastore.utils.FileProvider;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpSession;

import static ocrme_backend.datastore.utils.FileProvider.getFontAsStream;
import static ocrme_backend.servlets.ocr.OcrRequestManager.BUCKET_FOR_REQUEST_IMAGES_PARAMETER;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created by iuliia on 7/14/17.
 */
public class OcrRequestManagerTest {

    private final LocalServiceTestHelper helper =
            new LocalServiceTestHelper(new LocalDatastoreServiceTestConfig());
    private OcrRequestManager managerImageBytes;
    private OcrRequestManager managerImageUri;
    private OcrRequestManager managerNoLanguages;
    private OcrRequestManager managerLanguagesSet;
    private OcrRequestManager managerLanguagesSetEmptyImage;
    private String defaultFont = "FreeSans.ttf";
    private static final String imgUri = "gs://bucket-for-requests-test/2017-07-26-12-37-36-806-2017-07-26-12-37-36-806-ru.jpg";
    private static final String emptyImgUri = "gs://bucket-for-requests-test/search-3-512.jpg";
    


    @Before
    public void init() throws Exception {
        helper.setUp();
        HttpSession session = mock(HttpSession.class);
        ServletContext mockServletContext = mock(ServletContext.class);
        when(session.getServletContext()).thenReturn(mockServletContext);

        when(mockServletContext.getResourceAsStream(anyString())).thenReturn(getFontAsStream(defaultFont));

        when(mockServletContext.getInitParameter(PdfBuilderSyncTask.BUCKET_FOR_PDFS_PARAMETER)).
                thenReturn("bucket-for-pdf-test");
        when(mockServletContext.getInitParameter(BUCKET_FOR_REQUEST_IMAGES_PARAMETER)).
                thenReturn("bucket-for-requests-test");

        String[] languages = new String[]{"ru"};
        String filename = "ru.jpg";
        byte[] imageBytes = FileProvider.getRusImageFile().getImageBytes();
        String[] languagesSet = new String[]{"az", "bn", "bg"};

        managerImageBytes = new OcrRequestManager(filename, imageBytes, languages, session);
        managerImageUri = new OcrRequestManager(imgUri, languages, session);
        managerNoLanguages = new OcrRequestManager(imgUri, null, session);
        managerLanguagesSet = new OcrRequestManager(imgUri, languagesSet, session);
        managerLanguagesSetEmptyImage = new OcrRequestManager(emptyImgUri, languagesSet, session);
     
    }

    @After
    public void tearDown() {
        helper.tearDown();
    }

    @Test
    public void processForResult() throws Exception {
        OcrResponse response1 = managerImageBytes.process();
        Assert.assertTrue(response1.getTextResult().length() > 0);
        Assert.assertTrue(response1.getPdfResultGsUrl().length() > 0);
        Assert.assertTrue(response1.getStatus().equals(OcrResponse.Status.OK));


        OcrResponse response2 = managerImageUri.process();
        Assert.assertTrue(response2.getTextResult().length() > 0);
        Assert.assertTrue(response2.getPdfResultGsUrl().length() > 0);
        Assert.assertTrue(response2.getStatus().equals(OcrResponse.Status.OK));

        OcrResponse response3 = managerNoLanguages.process();
        Assert.assertTrue(response3.getTextResult().length() > 0);
        Assert.assertTrue(response3.getPdfResultGsUrl().length() > 0);
        Assert.assertTrue(response3.getStatus().equals(OcrResponse.Status.OK));

        OcrResponse response4 = managerLanguagesSet.process();
        Assert.assertTrue(response4.getTextResult() == null);
        Assert.assertTrue(response4.getPdfResultGsUrl()==null);
        Assert.assertTrue(response4.getStatus().equals(OcrResponse.Status.INVALID_LANGUAGE_HINTS));

        OcrResponse response5 = managerLanguagesSetEmptyImage.process();
        Assert.assertTrue(response5.getTextResult() == null);
        Assert.assertTrue(response5.getPdfResultGsUrl()==null);
        Assert.assertTrue(response5.getStatus().equals(OcrResponse.Status.TEXT_NOT_FOUND));
    }
}