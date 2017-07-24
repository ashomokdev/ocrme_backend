package ocrme_backend.datastore.utils;

import autovalue.shaded.org.apache.commons.lang.ArrayUtils;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.parser.PdfTextExtractor;
import ocrme_backend.file_builder.pdfbuilder.PDFBuilderImpl;
import ocrme_backend.file_builder.pdfbuilder.PdfBuilderInputData;
import ocrme_backend.file_builder.pdfbuilder.TextUnit;
import org.apache.commons.fileupload.FileItemIterator;
import org.apache.commons.fileupload.FileItemStream;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.mock.web.MockMultipartHttpServletRequest;

import javax.imageio.ImageIO;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpSession;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import static ocrme_backend.file_builder.pdfbuilder.PDFBuilderImpl.FONT_PATH_PARAMETER;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.when;

/**
 * Created by iuliia on 6/23/17.
 */
public class FileProvider {
    public static FileItemStream getItemStreamImageFile() throws Exception {

        URL url = Thread.currentThread().getContextClassLoader().getResource("test_imgs/img.jpg");
        File file = new File(url.getPath());
        Path path = Paths.get(file.getPath());
        byte[] data = Files.readAllBytes(path);

        ServletFileUpload upload = new ServletFileUpload(new DiskFileItemFactory());

        MockMultipartHttpServletRequest req = new MockMultipartHttpServletRequest();
        String boundary = "q1w2e3r4t5y6u7i8o9";
        req.setContentType("multipart/form-data; boundary=" + boundary);
        req.setContent(createFileContent(data, boundary, "image/jpeg", "img.jpg"));

        MockMultipartFile mockMultipartFile =
                new MockMultipartFile("img.jpg", "img.jpg", "image/jpeg", data);
        req.addFile(mockMultipartFile);

        assertTrue(ServletFileUpload.isMultipartContent(req));

        FileItemIterator it = upload.getItemIterator(req);
        assertTrue(it.hasNext());
        FileItemStream item = it.next();
        return item;
    }

    public static FileItemStream getRusItemStreamImageFile() throws Exception {

        URL url = Thread.currentThread().getContextClassLoader().getResource("test_imgs/rus.jpg");
        File file = new File(url.getPath());
        Path path = Paths.get(file.getPath());
        byte[] data = Files.readAllBytes(path);

        ServletFileUpload upload = new ServletFileUpload(new DiskFileItemFactory());

        MockMultipartHttpServletRequest req = new MockMultipartHttpServletRequest();
        String boundary = "q1w2e3r4t5y6u7i8o9";
        req.setContentType("multipart/form-data; boundary=" + boundary);
        req.setContent(createFileContent(data, boundary, "image/jpeg", "rus.jpg"));

        MockMultipartFile mockMultipartFile =
                new MockMultipartFile("rus.jpg", "rus.jpg", "image/jpeg", data);
        req.addFile(mockMultipartFile);

        assertTrue(ServletFileUpload.isMultipartContent(req));

        FileItemIterator it = upload.getItemIterator(req);
        assertTrue(it.hasNext());
        FileItemStream item = it.next();
        return item;
    }

    public static Path getPathFile() throws Exception {
        URL url = Thread.currentThread().getContextClassLoader().getResource("test_imgs/img.jpg");
        File file = new File(url.getPath());
        Path path = Paths.get(file.getPath());
        return path;
    }

    public static ImageFile getRusImageFile() throws Exception {
        URL url = Thread.currentThread().getContextClassLoader().getResource("test_imgs/rus.jpg");
        File file = new File(url.getPath());
        Path path = Paths.get(file.getPath());
        BufferedImage bimg = ImageIO.read(new File(file.getPath()));
        int sourceWidth = bimg.getWidth();
        int sourceHeight = bimg.getHeight();
        byte[] data = Files.readAllBytes(path);
        ImageFile image = null;
        if (data != null) {
            image = new ImageFile(data, sourceWidth, sourceHeight);
        } else {
            throw new Exception("file was not obtained");
        }
        return image;
    }

    public static ImageFile getImageFile() throws Exception {
        URL url = Thread.currentThread().getContextClassLoader().getResource("test_imgs/img.jpg");
        File file = new File(url.getPath());
        Path path = Paths.get(file.getPath());
        BufferedImage bimg = ImageIO.read(new File(file.getPath()));
        int sourceWidth = bimg.getWidth();
        int sourceHeight = bimg.getHeight();
        byte[] data = Files.readAllBytes(path);
        ImageFile image = null;
        if (data != null) {
            image = new ImageFile(data, sourceWidth, sourceHeight);
        } else {
            throw new Exception("file was not obtained");
        }
        return image;
    }

