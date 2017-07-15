package ocrme_backend.servlets.ocr;

import ocrme_backend.file_builder.pdfbuilder.PDFData;
import ocrme_backend.ocr.OCRProcessor;
import ocrme_backend.ocr.OcrProcessorImpl;
import org.apache.commons.fileupload.FileItemStream;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.util.Streams;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.util.Arrays;
import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by iuliia on 6/29/17.
 */
public class OcrCallableTask implements Callable<PDFData> {
    private final FileItemStream fileItemStream;
    private final String[] languages;
    private static Logger logger;

    public OcrCallableTask(FileItemStream fileItemStream, String[] languages) {
        this.fileItemStream = fileItemStream;
        this.languages = languages;
        logger = Logger.getLogger(OcrCallableTask.class.getName());
    }

    @Override
    public PDFData call() throws Exception {
        PDFData result = doStaff(fileItemStream, languages);
        logger.log(Level.INFO, "text result:" + result.getSimpleText());
        return result;
    }

    private byte[] convertToBytes(FileItemStream item) throws IOException, FileUploadException {
        byte[] bytes = null;
        String fieldName = item.getFieldName();
        InputStream fieldValue = item.openStream();

        if ("file".equals(fieldName)) {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            Streams.copy(fieldValue, out, true);
            bytes = out.toByteArray();
        }
        if (bytes == null) {
            throw new FileUploadException("File can not be obtained.");
        }
        return bytes;
    }

    private PDFData doStaff(FileItemStream fileItemStream, String[] languages) throws IOException, GeneralSecurityException {
        PDFData data;
        try {
            byte[] bytes = convertToBytes(fileItemStream);

            OCRProcessor processor = new OcrProcessorImpl();
            if (languages == null || languages.length <= 0) {//run without languages - auto language will be used
                data = processor.ocrForData(bytes);
            } else {
                data = processor.ocrForData(bytes, Arrays.asList(languages));
            }
        } catch (Exception e) {
            data = new PDFData(e.getMessage());
            logger.log(Level.WARNING, "ERROR! See log below.");
            e.printStackTrace();
        }
        return data;
    }
}
