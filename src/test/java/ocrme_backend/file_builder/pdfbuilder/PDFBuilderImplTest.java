package ocrme_backend.file_builder.pdfbuilder;

import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.parser.PdfTextExtractor;
import ocrme_backend.servlets.ocr.OcrData;
import org.junit.Before;
import org.junit.Test;

import javax.annotation.Nullable;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpSession;
import java.io.*;
import java.security.GeneralSecurityException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static ocrme_backend.utils.FileProvider.*;
import static ocrme_backend.utils.PdfBuilderInputDataProvider.ocrForData;
import static org.junit.Assert.assertFalse;
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
    private String rusFilename = "rus.jpg";
    private String cut_meFilename = "cut_me.jpg";
    private String cut_me_left_rightFilename = "cut_me_left_right.jpg";
    private String cut_me_rightFilename = "cut_me_right.jpg";
    private String cut_me_topFilename = "cut_me_top.jpg";
    private String cut_me_top2Filename = "cut_me_top2.jpg";

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
        pdfBuilder = new PDFBuilderImpl(session);

        ServletContext mockServletContext = mock(ServletContext.class);
        when(session.getServletContext()).thenReturn(mockServletContext);

        when(mockServletContext.getResourceAsStream(anyString())).thenReturn(getFontAsStream(defaultFont));
    }


    @Test
    public void buildPDFDefault() throws Exception {
        testBuildPdfFromRealData(defaultFileName, null);
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
        //preparation
//        OcrData cut_me_data = ocrForData(cut_meFilename, null);
//        OcrData cut_me_left_right = ocrForData(cut_me_left_rightFilename, null);
//        OcrData cut_me_right = ocrForData(cut_me_rightFilename, null);
        OcrData cut_me_top = ocrForData(cut_me_topFilename, null);
//        OcrData cut_me_top2 = ocrForData(cut_me_top2Filename, null);

        ArrayList<OcrData> ocrDataList = new ArrayList<>();
//        ocrDataList.add(cut_me_data);
//        ocrDataList.add(cut_me_left_right);
//        ocrDataList.add(cut_me_right);
        ocrDataList.add(cut_me_top);
//        ocrDataList.add(cut_me_top2);

        ArrayList<String> pathList = new ArrayList<>();

        for (OcrData data : ocrDataList) {
            ByteArrayOutputStream stream = pdfBuilder.buildPdfStream(data.getPdfBuilderInputData());

            String timeStamp = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss-SSS-").format(new Date());
            String pdfFileName = timeStamp + "filename.pdf";
            File destination = new File(getPathToTemp(), pdfFileName);
            try (OutputStream outputStream = new FileOutputStream(destination)) {
                stream.writeTo(outputStream);
            }

            String path = destination.getPath();
            testFileExistsAndNotEmpty(path);
            pathList.add(path);
        }
        for (String path : pathList) {
            deleteFile(path);
        }
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

        deleteFile(destinationPath);
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
        PdfBuilderInputData data = new PdfBuilderInputData(300, 300, texts);

        testBuildPdf(data);
    }

    /**
     * test build pdf from PdfBuilderInputData
     *
     * @param data
     * @throws IOException
     */
    private void testBuildPdf(PdfBuilderInputData data) throws IOException {
        ByteArrayOutputStream stream = pdfBuilder.buildPdfStream(data);

        String pdfFileName = "filename.pdf";
        File destination = new File(getPathToTemp(), pdfFileName);
        try (OutputStream outputStream = new FileOutputStream(destination)) {
            stream.writeTo(outputStream);
        }

        testFileExistsAndNotEmpty(destination.getPath());

        deleteFile(destination.getPath());
    }

    /**
     * test build pdf from real data obtained after ocr real image
     *
     * @param imageFileName
     * @throws Exception
     */
    private void testBuildPdfFromRealData(String imageFileName, @Nullable ArrayList<String> languages) throws Exception {
        //preparation
        OcrData data = ocrForData(imageFileName, languages);

        testBuildPdf(data.getPdfBuilderInputData());
    }

    private void testFileExistsAndNotEmpty(String path) throws IOException {
        //check file exists
        File pdf = new File(path);
        assertTrue(pdf.exists());

        //check file not empty
        BufferedReader br = new BufferedReader(new FileReader(path));
        assertFalse(br.readLine() == null);
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