    public static ByteArrayOutputStream getOutputStream() throws IOException {
        URL url = Thread.currentThread().getContextClassLoader().getResource("test_imgs/img.jpg");
        File file = new File(url.getPath());
        Path path = Paths.get(file.getPath());
        byte[] bytes = Files.readAllBytes(path);

        ByteArrayOutputStream baos = new ByteArrayOutputStream(bytes.length);
        baos.write(bytes, 0, bytes.length);

        return baos;
    }

    /**
     * returns simple pdf file with russian text, saved locally
     * @return
     */
    public static String getRusPdfFile() throws IOException {
        String simpleRusText = "Простой русский текст";
        return getRusPdfAsFilePath(simpleRusText);
    }

    /**
     * returns simple pdf file with russian text, saved locally
     * @return
     */
    public static ByteArrayOutputStream getRusPdfFileStream() throws IOException {
        String simpleRusText = "Простой русский текст";
        return getRusPdfAsStream(simpleRusText);
    }


    private static byte[] createFileContent(byte[] data, String boundary, String contentType, String fileName) {
        String start = "--" + boundary + "\r\n Content-Disposition: form-data; name=\"file\"; filename=\"" + fileName + "\"\r\n"
                + "Content-type: " + contentType + "\r\n\r\n";

        String end = "\r\n--" + boundary + "--"; // correction suggested @butfly
        return ArrayUtils.addAll(start.getBytes(), ArrayUtils.addAll(data, end.getBytes()));
    }

    private static ByteArrayOutputStream getRusPdfAsStream(String text) throws IOException {

        //preparation
        HttpSession session = mock(HttpSession.class);
        PDFBuilderImpl pdfBuilder = spy(new PDFBuilderImpl(session));

        ServletContext mockServletContext = mock(ServletContext.class);
        when(session.getServletContext()).thenReturn(mockServletContext);


        String defaultFont = "FreeSans.ttf";
        when(mockServletContext.getInitParameter(FONT_PATH_PARAMETER)).thenReturn(getFontPath(defaultFont));

        when(session.getId()).thenReturn("0");


        List<TextUnit> texts = new ArrayList<>();
        texts.add(new TextUnit(text, 20, 200, 200, 20));
        PdfBuilderInputData data = new PdfBuilderInputData(300, 300, texts);

        ByteArrayOutputStream outputStream = pdfBuilder.buildPdfStream(data);
        return outputStream;
    }

    private static String getRusPdfAsFilePath(String text) throws IOException {
        //preparation
        HttpSession session = mock(HttpSession.class);
        PDFBuilderImpl pdfBuilder = spy(new PDFBuilderImpl(session));

        ServletContext mockServletContext = mock(ServletContext.class);
        when(session.getServletContext()).thenReturn(mockServletContext);


       String defaultFont = "FreeSans.ttf";
        when(mockServletContext.getInitParameter(FONT_PATH_PARAMETER)).thenReturn(getFontPath(defaultFont));

        when(session.getId()).thenReturn("0");

        String uploadsDirPath = Thread.currentThread().getContextClassLoader().getResource("temp/").getPath();
        when(mockServletContext.getRealPath(PDFBuilderImpl.uploadsDir)).
                thenReturn(uploadsDirPath);

        String path = createTempFile(pdfBuilder, "rus");
        doReturn(path).when(pdfBuilder).createTempFile(anyString());

        List<TextUnit> texts = new ArrayList<>();
        texts.add(new TextUnit(text, 20, 200, 200, 20));
        PdfBuilderInputData data = new PdfBuilderInputData(300, 300, texts);

        pdfBuilder.buildPdfFile(data);
        baseFileChecks(path);
        assertTrue(pdfContainsText(path, text));
        return path;
    }

    private static String createTempFile(PDFBuilderImpl pdfBuilder, String fileName) {
        return pdfBuilder.createTempFile(fileName + ".pdf");
    }

    private static String getFontPath(String fontFileName) {
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
    private static boolean pdfContainsText(String filePath, String text) throws IOException {
        PdfReader reader = new PdfReader(filePath);
        String allText = "";
        for (int page = 1; page <= 1; page++) {
            allText = PdfTextExtractor.getTextFromPage(reader, page);
        }
        return allText.contains(text);
    }

    private static void baseFileChecks(String path) throws IOException {
        //check file exists
        File pdf = new File(path);
        assertTrue(pdf.exists());

        //check file not empty
        BufferedReader br = new BufferedReader(new FileReader(path));
        assertFalse(br.readLine() == null);
    }
}
