package ocrme_backend.utils;

import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.parser.PdfTextExtractor;
import ocrme_backend.datastore.gcloud_storage.utils.CloudStorageHelper;

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
    public static String pathToSecretKeys = "secret_data/secret.properties";
    private static final String defaultFont = "FreeSans.ttf";

    public static String getTestImageFullPathByFileName(String filename) {
        URL url = Thread.currentThread().getContextClassLoader().getResource("test_imgs/" + filename);
        assert url != null;
        return url.getPath();
    }

    public static String getDefaultFont(){
        return defaultFont;
    }

    /**
     * get Google Cloud URI of test image, example gs://bucket-for-tests/2019-09-11-18-50-52-854-rus.jpg
     * @return Google Cloud URI of test image, example gs://bucket-for-tests/2019-09-11-18-50-52-854-rus.jpg
     */
    public static String uploadTestImageForGsUri(String fileName){
        String bucketName = System.getProperty("bucket-for-tests");
        return new CloudStorageHelper().uploadFileForUri(
                FileProvider.getFileAsInputStream("test_imgs/" + fileName), fileName, bucketName);
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
        return new ImageFile(data, sourceWidth, sourceHeight);
    }

    public static ByteArrayOutputStream getImageAsStream() throws IOException {
        return getFileAsyteArrayOutputStream("test_imgs/img.jpg");
    }

    public static ByteArrayOutputStream getBigImageAsStream() throws IOException {
        return getFileAsyteArrayOutputStream("test_imgs/big_image.jpg");
    }

    public static ByteArrayOutputStream getBlankImageAsStream() throws IOException {
        return getFileAsyteArrayOutputStream("test_imgs/blank.jpg");
    }

    public static ByteArrayOutputStream getSmallRuImageAsStream() throws IOException {
        return getFileAsyteArrayOutputStream("test_imgs/rus.jpg");
    }

    public static ByteArrayOutputStream getPdfAsStream() throws IOException {
        return getFileAsyteArrayOutputStream("pdfs/ru.pdf");
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

    public static InputStream getFontAsStream(String fontFileName) {
        return Thread.currentThread()
                .getContextClassLoader().getResourceAsStream("fonts/" + fontFileName);
    }

    private static ByteArrayOutputStream getFileAsyteArrayOutputStream(String filePath) throws IOException {
        URL url = Thread.currentThread().getContextClassLoader().getResource(filePath);
        File file = new File(url.getPath());
        Path path = Paths.get(file.getPath());
        byte[] bytes = Files.readAllBytes(path);

        ByteArrayOutputStream baos = new ByteArrayOutputStream(bytes.length);
        baos.write(bytes, 0, bytes.length);

        return baos;
    }

    public static InputStream getFileAsInputStream(String filePath) {
        return Thread.currentThread().getContextClassLoader().getResourceAsStream(filePath);
    }
}
