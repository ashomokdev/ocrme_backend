package ocrme_backend.utils;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import java.io.*;
import java.util.Iterator;

public class FileUtils {

    public static boolean canReadFile(File f) throws IOException {
        ImageInputStream iis = ImageIO.createImageInputStream(f);
        Iterator<ImageReader> readers = ImageIO.getImageReadersByFormatName("jpg");
        boolean canRead = false;
        if (readers.hasNext()) {
            ImageReader reader = readers.next();
            reader.setInput(iis);
            reader.read(0);
            canRead = true;
        }
        return canRead;
    }

    public static InputStream toInputStream(ByteArrayOutputStream baos) {
        byte[] array = baos.toByteArray();
        return new ByteArrayInputStream(array);
    }
}
