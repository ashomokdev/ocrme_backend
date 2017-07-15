package ocrme_backend.servlets.ocr;

import ocrme_backend.datastore.gcloud_datastore.objects.OcrRequest;
import ocrme_backend.datastore.gcloud_storage.utils.CloudStorageHelper;
import ocrme_backend.file_builder.pdfbuilder.PDFData;
import org.apache.commons.fileupload.FileItemStream;

import javax.servlet.ServletException;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by iuliia on 7/13/17.
 */
public class OcrRequestManager {
    private FileItemStream file;
    private String[] languages;
    private HttpSession session;
    private final Logger logger = Logger.getLogger(OcrRequestManager.class.getName());
    public static final String BUCKET_FOR_REQUESTS_PARAMETER = "ocrme.bucket.request_images";


    public OcrRequestManager(FileItemStream file, String[] languages, HttpSession session) {
        this.file = file;
        this.languages = languages;
        this.session = session;
    }

    public OcrResponse processForResult() throws IOException, ServletException {

        OcrResponse response = new OcrResponse();
        try {
            final ExecutorService threadPool = (ExecutorService) session.getServletContext()
                    .getAttribute("threadPoolAlias");

            PDFData ocrResult = getOcrResult(threadPool);

            String pdfUrl = makePdf(threadPool, ocrResult);

            addToDb(threadPool, ocrResult, pdfUrl);

            response.setTextResult(ocrResult.getSimpleText());
            response.setPdfResultUrl(pdfUrl);

        } catch (Exception e) {
            response.setErrorMessage(e.getMessage());
        }

        return response;
    }

    private void addToDb(ExecutorService threadPool, PDFData ocrResult, String pdfUrl) {

        threadPool.submit(new Runnable() {
            @Override
            public void run() {
                //upload request file to google cloud storage
                CloudStorageHelper helper = new CloudStorageHelper();
                String bucketName = session.getServletContext().getInitParameter(BUCKET_FOR_REQUESTS_PARAMETER);
                helper.createBucket(bucketName);
                String inputImageUrl = null;
                try {
                    inputImageUrl = helper.uploadFile(file, bucketName);
                } catch (IOException | ServletException e) {
                    e.printStackTrace();
                }

                //put request data to Db
                DbPusher dbPusher = new DbPusher();
                long requestId = dbPusher.add(
                        new OcrRequest.Builder()
                                .inputImageUrl(inputImageUrl)
                                .languages(languages)
                                .pdfResultUrl(pdfUrl)
                                .textResult(ocrResult.getSimpleText())
                                .build());
                logger.log(Level.INFO, "data saved in DB, entity id = " + requestId);
            }
        });
    }

    private String makePdf(ExecutorService threadPool, PDFData ocrResult)
            throws InterruptedException, java.util.concurrent.ExecutionException {

        PdfBuilderCallableTask pdfBuilderCallableTask = new PdfBuilderCallableTask(ocrResult, session);
        final Future<String> pdfUrlFuture = threadPool.submit(pdfBuilderCallableTask);
        String pdfUrl = pdfUrlFuture.get();
        logger.log(Level.INFO, "pdf file generated");
        return pdfUrl;
    }

    private PDFData getOcrResult(ExecutorService threadPool)
            throws InterruptedException, java.util.concurrent.ExecutionException {

        OcrCallableTask ocrCallableTask = new OcrCallableTask(file, languages);
        final Future<PDFData> pdfDataFuture = threadPool.submit(ocrCallableTask);
        PDFData pdfData = pdfDataFuture.get();
        logger.log(Level.INFO, "ocr result obtained");
        return pdfData;
    }
}
