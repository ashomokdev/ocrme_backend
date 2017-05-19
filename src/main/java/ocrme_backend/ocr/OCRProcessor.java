package ocrme_backend.ocr;

import java.io.IOException;

/**
 * Created by iuliia on 5/17/17.
 */
public interface OCRProcessor {
    String doOCR(byte[] image) throws IOException;
}
