package ocrme_backend.ocr;

import ocrme_backend.ocr.OCRProcessorImpl;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.GeneralSecurityException;
import java.util.ArrayList;

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

    @Test
    public void OCRRussian() throws Exception {
        byte[] file = getRusFile();
        ArrayList<String> languages = new ArrayList<>();
        languages.add("ru");
        String result = ocrProcessor.doOCR(file, languages);
        assertNotNull(result);
        assertTrue(result.length()>0);
        assertTrue(result.toLowerCase().contains("барышня"));
    }

    private byte[] getRusFile() throws Exception{
        URL url = Thread.currentThread().getContextClassLoader().getResource("test_imgs/rus.jpg");
        File file = new File(url.getPath());
        Path path = Paths.get(file.getPath());
        byte[] data = Files.readAllBytes(path);
        if (data != null) {
            return data;
        } else {
            throw new Exception("file was not obtained");
        }
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