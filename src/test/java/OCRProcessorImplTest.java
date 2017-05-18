import ocrme_backend.OCRProcessorImpl;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.GeneralSecurityException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Created by iuliia on 5/18/17.
 */
public class OCRProcessorImplTest {

    OCRProcessorImpl ocrProcessor;

    @Before
    public void init() throws IOException, GeneralSecurityException {
        ocrProcessor = new OCRProcessorImpl();
    }

    @Test
    public void doOCR() throws Exception {
        byte[] file = getFile();
        String result = ocrProcessor.doOCR(file);
        assertNotNull(result);
        assertTrue(result.length()>0);
    }

    private byte[] getFile() throws Exception {
        URL url = Thread.currentThread().getContextClassLoader().getResource("test_imgs/img.jpg");
        File file = new File(url.getPath());
        Path path = Paths.get(file.getPath());
        byte[] data = Files.readAllBytes(path);
        if (data != null) {
            return data;
        } else {
            throw new Exception("file was not obtained");
        }
    }

    @Test
    public void testAddition() {
        assertEquals(4, 2 + 2);
    }
}