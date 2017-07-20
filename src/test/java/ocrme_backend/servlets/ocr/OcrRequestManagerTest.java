package ocrme_backend.servlets.ocr;

import ocrme_backend.datastore.utils.FileProvider;
import ocrme_backend.file_builder.pdfbuilder.PDFBuilderImpl;
import org.apache.commons.fileupload.FileItemIterator;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpSession;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static ocrme_backend.servlets.ocr.OcrRequestManager.BUCKET_FOR_REQUESTS_PARAMETER;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created by iuliia on 7/14/17.
 */
public class OcrRequestManagerTest {

    private OcrRequestManager manager;
    private String defaultFont = "FreeSans.ttf";

    @Before
    public void init() throws Exception {

        HttpSession session = mock(HttpSession.class);
        String path = Thread.currentThread().getContextClassLoader().getResource("temp/").getPath();

        ServletContext mockServletContext = mock(ServletContext.class);
        when(mockServletContext.getRealPath(PDFBuilderImpl.uploadsDir)).
                thenReturn(path);

        when(mockServletContext.getInitParameter(PdfBuilderCallableTask.BUCKET_FOR_PDFS_PARAMETER)).
                thenReturn("bucket-for-pdf-test");
        when(mockServletContext.getInitParameter(BUCKET_FOR_REQUESTS_PARAMETER)).
                thenReturn("bucket-for-requests-test");
        when(session.getServletContext()).thenReturn(mockServletContext);
        when(mockServletContext.getRealPath(PDFBuilderImpl.FONT_PATH)).thenReturn(getFont(defaultFont));
        when(session.getId()).thenReturn("0");
        ExecutorService service = Executors.newFixedThreadPool(2);
        when(mockServletContext.getAttribute("threadPoolAlias")).thenReturn(service);

        String[] languages = new String[]{"en"};
        FileItemIterator mockFileItemIterator = mock(FileItemIterator.class);
        when(mockFileItemIterator.next()).thenReturn(FileProvider.getItemStreamFile());
        when(mockFileItemIterator.hasNext()).thenReturn(true).thenReturn(false);

        manager = new OcrRequestManager(mockFileItemIterator, languages, session);
    }

    @Test
    public void processForResult() throws Exception {
        OcrResponse response = manager.processForResult();
        Assert.assertTrue(response.getTextResult().length() > 0);
        Assert.assertTrue(response.getPdfResultUrl().length() > 0);
        Assert.assertTrue(response.getStatus().equals(OcrResponse.Status.OK));
    }

    private String getFont(String fontFileName) {
        URL url = Thread.currentThread().getContextClassLoader().getResource("fonts/" + fontFileName);
        assert url != null;
        return url.getPath();
    }
}