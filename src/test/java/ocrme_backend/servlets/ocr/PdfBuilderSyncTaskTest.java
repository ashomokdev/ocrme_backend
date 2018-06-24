package ocrme_backend.servlets.ocr;

import ocrme_backend.utils.FileProvider;
import ocrme_backend.utils.PdfBuilderInputDataProvider;
import ocrme_backend.file_builder.pdfbuilder.PdfBuilderInputData;
import ocrme_backend.file_builder.pdfbuilder.PdfBuilderOutputData;
import ocrme_backend.file_builder.pdfbuilder.PdfBuilderOutputData.Status;
import ocrme_backend.file_builder.pdfbuilder.TextUnit;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.List;

import static ocrme_backend.utils.FileProvider.getFontAsStream;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created by iuliia on 7/13/17.
 */
public class PdfBuilderSyncTaskTest {

    private String rusFilename = "rus.jpg";

    private PdfBuilderInputData data;
    private HttpSession session;
    private String simpleChinaText = "简单的文字中国";

    @Before
    public void init() throws Exception {

        List<String> languages = new ArrayList<>();
        languages.add("ru");

        data = PdfBuilderInputDataProvider.ocrForData(rusFilename, languages).getPdfBuilderInputData();

        session = mock(HttpSession.class);

        ServletContext mockServletContext = mock(ServletContext.class);
        when(session.getServletContext()).thenReturn(mockServletContext);
        String defaultFont = FileProvider.getDefaultFont();
        when(mockServletContext.getResourceAsStream(anyString())).thenReturn(getFontAsStream(defaultFont));

        when(mockServletContext.getInitParameter(PdfBuilderSyncTask.BUCKET_FOR_PDFS_PARAMETER)).
                thenReturn("bucket-for-pdf-test");
    }


    @Test
    public void testExecute() {
        PdfBuilderOutputData result = new PdfBuilderSyncTask(data, session).execute();
        Assert.assertNotNull(result);
        Assert.assertTrue(result.getGsUrl().length() > 0);
        Assert.assertEquals(result.getStatus(), Status.OK);
    }

    @Test
    public void processFileWithNoText() {
        data = new PdfBuilderInputData( new ArrayList<>());

        PdfBuilderOutputData result = new PdfBuilderSyncTask(data, session).execute();

        Assert.assertEquals(result.getStatus(), Status.PDF_CAN_NOT_BE_CREATED_EMPTY_DATA);
    }

    @Test
    public void processFileWithNotSupportedLanguage() {

        List<TextUnit> texts = new ArrayList<>();
        texts.add(new TextUnit(simpleChinaText, 20, 200, 200, 20));
        data = new PdfBuilderInputData( texts);
        PdfBuilderOutputData result = new PdfBuilderSyncTask(data, session).execute();

        Assert.assertEquals(result.getStatus(), Status.PDF_CAN_NOT_BE_CREATED_LANGUAGE_NOT_SUPPORTED);

    }

}