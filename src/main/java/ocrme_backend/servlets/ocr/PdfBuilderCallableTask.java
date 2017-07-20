package ocrme_backend.servlets.ocr;

import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.parser.PdfTextExtractor;
import ocrme_backend.datastore.gcloud_storage.utils.CloudStorageHelper;
import ocrme_backend.file_builder.pdfbuilder.PDFBuilder;
import ocrme_backend.file_builder.pdfbuilder.PDFBuilderImpl;
import ocrme_backend.file_builder.pdfbuilder.PdfBuilderInputData;
import ocrme_backend.file_builder.pdfbuilder.PdfBuilderOutputData;

import javax.servlet.ServletException;
import javax.servlet.http.HttpSession;
import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.logging.Logger;

import ocrme_backend.file_builder.pdfbuilder.PdfBuilderOutputData.Status;

/**
 * Created by iuliia on 7/13/17.
 * builds pdf and save in Google cloud storage
 */
public class PdfBuilderCallableTask implements Callable<PdfBuilderOutputData> {
    private static Logger logger;
    private HttpSession session;
    private PdfBuilderInputData data;
    public static final String BUCKET_FOR_PDFS_PARAMETER = "ocrme.bucket.pdf";

    public PdfBuilderCallableTask(PdfBuilderInputData data, HttpSession session) {
        this.data = data;
        this.session = session;
        logger = Logger.getLogger(PdfBuilderCallableTask.class.getName());
    }


    @Override
    public PdfBuilderOutputData call() throws Exception{
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
        String pdfTempFilePath = pdfBuilder.buildPDF(data);

        if (isFileEmpty(pdfTempFilePath)) {
            throw new LanguageNotSupportedException();
        }

        String url = uploadToStorage(pdfTempFilePath);
        return url;
    }

    private String uploadToStorage(String filePath) {
        String url = "";
        try {
            CloudStorageHelper helper = new CloudStorageHelper();
            String bucketName = session.getServletContext().getInitParameter(BUCKET_FOR_PDFS_PARAMETER);
            helper.createBucket(bucketName);
            url = helper.uploadFile(Paths.get(filePath), bucketName);
        } catch (IOException | ServletException e) {
            e.printStackTrace();
        } finally {
            deleteFile(filePath);
        }
        return url;
    }

    /**
     * check if pdf file contains any text
     * @param path
     * @return
     */
    private boolean isFileEmpty(String path) {
        String allText = "";
        try {
            PdfReader reader = new PdfReader(path);

            for (int page = 1; page <= 1; page++) {
                allText = PdfTextExtractor.getTextFromPage(reader, page);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return (allText == null) || allText.length() < 1;
    }

    /**
     * delete file if exists
     *
     * @param path path
     */
    private void deleteFile(String path) {
        try {
            File file = new File(path);
            if (file.delete()) {
                logger.info(path + " is deleted");
            } else {
                logger.info("Delete operation is failed.");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public class TextNotFoundException extends Throwable {
    }

    public class LanguageNotSupportedException extends Throwable {
    }
}
