package ocrme_backend.file_builder.pdfbuilder;

import ocrme_backend.ocr.OCRProcessor;

import ocrme_backend.ocr.OcrProcessorImpl;
import org.junit.Before;
import org.junit.Test;

import javax.imageio.ImageIO;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.GeneralSecurityException;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Created by iuliia on 6/4/17.
 */
public class PDFBuilderImplTest {

    private PDFBuilderImpl pdfBuilder;
    private HttpServletRequest request;
    String defaultFileName = "img.jpg";
    String a4FileName = "a4.jpg";
    String columnsFileName = "columns.png";
    @Before
    public void init() throws IOException, GeneralSecurityException {
        request = mock(HttpServletRequest.class);
        pdfBuilder = spy(new PDFBuilderImpl(request));
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
    public void buildPDFWithColumns() throws Exception {
        buildPDF(columnsFileName);
    }

    @Test
    public void buildPDFWithBigContent() throws Exception {
        buildPDF(a4FileName);
    }

    @Test
    public void buildRussianPDF() throws Exception {
        //todo
    }

    @Test
    public void buildSmallPDF() throws Exception {
       //todo
    }


    @Test
    public void addSimpleContent() {
        String path = createTempFile("filename");
        doReturn(path).when(pdfBuilder).createTempFile(anyString());

        pdfBuilder.addSimpleContent(path);
    }

    private void buildPDF(String fileName) throws Exception {
        //preparation
        HttpSession mockHttpSession = mock(HttpSession.class);
        when(request.getSession()).
                thenReturn(mockHttpSession);
        when(mockHttpSession.getId()).thenReturn("0");

        String path = createTempFile(fileName);
        doReturn(path).when(pdfBuilder).createTempFile(anyString());

        PDFData data = getTestData(fileName);

        pdfBuilder.buildPDF(data);

        //check file exists
        File pdf = new File(path);
        assertTrue(pdf.exists());

        //check file not empty
        BufferedReader br = new BufferedReader(new FileReader(path));
        assertFalse(br.readLine() == null);
    }


    private PDFData getTestData(String fileName) throws Exception {
        String filePath = getTestFile(fileName);
        Path path = Paths.get(filePath);
        byte[] data = Files.readAllBytes(path);

        OCRProcessor processor = new OcrProcessorImpl();
        return processor.ocrForData(data);
    }

    private String createTempFile(String fileName) {
        String path = Thread.currentThread().getContextClassLoader().getResource("temp/").getPath();

        ServletContext mockServletContext = mock(ServletContext.class);
        when(mockServletContext.getRealPath(PDFBuilderImpl.uploadsDir)).
                thenReturn(path);

        HttpSession mockHttpSession = mock(HttpSession.class);
        when(mockHttpSession.getServletContext()).thenReturn(mockServletContext);

        when(request.getSession()).
                thenReturn(mockHttpSession);

        return pdfBuilder.createTempFile(fileName +".pdf");
    }

    private String getTestFile(String filename) throws Exception {
        URL url = Thread.currentThread().getContextClassLoader().getResource("test_imgs/" + filename);
        return url.getPath();
    }
}