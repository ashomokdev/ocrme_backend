package ocrme_backend.ocr;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.List;

/**
 * Created by iuliia on 5/17/17.
 */
public interface OCRProcessor {
    String doOCR(byte[] image) throws IOException;

    String doOCR(byte[] image, @Nullable List<String> languages) throws IOException;
}
