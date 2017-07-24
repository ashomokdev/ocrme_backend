package ocrme_backend.file_builder.pdfbuilder;

import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.parser.PdfTextExtractor;
import ocrme_backend.ocr.OCRProcessor;
import ocrme_backend.ocr.OcrProcessorImpl;
import ocrme_backend.servlets.ocr.PdfBuilderSyncTask;
import org.apache.commons.io.FileUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import javax.annotation.Nullable;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpSession;
import java.io.*;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.List;

import static ocrme_backend.file_builder.pdfbuilder.PDFBuilderImpl.FONT_PATH_PARAMETER;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Created by iuliia on 6/4/17.
 */
public class PDFBuilderImplTest {

    private PDFBuilderImpl pdfBuilder;

    private String defaultFileName = "img.jpg";
    private String a4FileName = "a4.jpg";
    private String columnsFileName = "columns.png";
    private String rusFilename = "rus.jpg";

    private String defaultFont = "FreeSans.ttf";

    private String simpleRusText = "Простой русский текст";
    private String simpleUkrText = "Проста українська мова";
    private String simpleEngText = "Simple English text";
    private String simplePlnText = "Ocenia się że język polski jest językiem ojczystym około 44 milionów ludzi na świecie";
    private String simpleHindiText = "साधारण पाठ हिन्दी";
    private String simpleChinaText = "简单的文字中国";

    @Before
    public void init() throws IOException, GeneralSecurityException {
        HttpSession session = mock(HttpSession.class);
        pdfBuilder = spy(new PDFBuilderImpl(session));

        ServletContext mockServletContext = mock(ServletContext.class);
        when(session.getServletContext()).thenReturn(mockServletContext);
        when(mockServletContext.getInitParameter(FONT_PATH_PARAMETER)).thenReturn(getFontPath(defaultFont));

        when(session.getId()).thenReturn("0");

        String path = Thread.currentThread().getContextClassLoader().getResource("temp/").getPath();
        when(mockServletContext.getRealPath(PDFBuilderImpl.uploadsDir)).
                thenReturn(path);
    }

    @Test
    public void testBuildStream() throws Exception {
        ArrayList<String> languages = new ArrayList<>();
        languages.add("ru");
        PdfBuilderInputData data = getTestData(rusFilename, languages);

        ByteArrayOutputStream baos = pdfBuilder.buildPdfStream(data);
        byte[] byteArray = baos.toByteArray();
        String s = new String(byteArray);
        Assert.assertTrue(s.length() > 0);

        //create file
        File file = new File(createTempFile("fileName"));
        try (OutputStream outputStream = new FileOutputStream(file.getPath())) {
            baos.writeTo(outputStream);
        }
        assertTrue(file.exists());

        //check file not empty
        BufferedReader br = new BufferedReader(new FileReader(file.getPath()));
        assertFalse(br.readLine() == null);
    }

    @Test
    public void testCreateTempFile() throws Exception {
        File file = new File(createTempFile("fileName"));
        assertTrue(file.exists());
    }

    @Test
    public void buildPDFDefault() throws Exception {
        buildPDF(defaultFileName);

    }

    @Test
    public void addSimpleContent() {
        String path = createTempFile("filename");
        doReturn(path).when(pdfBuilder).createTempFile(anyString());

        pdfBuilder.addSimpleContent(path);
    }

    @Test
    public void buildPDFWithColumns() throws Exception {
        buildPDF(columnsFileName);
    }

    @Test
    public void buildPDFWithBigContent() throws Exception {
        buildPDF(a4FileName);
    }

//    @Deprecated
//    @Test
//    public void buildRussianPdfFromFile() throws Exception {
//        //preparation
//        String path = createTempFile(rusFilename);
//        doReturn(path).when(pdfBuilder).createTempFile(anyString());
//
//        ArrayList<String> languages = new ArrayList<>();
//        languages.add("ru");
//        PdfBuilderInputData data = getTestData(rusFilename, languages);
//
//        pdfBuilder.buildPdfFile(data);
//
//        //check file exists
//        baseFileChecks(path);
//        assertTrue(pdfContainsText(path, "Барышня"));
//    }

