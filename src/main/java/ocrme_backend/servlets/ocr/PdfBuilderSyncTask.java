package ocrme_backend.servlets.ocr;

import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.parser.PdfTextExtractor;
import ocrme_backend.datastore.gcloud_storage.utils.CloudStorageHelper;
import ocrme_backend.datastore.gcloud_storage.utils.FileUtils;
import ocrme_backend.file_builder.pdfbuilder.PDFBuilder;
import ocrme_backend.file_builder.pdfbuilder.PDFBuilderImpl;
import ocrme_backend.file_builder.pdfbuilder.PdfBuilderInputData;
import ocrme_backend.file_builder.pdfbuilder.PdfBuilderOutputData;
import ocrme_backend.file_builder.pdfbuilder.PdfBuilderOutputData.Status;

import javax.servlet.ServletException;
import javax.servlet.http.HttpSession;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by iuliia on 7/13/17.
 * builds pdf and save in Google cloud storage
 */
public class PdfBuilderSyncTask {
    private static Logger logger;
    private HttpSession session;
    private PdfBuilderInputData data;
    public static final String BUCKET_FOR_PDFS_PARAMETER = "ocrme.bucket.pdf";

    public PdfBuilderSyncTask(PdfBuilderInputData data, HttpSession session) {
        this.data = data;
        this.session = session;
        logger = Logger.getLogger(PdfBuilderSyncTask.class.getName());
    }

    public PdfBuilderOutputData execute() {
        PdfBuilderOutputData result = new PdfBuilderOutputData();
        String url = null;
        try {
            url = buildPdf();
            result.setUrl(url);
            result.setStatus(Status.OK);
            logger.log(Level.INFO, "pdf generated, url for download: " + url);
        } catch (TextNotFoundException e) {
           result.setStatus(Status.PDF_CAN_NOT_BE_CREATED_EMPTY_DATA);
            logger.log(Level.INFO, "pdf not generated, empty data");
        } catch (LanguageNotSupportedException e) {
            result.setStatus(Status.PDF_CAN_NOT_BE_CREATED_LANGUAGE_NOT_SUPPORTED);
            logger.log(Level.INFO, "pdf not generated, language not supported");
        }
        return result;
    }

    private String buildPdf()
            throws TextNotFoundException, LanguageNotSupportedException {

        if (data.getText().size() == 0) {
            throw new TextNotFoundException();
        }

        PDFBuilder pdfBuilder = new PDFBuilderImpl(session);

        ByteArrayOutputStream outputStream = pdfBuilder.buildPdfStream(data);

        if (isFileEmpty(outputStream)) {
            throw new LanguageNotSupportedException();
        }

        String url = uploadToStorage(outputStream.toByteArray());
        return url;
    }

    private String uploadToStorage(byte[] file) {
        String fileName = "file.pdf";
        String url = "";
        try {
            CloudStorageHelper helper = new CloudStorageHelper();
            String bucketName = session.getServletContext().getInitParameter(BUCKET_FOR_PDFS_PARAMETER);
            helper.createBucket(bucketName);
            url = helper.uploadFile(file, fileName, bucketName);
        } catch (IOException | ServletException e) {
            e.printStackTrace();
        }
        return url;
    }

    /**
     * check if pdf file contains any text
     * @param outputStream
     * @return
     */
    private boolean isFileEmpty(ByteArrayOutputStream outputStream) {
        String allText = "";
        try {
            PdfReader reader = new PdfReader(outputStream.toByteArray());

            for (int page = 1; page <= 1; page++) {
                allText = PdfTextExtractor.getTextFromPage(reader, page);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return (allText == null) || allText.length() < 1;
    }

    public class TextNotFoundException extends Throwable {
    }

    public class LanguageNotSupportedException extends Throwable {
    }
}
