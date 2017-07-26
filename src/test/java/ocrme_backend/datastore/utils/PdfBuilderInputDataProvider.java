package ocrme_backend.datastore.utils;

import ocrme_backend.file_builder.pdfbuilder.PdfBuilderInputData;
import ocrme_backend.ocr.OCRProcessor;
import ocrme_backend.ocr.OcrProcessorImpl;

import javax.annotation.Nullable;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

/**
 * Created by iuliia on 7/25/17.
 */
public class PdfBuilderInputDataProvider {

    public static PdfBuilderInputData ocrForData(String fileName, @Nullable List<String> languages) throws Exception {

        String filePath = FileProvider.getTestImageByName(fileName);
        Path path = Paths.get(filePath);
        byte[] imageBytes = Files.readAllBytes(path);

        OCRProcessor processor = new OcrProcessorImpl();
        PdfBuilderInputData pdfData = null;
        if (languages == null || languages.size() == 0) {
            pdfData = processor.ocrForData(imageBytes);
        } else {
            pdfData = processor.ocrForData(imageBytes, languages);
        }
        return pdfData;
    }
}
