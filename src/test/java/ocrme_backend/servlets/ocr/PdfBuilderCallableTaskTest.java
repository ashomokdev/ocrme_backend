package ocrme_backend.servlets.ocr;

import ocrme_backend.datastore.utils.FileProvider;
import ocrme_backend.file_builder.pdfbuilder.PDFBuilderImpl;
import ocrme_backend.file_builder.pdfbuilder.PDFData;
import org.junit.Assert;
import org.junit.Test;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpSession;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created by iuliia on 7/13/17.
 */
public class PdfBuilderCallableTaskTest {
    @Test
    public void testCall() throws Exception {
        //prepare
        ExecutorService service = Executors.newFixedThreadPool(2);

        Future<PDFData> pdfDataFuture = service.submit(
                new OcrCallableTask(FileProvider.getItemStreamFile(), null));
        PDFData data = pdfDataFuture.get();
        HttpSession session = mock(HttpSession.class);
        String path = Thread.currentThread().getContextClassLoader().getResource("temp/").getPath();

        ServletContext mockServletContext = mock(ServletContext.class);
        when(mockServletContext.getRealPath(PDFBuilderImpl.uploadsDir)).
                thenReturn(path);

        when(mockServletContext.getInitParameter(PdfBuilderCallableTask.BUCKET_FOR_PDFS_PARAMETER)).
                thenReturn("bucket-for-pdf-test");
        when(session.getServletContext()).thenReturn(mockServletContext);
        when(session.getId()).thenReturn("0");

        //test
        Future<String> result = service.submit(
                new PdfBuilderCallableTask(data, session));
        Assert.assertTrue(result.get() != null);
        Assert.assertTrue(result.get().length() > 0);
        service.shutdown();
    }

}