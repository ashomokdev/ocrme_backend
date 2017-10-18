package ocrme_backend.ocr;

import com.google.api.services.vision.v1.model.BatchAnnotateImagesResponse;
import com.google.api.services.vision.v1.model.BoundingPoly;
import com.google.api.services.vision.v1.model.Vertex;
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

    //todo delete test
    @Test
    public void rotate() {
        BoundingPoly poly = new BoundingPoly();
        ArrayList<Vertex> vertices = new ArrayList<>();

        Vertex v0 = new Vertex();
        v0.setX(10);
        v0.setY(10);

        Vertex v1 = new Vertex();
        v1.setX(50);
        v1.setY(10);


        Vertex v2 = new Vertex();
        v2.setX(50);
        v2.setY(20);

        Vertex v3 = new Vertex();
        v3.setX(10);
        v3.setY(20);

        vertices.add(v0);
        vertices.add(v1);
        vertices.add(v2);
        vertices.add(v3);

        poly.setVertices(vertices);

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
        String imgUri = "gs://bucket-for-requests-test/2017-07-26-12-37-36-806-2017-07-26-12-37-36-806-ru.jpg";
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
                return;
            }
        }
        assertTrue(containsRus);
    }

}