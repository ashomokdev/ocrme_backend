package ocrme_backend.ocr;

import com.google.api.gax.grpc.ApiException;
import io.grpc.StatusRuntimeException;
import ocrme_backend.file_builder.pdfbuilder.PDFData;
import ocrme_backend.file_builder.pdfbuilder.TextUnit;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.List;

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

    /**
     * skip ApiException which occurs because of limits
     https://cloud.google.com/vision/docs/limits
     Requests per second	10
     */
    @Rule
    public TestRule skipRule = new TestRule() {
        public Statement apply(final Statement base, Description desc) {

            return new Statement() {
                public void evaluate() throws Throwable {
                    try {
                        base.evaluate();
                    } catch (ApiException ex) {
                        Assume.assumeTrue(true);
                    }
                }
            };
        }
    };

    @Test
    public void doOCR() throws Exception {
        byte[] file = getFile().getFile();
        String result = ocrProcessor.ocrForText(file);
        assertNotNull(result);
        assertTrue(result.length() > 0);
    }

    @Test
    public void OCRRussian() throws Exception {
        byte[] file = getRusFile().getFile();
        ArrayList<String> languages = new ArrayList<>();
        languages.add("ru");
        String result = ocrProcessor.ocrForText(file, languages);
        assertNotNull(result);
        assertTrue(result.length() > 0);
        assertTrue(result.toLowerCase().contains("барышня"));
    }

    @Test
    public void ocrForData() throws Exception {
        ImageFile file = getFile();
        PDFData data = ocrProcessor.ocrForData(file.getFile(), file.getHeight(), file.getWidth());
        assertTrue(data.getText().size() > 0);
    }

    @Test
    public void ocrForRussianData() throws Exception {
        ImageFile file = getRusFile();
        List<String> languages = new ArrayList<>();
        languages.add("ru");
        PDFData data = ocrProcessor.ocrForData(file.getFile(), file.getHeight(), file.getWidth(), languages);
        List<TextUnit> text = data.getText();
        assertTrue(text.size() > 0);


        boolean containsRus = false;
        for (TextUnit unit : text) {
            if (unit.getText().toLowerCase().contains("барышня")) {
                containsRus = true;
                return;
            }
        }
        assertTrue(containsRus);
    }


    private ImageFile getRusFile() throws Exception {
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

    private ImageFile getFile() throws Exception {
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
}