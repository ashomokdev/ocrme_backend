package ocrme_backend.servlets.ocr;

import ocrme_backend.ocr.OCRProcessor;
import ocrme_backend.ocr.OCRProcessorImpl;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Arrays;
import java.util.concurrent.Callable;

/**
 * Created by iuliia on 6/29/17.
 */
public class OcrCallableTask implements Callable<String>{
    private final byte[] bytes;
    private final String[] languages;

    public OcrCallableTask(byte[] bytes, String[] languages) {
        this.bytes = bytes;
        this.languages = languages;

    }

    private String doStaff(byte[] bytes, String[] languages) throws IOException, GeneralSecurityException {
        String jsonResult;

        OCRProcessor processor = new OCRProcessorImpl();
        if (languages == null || languages.length <= 0) //run without languages - auto language will be used
        {
            jsonResult = processor.ocrForText(bytes);
        } else {
            jsonResult = processor.ocrForText(bytes, Arrays.asList(languages));
        }
        return jsonResult;
    }

    @Override
    public String call() throws Exception {
        return doStaff(bytes, languages);
    }
}
