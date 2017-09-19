package ocrme_backend.ocr;

import ocrme_backend.servlets.ocr.OcrData;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.List;

/**
 * Created by iuliia on 5/17/17.
 */
public interface OCRProcessor {
    /**
     * perform ocr for retrieving text with auto language detection.
     * @param image
     * @return text
     * @throws IOException
     */
    String ocrForText(byte[] image) throws IOException;

    /**
     * perform ocr for retrieving text.
     * @param image
     * @param languages list of languages
     * @return text
     * @throws IOException
     */
    String ocrForText(byte[] image, @Nullable List<String> languages) throws IOException;

    /**
     * perform OCR for retrieving text data for generation pdf file or another formatted file.
     * With auto language detection.
     * @param image
     * @return data about text for generation pdf
     * @throws IOException
     */
    OcrData ocrForData(byte[] image) throws IOException;

    /**
     * perform OCR for retrieving text data for generation pdf file or another formatted file.
     * With auto language detection.
     * @param image
     * @param languages list of languages
     * @return data about text for generation pdf
     * @throws IOException
     */
    OcrData ocrForData(byte[] image, @Nullable List<String> languages) throws IOException;
}
