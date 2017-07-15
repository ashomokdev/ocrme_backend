package ocrme_backend.servlets.ocr;

import ocrme_backend.datastore.utils.FileProvider;
import ocrme_backend.file_builder.pdfbuilder.PDFBuilderImpl;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpSession;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created by iuliia on 7/14/17.
 */
public class OcrRequestManagerTest {

    private OcrRequestManager manager;

    @Before
    public void init() throws Exception {

        HttpSession session = mock(HttpSession.class);
        String path = Thread.currentThread().getContextClassLoader().getResource("temp/").getPath();

        ServletContext mockServletContext = mock(ServletContext.class);
        when(mockServletContext.getRealPath(PDFBuilderImpl.uploadsDir)).
                thenReturn(path);

        when(mockServletContext.getInitParameter(PdfBuilderCallableTask.BUCKET_FOR_PDFS_PARAMETER)).
                thenReturn("bucket-for-pdf-test");
        when(session.getServletContext()).thenReturn(mockServletContext);
        when(session.getId()).thenReturn("0");
        ExecutorService service = Executors.newFixedThreadPool(2);
        when(mockServletContext.getAttribute("threadPoolAlias")).thenReturn(service);

        String[] languages = new String[]{"en"};
        manager = new OcrRequestManager(FileProvider.getItemStreamFile(), languages, session);
    }

    @Test
    public void processForResult() throws Exception {
        OcrResponse response = manager.processForResult();
        Assert.assertTrue(response.getTextResult().length() > 0);
        Assert.assertTrue(response.getPdfResultUrl().length() >0);
        Assert.assertFalse(response.isContainsError());
    }
}