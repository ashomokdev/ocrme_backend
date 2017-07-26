package ocrme_backend.servlets.ocr;

import ocrme_backend.datastore.utils.FileProvider;
import ocrme_backend.datastore.utils.PdfBuilderInputDataProvider;
import ocrme_backend.file_builder.pdfbuilder.PDFBuilderImpl;
import ocrme_backend.file_builder.pdfbuilder.PdfBuilderInputData;
import ocrme_backend.file_builder.pdfbuilder.PdfBuilderOutputData;
import ocrme_backend.file_builder.pdfbuilder.PdfBuilderOutputData.Status;
import ocrme_backend.file_builder.pdfbuilder.TextUnit;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpSession;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import static ocrme_backend.datastore.utils.FileProvider.getFontAsStream;
import static ocrme_backend.file_builder.pdfbuilder.PDFBuilderImpl.FONT_PATH_PARAMETER;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created by iuliia on 7/13/17.
 */
public class PdfBuilderSyncTaskTest {

    private String rusFilename = "rus.jpg";
    private String defaultFont = "FreeSans.ttf";

    private PdfBuilderInputData data;
    private HttpSession session;
    private String simpleChinaText = "简单的文字中国";

    @Before
    public void init() throws Exception {

        List<String> languages = new ArrayList<>();
        languages.add("ru");

        data = PdfBuilderInputDataProvider.ocrForData(rusFilename, languages);

        session = mock(HttpSession.class);

        ServletContext mockServletContext = mock(ServletContext.class);
        when(session.getServletContext()).thenReturn(mockServletContext);
        when(mockServletContext.getResourceAsStream(anyString())).thenReturn(getFontAsStream(defaultFont));

        when(mockServletContext.getInitParameter(PdfBuilderSyncTask.BUCKET_FOR_PDFS_PARAMETER)).
                thenReturn("bucket-for-pdf-test");
    }


    @Test
    public void testExecute() throws Exception {
        PdfBuilderOutputData result = new PdfBuilderSyncTask(data, session).execute();
        Assert.assertTrue(result != null);
        Assert.assertTrue(result.getUrl().length() > 0);
        Assert.assertTrue(result.getStatus().equals(PdfBuilderOutputData.Status.OK));
    }

    @Test
    public void processFileWithNoText() throws Exception {
        data = new PdfBuilderInputData(300, 300, new ArrayList<>());

        PdfBuilderOutputData result = new PdfBuilderSyncTask(data, session).execute();

        Assert.assertTrue(
                result.getStatus().equals(Status.PDF_CAN_NOT_BE_CREATED_EMPTY_DATA));
    }

    @Test
    public void processFileWithNotSupportedLanguage() throws Exception {

        List<TextUnit> texts = new ArrayList<>();
        texts.add(new TextUnit(simpleChinaText, 20, 200, 200, 20));
        data = new PdfBuilderInputData(300, 300, texts);
        PdfBuilderOutputData result = new PdfBuilderSyncTask(data, session).execute();

        Assert.assertTrue(
                result.getStatus().equals(Status.PDF_CAN_NOT_BE_CREATED_LANGUAGE_NOT_SUPPORTED));

    }

}