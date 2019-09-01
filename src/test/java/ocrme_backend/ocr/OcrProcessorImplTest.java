package ocrme_backend.ocr;

import com.google.api.services.vision.v1.model.BatchAnnotateImagesResponse;
import ocrme_backend.file_builder.pdfbuilder.TextUnit;
import ocrme_backend.servlets.ocr.OcrData;
import ocrme_backend.utils.FileProvider;
import ocrme_backend.utils.ImageFile;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Created by iuliia on 7/11/17.
 */
public class OcrProcessorImplTest {
    private OcrProcessorImpl ocrProcessor;

    @Before
    public void init() throws IOException, GeneralSecurityException {
        ocrProcessor = new OcrProcessorImpl();
    }

    @Test
    public void doOCR() throws Exception {
        byte[] file = FileProvider.getImageFile().getImageBytes();
        String result = ocrProcessor.ocrForText(file);
        assertNotNull(result);
        assertTrue(result.length() > 0);
    }

    @Test
    public void OCRRussian() throws Exception {
        byte[] file = FileProvider.getRusImageFile().getImageBytes();
        ArrayList<String> languages = new ArrayList<>();
        languages.add("ru");
        String result = ocrProcessor.ocrForText(file, languages);
        assertNotNull(result);
        assertTrue(result.length() > 0);
        assertTrue(result.toLowerCase().contains("барышня"));
    }

    @Test
    public void ocrForData() throws Exception {
        ImageFile file = FileProvider.getImageFile();
        OcrData data = ocrProcessor.ocrForData(file.getImageBytes());
        assertTrue(data.getPdfBuilderInputData().getText().size() > 0);
    }

    @Test
    public void ocrFromUri() throws Exception {
        String imgUri = FileProvider.getImageUri();
        BatchAnnotateImagesResponse data = ocrProcessor.ocrForResponse(imgUri, null);
        assertTrue(ocrProcessor.extractData(data).size() > 0);
    }

    @Test
    public void ocrForRussianData() throws Exception {
        ImageFile file = FileProvider.getRusImageFile();
        List<String> languages = new ArrayList<>();
        languages.add("ru");
        OcrData data = ocrProcessor.ocrForData(file.getImageBytes(), languages);
        List<TextUnit> text = data.getPdfBuilderInputData().getText();
        assertTrue(text.size() > 0);

        boolean containsRus = false;
        for (TextUnit unit : text) {
            if (unit.getText().toLowerCase().contains("барышня")) {
                containsRus = true;
            }
        }
        assertTrue(containsRus);
    }
}