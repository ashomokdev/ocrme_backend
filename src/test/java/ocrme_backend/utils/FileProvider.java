package ocrme_backend.utils;

import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.parser.PdfTextExtractor;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Created by iuliia on 6/23/17.
 */
public class FileProvider {

    public static String getTestImageByName(String filename) throws Exception {
        URL url = Thread.currentThread().getContextClassLoader().getResource("test_imgs/" + filename);
        assert url != null;
        return url.getPath();
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

    public static ByteArrayOutputStream getImageAsStream() throws IOException {
        return getFileAsStream("test_imgs/img.jpg");
    }

    public static ByteArrayOutputStream getPdfAsStream() throws IOException {
        return getFileAsStream("pdfs/ru.pdf");
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

    public static void deleteFile(String path) {
        File file = new File(path);
        file.delete();
    }

    public static String getPathToTemp() {
        return Thread.currentThread().getContextClassLoader().getResource("temp/").getPath();
    }

    public static InputStream getFontAsStream(String fontFileName) throws IOException {
        return Thread.currentThread()
                .getContextClassLoader().getResourceAsStream("fonts/" + fontFileName);
    }

    private static ByteArrayOutputStream getFileAsStream(String filePath) throws IOException {
        URL url = Thread.currentThread().getContextClassLoader().getResource(filePath);
        File file = new File(url.getPath());
        Path path = Paths.get(file.getPath());
        byte[] bytes = Files.readAllBytes(path);

        ByteArrayOutputStream baos = new ByteArrayOutputStream(bytes.length);
        baos.write(bytes, 0, bytes.length);

        return baos;
    }
}
