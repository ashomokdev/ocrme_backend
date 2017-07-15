package ocrme_backend.servlets.ocr;

import ocrme_backend.datastore.gcloud_storage.utils.CloudStorageHelper;
import ocrme_backend.file_builder.pdfbuilder.PDFBuilder;
import ocrme_backend.file_builder.pdfbuilder.PDFBuilderImpl;
import ocrme_backend.file_builder.pdfbuilder.PDFData;

import javax.servlet.ServletException;
import javax.servlet.http.HttpSession;
import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by iuliia on 7/13/17.
 * builds pdf and save in Google cloud storage
 */
public class PdfBuilderCallableTask implements Callable<String> {
    private static Logger logger;
    private HttpSession session;
    private PDFData data;
    public static final String BUCKET_FOR_PDFS_PARAMETER = "ocrme.bucket.pdf";

    public PdfBuilderCallableTask(PDFData data, HttpSession session) {
        this.data = data;
        this.session = session;
        logger = Logger.getLogger(PdfBuilderCallableTask.class.getName());
    }


    @Override
    public String call() throws Exception {
        logger.log(Level.INFO, "call called");
        String path = buildPdf(data, session);
        logger.log(Level.INFO, "pdf generated, url for download: " + path);
        return path;
    }

    private String buildPdf(PDFData data, HttpSession session) {

        PDFBuilder pdfBuilder = new PDFBuilderImpl(session);
        String pdfTempFilePath = pdfBuilder.buildPDF(data);
        String url = "";

        try {
            CloudStorageHelper helper = new CloudStorageHelper();
            String bucketName = session.getServletContext().getInitParameter(BUCKET_FOR_PDFS_PARAMETER);
            helper.createBucket(bucketName);
            url = helper.uploadFile(Paths.get(pdfTempFilePath), bucketName);
        } catch (IOException | ServletException e) {
            e.printStackTrace();
        } finally {
            deleteFile(pdfTempFilePath);
        }

        return url;
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
}
