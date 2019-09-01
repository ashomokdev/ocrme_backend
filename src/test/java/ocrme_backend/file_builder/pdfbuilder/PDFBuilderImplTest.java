package ocrme_backend.file_builder.pdfbuilder;

import com.itextpdf.text.DocumentException;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.parser.PdfTextExtractor;
import ocrme_backend.servlets.ocr.OcrData;
import ocrme_backend.utils.FileProvider;
import org.junit.Before;
import org.junit.Test;

import javax.annotation.Nullable;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpSession;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static ocrme_backend.utils.FileProvider.getFontAsStream;
import static ocrme_backend.utils.FileProvider.getPathToTemp;
import static ocrme_backend.utils.PdfBuilderInputDataProvider.ocrForData;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created by iuliia on 6/4/17.
 */

public class PDFBuilderImplTest {

    private PDFBuilderImpl pdfBuilder;

    private String defaultFileName = "img.jpg";
    private String a4FileName = "a4.jpg";
    private String columnsFileName = "columns.png";
    private String columns_90 = "columns_90.png";
    private String columns_180 = "columns_180.png";
    private String columns_270 = "columns_270.png";
    private String rusFilename = "rus.jpg";
    private String cut_meFilename = "cut_me.jpg";
    private String cut_me_left_rightFilename = "cut_me_left_right.jpg";
    private String cut_me_rightFilename = "cut_me_right.jpg";
    private String cut_me_topFilename = "cut_me_top.jpg";
    private String cut_me_rotated_90 = "cut_me_rotated_90.jpg";
    private String cut_me2Filename = "cut_me2.jpg";

    private String image_rotated_90 = "image_rotated_90.jpg";
    private String rotated_180 = "rotated_180.jpg";
    private String rotated_ok = "rotated_ok.jpg";
    private String rotated_180_2 = "rotated_180_2.jpg";
    private String rotated_270 = "rotated_270.jpg";
    private String rotated_270_2 = "rotated_270_2.jpg";


    private String simpleRusText = "Простой русский текст";
    private String simpleUkrText = "Проста українська мова";
    private String simpleEngText = "Simple English text";
    private String simplePlnText = "Ocenia się że język polski jest językiem ojczystym około 44 milionów ludzi na świecie";
    private String simpleHindiText = "साधारण पाठ हिन्दी";
    private String simpleChinaText = "简单的文字中国"; //todo fix me - not supported yet
    ArrayList<String> imageLocalPathArray = new ArrayList<>();

    @Before
    public void init() {
        HttpSession session = mock(HttpSession.class);
        pdfBuilder = new PDFBuilderImpl(session);

        ServletContext mockServletContext = mock(ServletContext.class);
        when(session.getServletContext()).thenReturn(mockServletContext);

        String defaultFont = FileProvider.getDefaultFont();
        when(mockServletContext.getResourceAsStream(anyString())).thenReturn(getFontAsStream(defaultFont));
    }

//    @After
//    public void deleteFiles() {
//        for (String path : imageLocalPathArray) {
//            deleteFile(path);
//        }
//    }


    @Test
    public void buildPDFDefault() throws Exception {
        testBuildPdfFromRealData(defaultFileName, null);
    }

    @Test
    public void buildPDFsourseRotated90() throws Exception {
        testBuildPdfFromRealData(image_rotated_90, null);
        testBuildPdfFromRealData(columns_90, null);
        testBuildPdfFromRealData(cut_me_rotated_90, null);
    }

    @Test
    public void buildPDFsourseRotated180() throws Exception {
        testBuildPdfFromRealData(rotated_180_2, null);
        testBuildPdfFromRealData(rotated_180, null);
        testBuildPdfFromRealData(rotated_ok, null);
        testBuildPdfFromRealData(columns_180, null);

    }


    @Test
    public void buildPDFsourseRotated270() throws Exception {
        testBuildPdfFromRealData(rotated_270, null);
        testBuildPdfFromRealData(rotated_270_2, null);
        testBuildPdfFromRealData(columns_270, null);
    }

    @Test
    public void buildPDFWithColumns() throws Exception {
        testBuildPdfFromRealData(columnsFileName, null);
    }

    @Test
    public void buildPDFWithBigContent() throws Exception {
        testBuildPdfFromRealData(a4FileName, null);
    }

