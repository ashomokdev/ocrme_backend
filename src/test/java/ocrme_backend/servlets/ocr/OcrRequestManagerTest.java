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
import static ocrme_backend.servlets.ocr.OcrRequestManager.BUCKET_FOR_REQUESTS_PARAMETER;
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
    private String defaultFont = "FreeSans.ttf";
    private static final String imgUri = "gs://bucket-for-requests-test/2017-07-26-12-37-36-806-2017-07-26-12-37-36-806-ru.jpg";


    @Before
    public void init() throws Exception {
        helper.setUp();
        HttpSession session = mock(HttpSession.class);
        ServletContext mockServletContext = mock(ServletContext.class);
        when(session.getServletContext()).thenReturn(mockServletContext);

        when(mockServletContext.getResourceAsStream(anyString())).thenReturn(getFontAsStream(defaultFont));

        when(mockServletContext.getInitParameter(PdfBuilderSyncTask.BUCKET_FOR_PDFS_PARAMETER)).
                thenReturn("bucket-for-pdf-test");
        when(mockServletContext.getInitParameter(BUCKET_FOR_REQUESTS_PARAMETER)).
                thenReturn("bucket-for-requests-test");

        String[] languages = new String[]{"ru"};
        String filename = "ru.jpg";
        byte[] imageBytes = FileProvider.getRusImageFile().getImageBytes();

        managerImageBytes = new OcrRequestManager(filename, imageBytes, languages, session);
        managerImageUri = new OcrRequestManager(imgUri, languages, session);
    }

    @After
    public void tearDown() {
        helper.tearDown();
    }

    @Test
    public void processForResult() throws Exception {
        OcrResponse response1 = managerImageBytes.process();
        Assert.assertTrue(response1.getTextResult().length() > 0);
        Assert.assertTrue(response1.getPdfResultUrl().length() > 0);
        Assert.assertTrue(response1.getStatus().equals(OcrResponse.Status.OK));


        OcrResponse response2 = managerImageUri.process();
        Assert.assertTrue(response2.getTextResult().length() > 0);
        Assert.assertTrue(response2.getPdfResultUrl().length() > 0);
        Assert.assertTrue(response2.getStatus().equals(OcrResponse.Status.OK));
    }
}