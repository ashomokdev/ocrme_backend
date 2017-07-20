package ocrme_backend.servlets.ocr;

import ocrme_backend.datastore.utils.FileProvider;
import ocrme_backend.file_builder.pdfbuilder.PDFBuilderImpl;
import ocrme_backend.file_builder.pdfbuilder.PdfBuilderInputData;
import ocrme_backend.file_builder.pdfbuilder.PdfBuilderOutputData;
import ocrme_backend.file_builder.pdfbuilder.TextUnit;
import org.apache.commons.fileupload.FileItemIterator;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpSession;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import ocrme_backend.file_builder.pdfbuilder.PdfBuilderOutputData.Status;

/**
 * Created by iuliia on 7/13/17.
 */
public class PdfBuilderCallableTaskTest {

    private String defaultFont = "FreeSans.ttf";
    private ExecutorService service;
    private PdfBuilderInputData data;
    private HttpSession session;
    private String simpleChinaText = "简单的文字中国";

    @Before
    public void init() throws Exception {
        service = Executors.newFixedThreadPool(2);
        FileItemIterator mockFileItemIterator = mock(FileItemIterator.class);
        when(mockFileItemIterator.next()).thenReturn(FileProvider.getItemStreamFile());
        when(mockFileItemIterator.hasNext()).thenReturn(true).thenReturn(false);

        Future<PdfBuilderInputData> pdfDataFuture = service.submit(
                new OcrCallableTask(mockFileItemIterator, null));
        data = pdfDataFuture.get();
        session = mock(HttpSession.class);
        String path = Thread.currentThread().getContextClassLoader().getResource("temp/").getPath();

        ServletContext mockServletContext = mock(ServletContext.class);
        when(mockServletContext.getRealPath(PDFBuilderImpl.uploadsDir)).
                thenReturn(path);

        when(mockServletContext.getRealPath(PDFBuilderImpl.FONT_PATH)).thenReturn(getFont(defaultFont));

        when(mockServletContext.getInitParameter(PdfBuilderCallableTask.BUCKET_FOR_PDFS_PARAMETER)).
                thenReturn("bucket-for-pdf-test");
        when(session.getServletContext()).thenReturn(mockServletContext);
        when(session.getId()).thenReturn("0");
    }

    @After
    public void shutdown() {
        service.shutdown();
    }


    @Test
    public void testCall() throws Exception {
        Future<PdfBuilderOutputData> result = service.submit(
                new PdfBuilderCallableTask(data, session));
        Assert.assertTrue(result.get() != null);
        Assert.assertTrue(result.get().getUrl().length() > 0);
        Assert.assertTrue(result.get().getStatus().equals(PdfBuilderOutputData.Status.OK));
    }

    @Test
    public void processFileWithNoText() throws Exception {
        data = new PdfBuilderInputData(300, 300, new ArrayList<>());
        Future<PdfBuilderOutputData> result = service.submit(
                new PdfBuilderCallableTask(data, session));

        Assert.assertTrue(
                result.get().getStatus().equals(Status.PDF_CAN_NOT_BE_CREATED_EMPTY_DATA));
    }

    @Test
    public void processFileWithNotSupportedLanguage() throws Exception {

        List<TextUnit> texts = new ArrayList<>();
        texts.add(new TextUnit(simpleChinaText, 20, 200, 200, 20));
        data = new PdfBuilderInputData(300, 300, texts);
        Future<PdfBuilderOutputData> result = service.submit(
                new PdfBuilderCallableTask(data, session));

        Assert.assertTrue(
                result.get().getStatus().equals(Status.PDF_CAN_NOT_BE_CREATED_LANGUAGE_NOT_SUPPORTED));

    }

    private String getFont(String fontFileName) {
        URL url = Thread.currentThread().getContextClassLoader().getResource("fonts/" + fontFileName);
        assert url != null;
        return url.getPath();
    }

}