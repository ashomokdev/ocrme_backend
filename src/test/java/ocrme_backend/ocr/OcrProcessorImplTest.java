package ocrme_backend.ocr;

import ocrme_backend.datastore.utils.FileProvider;
import ocrme_backend.datastore.utils.ImageFile;
import ocrme_backend.file_builder.pdfbuilder.PdfBuilderInputData;
import ocrme_backend.file_builder.pdfbuilder.TextUnit;
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
        byte[] file = FileProvider.getImageFile().getFile();
        String result = ocrProcessor.ocrForText(file);
        assertNotNull(result);
        assertTrue(result.length() > 0);
    }

    @Test
    public void OCRRussian() throws Exception {
        byte[] file = FileProvider.getRusImageFile().getFile();
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
        PdfBuilderInputData data = ocrProcessor.ocrForData(file.getFile());
        assertTrue(data.getText().size() > 0);
    }

    @Test
    public void ocrForRussianData() throws Exception {
        ImageFile file = FileProvider.getRusImageFile();
        List<String> languages = new ArrayList<>();
        languages.add("ru");
        PdfBuilderInputData data = ocrProcessor.ocrForData(file.getFile(), languages);
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

}