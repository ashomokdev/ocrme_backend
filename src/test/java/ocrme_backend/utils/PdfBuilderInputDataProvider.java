package ocrme_backend.utils;

import ocrme_backend.ocr.OCRProcessor;
import ocrme_backend.ocr.OcrProcessorImpl;
import ocrme_backend.servlets.ocr.OcrData;

import javax.annotation.Nullable;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

/**
 * Created by iuliia on 7/25/17.
 */
public class PdfBuilderInputDataProvider {

    public static OcrData ocrForData(String fileName, @Nullable List<String> languages) throws Exception {

        String filePath = FileProvider.getTestImageFullPathByFileName(fileName);
        Path path = Paths.get(filePath);
        byte[] imageBytes = Files.readAllBytes(path);

        OCRProcessor processor = new OcrProcessorImpl();
        OcrData pdfData = null;
        if (languages == null || languages.size() == 0) {
            pdfData = processor.ocrForData(imageBytes);
        } else {
            pdfData = processor.ocrForData(imageBytes, languages);
        }
        return pdfData;
    }
}
