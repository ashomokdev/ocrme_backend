package ocrme_backend.datastore.utils;

import autovalue.shaded.org.apache.commons.lang.ArrayUtils;
import org.apache.commons.fileupload.FileItemIterator;
import org.apache.commons.fileupload.FileItemStream;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.mock.web.MockMultipartHttpServletRequest;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.Assert.assertTrue;

/**
 * Created by iuliia on 6/23/17.
 */
public class FileProvider {
    public static FileItemStream getItemStreamFile() throws Exception {

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

    private static byte[] createFileContent(byte[] data, String boundary, String contentType, String fileName) {
        String start = "--" + boundary + "\r\n Content-Disposition: form-data; name=\"file\"; filename=\"" + fileName + "\"\r\n"
                + "Content-type: " + contentType + "\r\n\r\n";

        String end = "\r\n--" + boundary + "--"; // correction suggested @butfly
        return ArrayUtils.addAll(start.getBytes(), ArrayUtils.addAll(data, end.getBytes()));
    }
}
