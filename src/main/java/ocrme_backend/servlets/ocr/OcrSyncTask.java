package ocrme_backend.servlets.ocr;

import ocrme_backend.file_builder.pdfbuilder.PdfBuilderInputData;
import ocrme_backend.ocr.OCRProcessor;
import ocrme_backend.ocr.OcrProcessorImpl;
import org.apache.commons.fileupload.FileUploadException;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by iuliia on 6/29/17.
 */
public class OcrSyncTask {
    private byte[] imageBytes;
    private final String[] languages;
    private static Logger logger;

    public OcrSyncTask(byte[] imageBytes, String[] languages) {
        this.imageBytes = imageBytes;
        this.languages = languages;
        logger = Logger.getLogger(OcrSyncTask.class.getName());
    }

    public OcrData execute() throws IOException, GeneralSecurityException {

        OcrData result = doStaff();
        logger.log(Level.INFO, "text result:" + result.getSimpleText());
        return result;
    }

    private OcrData doStaff() throws IOException, GeneralSecurityException {
        OcrData data;
        try {
            if (imageBytes == null) {
                throw new FileUploadException("Can not get file");
            }
            OCRProcessor processor = new OcrProcessorImpl();
            if (languages == null || languages.length <= 0) {//run without languages - auto language will be used
                data = processor.ocrForData(imageBytes);
            } else {
                data = processor.ocrForData(imageBytes, Arrays.asList(languages));
            }
        } catch (Exception e) {
            data = new OcrData(new PdfBuilderInputData(e.getMessage()), "");
            logger.log(Level.WARNING, "ERROR! See log below.");
            e.printStackTrace();
        }
        return data;
    }
}
