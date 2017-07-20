package ocrme_backend.servlets.ocr;

import ocrme_backend.file_builder.pdfbuilder.PdfBuilderInputData;
import ocrme_backend.ocr.OCRProcessor;
import ocrme_backend.ocr.OcrProcessorImpl;
import org.apache.commons.fileupload.FileItemIterator;
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
public class OcrCallableTask implements Callable<PdfBuilderInputData> {
    private final FileItemIterator fileItemIterator;
    private final String[] languages;
    private static Logger logger;

    public OcrCallableTask(FileItemIterator fileItemIterator, String[] languages) {
        this.fileItemIterator = fileItemIterator;
        this.languages = languages;
        logger = Logger.getLogger(OcrCallableTask.class.getName());
    }

    @Override
    public PdfBuilderInputData call() throws Exception {
        PdfBuilderInputData result = doStaff(fileItemIterator, languages);
        logger.log(Level.INFO, "text result:" + result.getSimpleText());
        return result;
    }

    private byte[] convertToBytes(FileItemIterator it) throws IOException, FileUploadException {
        byte[] bytes = null;
        while (it.hasNext()) {
            FileItemStream item = it.next();
            String fieldName = item.getFieldName();
            InputStream fieldValue = item.openStream();

            if ("file".equals(fieldName)) {
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                Streams.copy(fieldValue, out, true);
                bytes = out.toByteArray();
            }
        }
        if (bytes == null) {
            throw new FileUploadException("Can not get file");
        }
        return bytes;
    }

    private PdfBuilderInputData doStaff(FileItemIterator fileItemIterator, String[] languages) throws IOException, GeneralSecurityException {
        PdfBuilderInputData data;
        try {
            byte[] bytes = convertToBytes(fileItemIterator);

            OCRProcessor processor = new OcrProcessorImpl();
            if (languages == null || languages.length <= 0) {//run without languages - auto language will be used
                data = processor.ocrForData(bytes);
            } else {
                data = processor.ocrForData(bytes, Arrays.asList(languages));
            }
        } catch (Exception e) {
            data = new PdfBuilderInputData(e.getMessage());
            logger.log(Level.WARNING, "ERROR! See log below.");
            e.printStackTrace();
        }
        return data;
    }
}