    @Test
    public void buildRussianPdfFromStream() throws Exception {
        String path = createTempFile(rusFilename);

        //preparation
        ArrayList<String> languages = new ArrayList<>();
        languages.add("ru");
        PdfBuilderInputData data = getTestData(rusFilename, languages);

        pdfBuilder.buildPdfStream(data);

        ByteArrayOutputStream outputStream = pdfBuilder.buildPdfStream(data);
        FileUtils.writeByteArrayToFile(new File(path), outputStream.toByteArray());

        //check file exists
        baseFileChecks(path);
        assertTrue(pdfContainsText(path, "Барышня"));
    }

    @Test
    public void buildSimpleRussianPDF() throws IOException {
        buildTestPdf(simpleRusText);
    }

    @Test
    public void buildSimpleHindiPDF() throws IOException {
        buildTestPdf(simpleHindiText);
    }

    @Test
    public void buildSimpleUkrainianPDF() throws IOException {
        buildTestPdf(simpleUkrText);
    }

    @Test
    public void buildSimpleEngPDF() throws IOException {
        buildTestPdf(simpleEngText);
    }

    //china language not supported by the font - todo add more languages
//    @Test
//    public void buildSimpleChinaPDF() throws IOException {
//        buildTestPdf(simpleChinaText);
//    }

    @Test
    public void buildSimplePolishPDF() throws IOException {
        buildTestPdf(simplePlnText);
    }

    private void buildTestPdf(String text) throws IOException {
        //preparation
        String path = createTempFile(text);
        doReturn(path).when(pdfBuilder).createTempFile(anyString());

        List<TextUnit> texts = new ArrayList<>();
        texts.add(new TextUnit(text, 20, 200, 200, 20));
        PdfBuilderInputData data = new PdfBuilderInputData(300, 300, texts);

        pdfBuilder.buildPdfFile(data);
        baseFileChecks(path);
        assertTrue(pdfContainsText(path, text));
    }

    private void baseFileChecks(String path) throws IOException {
        //check file exists
        File pdf = new File(path);
        assertTrue(pdf.exists());

        //check file not empty
        BufferedReader br = new BufferedReader(new FileReader(path));
        assertFalse(br.readLine() == null);
    }

    private void buildPDF(String fileName) throws Exception {
        //preparation
        String path = createTempFile(fileName);
        doReturn(path).when(pdfBuilder).createTempFile(anyString());

        PdfBuilderInputData data = getTestData(fileName, null);

        pdfBuilder.buildPdfFile(data);

        baseFileChecks(path);
    }

    private PdfBuilderInputData getTestData(String fileName, @Nullable List<String> languages) throws Exception {

        String filePath = getTestFile(fileName);
        Path path = Paths.get(filePath);
        byte[] data = Files.readAllBytes(path);

        OCRProcessor processor = new OcrProcessorImpl();
        PdfBuilderInputData pdfData = null;
        if (languages == null || languages.size() == 0) {
            pdfData = processor.ocrForData(data);
        } else {
            pdfData = processor.ocrForData(data, languages);
        }
        return pdfData;
    }

    private String createTempFile(String fileName) {
        return pdfBuilder.createTempFile(fileName + ".pdf");
    }

    private String getTestFile(String filename) throws Exception {
        URL url = Thread.currentThread().getContextClassLoader().getResource("test_imgs/" + filename);
        assert url != null;
        return url.getPath();
    }

    private String getFontPath(String fontFileName) {
        URL url = Thread.currentThread().getContextClassLoader().getResource("fonts/" + fontFileName);
        assert url != null;
        return url.getPath();
    }

    /**
     * parse pdf and check does it contains text
     *
     * @param filePath path to pdf
     * @param text     text for checking
     * @return does contain
     * @throws IOException
     */
    private boolean pdfContainsText(String filePath, String text) throws IOException {
        PdfReader reader = new PdfReader(filePath);
        String allText = "";
        for (int page = 1; page <= 1; page++) {
            allText = PdfTextExtractor.getTextFromPage(reader, page);
        }
        return allText.contains(text);
    }
}