    @Test
    public void cutAndBuildPDF() throws Exception {
        //get all files with cut_me part
        testBuildPdfFromRealData(cut_meFilename, null);
        testBuildPdfFromRealData(cut_me_left_rightFilename, null);
        testBuildPdfFromRealData(cut_me_rightFilename, null);
        testBuildPdfFromRealData(cut_me_topFilename, null);
        testBuildPdfFromRealData(cut_me2Filename, null);
    }


    @Test
    public void buildRussianPdfFromStream() throws Exception {

        //preparation
        ArrayList<String> languages = new ArrayList<>();
        languages.add("ru");
        OcrData data = ocrForData(rusFilename, languages);

        ByteArrayOutputStream stream = pdfBuilder.buildPdfStream(data.getPdfBuilderInputData());

        String pdfFileName = "filename.pdf";
        File destination = new File(getPathToTemp(), pdfFileName);
        try (OutputStream outputStream = new FileOutputStream(destination)) {
            stream.writeTo(outputStream);
        }

        String destinationPath = destination.getPath();
        testFileExistsAndNotEmpty(destinationPath);
        assertTrue(pdfContainsText(destinationPath, "Барышня"));
        imageLocalPathArray.add(destinationPath);
    }

    @Test
    public void buildSimpleRussianPDF() throws IOException {
        testBuildSimplePdf(simpleRusText);
    }

    @Test
    public void buildSimpleHindiPDF() throws IOException {
        testBuildSimplePdf(simpleHindiText);
    }

    @Test
    public void buildSimpleUkrainianPDF() throws IOException {
        testBuildSimplePdf(simpleUkrText);
    }

    @Test
    public void buildSimpleEngPDF() throws IOException {
        testBuildSimplePdf(simpleEngText);
    }

    //china language not supported by the font - todo add more languages
//    @Test
//    public void buildSimpleChinaPDF() throws IOException {
//         testBuildSimplePdf(simpleChinaText);
//    }

    //
    @Test
    public void buildSimplePolishPDF() throws IOException {
        testBuildSimplePdf(simplePlnText);
    }

    /**
     * test building pdf with simple text setted as param
     *
     * @param text
     * @throws IOException
     */
    private void testBuildSimplePdf(String text) throws IOException {
        //preparation
        List<TextUnit> texts = new ArrayList<>();
        texts.add(new TextUnit(text, 20, 200, 200, 20));
        PdfBuilderInputData data = new PdfBuilderInputData(texts);

        String timeStamp = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss-SSS-").format(new Date());
        testBuildPdf(timeStamp + ".pdf", data);
    }

    /**
     * test build pdf from PdfBuilderInputData
     *
     * @param imageFileName
     * @param data
     * @throws IOException
     */
    private String testBuildPdf(String imageFileName, PdfBuilderInputData data) throws IOException {
        ByteArrayOutputStream stream = pdfBuilder.buildPdfStream(data);

        return saveFileForPath(imageFileName, stream);
    }

    /**
     * test build pdf from byte[]
     *
     * @param data
     * @throws IOException
     */
    private String testBuildPdf(String imageFileName, byte[] data) throws IOException, DocumentException {
        ByteArrayOutputStream stream = pdfBuilder.buildPdfStream(data);

        return saveFileForPath(imageFileName, stream);
    }

    private String saveFileForPath(String imageFileName, ByteArrayOutputStream stream) throws IOException {
        String pdfFileName = imageFileName + ".pdf";
        File destination = new File(getPathToTemp(), pdfFileName);
        try (OutputStream outputStream = new FileOutputStream(destination)) {
            stream.writeTo(outputStream);
        }
        String path = destination.getPath();

        testFileExistsAndNotEmpty(path);
        imageLocalPathArray.add(path);
        return path;
    }

    /**
     * test build pdf from real data obtained after ocr real image
     *
     * @param imageFileName
     * @throws Exception
     */
    private String testBuildPdfFromRealData(String imageFileName, @Nullable ArrayList<String> languages) throws Exception {
        //preparation
        OcrData data = ocrForData(imageFileName, languages);

        return testBuildPdf(imageFileName, data.getPdfBuilderInputData());
    }

    private void testFileExistsAndNotEmpty(String path) throws IOException {
        //check file exists
        File pdf = new File(path);
        assertTrue(pdf.exists());

        //check file not empty
        BufferedReader br = new BufferedReader(new FileReader(path));
        assertNotNull(br.readLine());
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

    @Test
    public void buildPdfStream() throws IOException, DocumentException {
        byte[] bytes = FileProvider.getImageAsStream().toByteArray();
        testBuildPdf("img.jpg", bytes);
    }
}