package ocrme_backend.servlets.ocr;

import ocrme_backend.datastore.gcloud_datastore.objects.OcrRequest;
import ocrme_backend.datastore.gcloud_storage.utils.CloudStorageHelper;
import ocrme_backend.file_builder.pdfbuilder.PdfBuilderInputData;
import ocrme_backend.file_builder.pdfbuilder.PdfBuilderOutputData;
import org.apache.commons.fileupload.FileItemIterator;
import org.apache.commons.fileupload.FileItemStream;
import org.apache.commons.fileupload.FileUploadException;

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
    private FileItemIterator file;
    private String[] languages;
    private HttpSession session;
    private final ExecutorService threadPool;
    private final Logger logger = Logger.getLogger(OcrRequestManager.class.getName());
    public static final String BUCKET_FOR_REQUESTS_PARAMETER = "ocrme.bucket.request_images";


    public OcrRequestManager(FileItemIterator file, String[] languages, HttpSession session) {
        this.file = file;
        this.languages = languages;
        this.session = session;
        threadPool = (ExecutorService) session.getServletContext().getAttribute("threadPoolAlias");
    }

    public OcrResponse processForResult() throws IOException, ServletException {

        OcrResponse response = new OcrResponse();
        try {
            PdfBuilderInputData ocrResult = getOcrResult(threadPool);
            String simpleTextResult = ocrResult.getSimpleText();
            response.setTextResult(simpleTextResult);

            PdfBuilderOutputData pdfBuilderOutputData = makePdf(threadPool, ocrResult);

            String pdfUrl = pdfBuilderOutputData.getUrl();
            response.setPdfResultUrl(pdfUrl);
            PdfBuilderOutputData.Status status = pdfBuilderOutputData.getStatus();
            switch (status)
            {
                case OK:
                    response.setStatus(OcrResponse.Status.OK);
                    break;
                case PDF_CAN_NOT_BE_CREATED_LANGUAGE_NOT_SUPPORTED:
                    response.setStatus(OcrResponse.Status.PDF_CAN_NOT_BE_CREATED_LANGUAGE_NOT_SUPPORTED);
                    break;
                case PDF_CAN_NOT_BE_CREATED_EMPTY_DATA:
                    response.setStatus(OcrResponse.Status.TEXT_NOT_FOUND);
                    break;
                default:
                    logger.log(Level.INFO,"Unexpected status received.");
                    break;
            }

        } catch (Exception e) {
            response.setStatus(OcrResponse.Status.UNKNOWN_ERROR);
            e.printStackTrace();
        }
        finally {
            addToDb(response);
        }

        return response;
    }

    private void addToDb(OcrResponse response) {
        threadPool.submit(new Runnable() {
            @Override
            public void run() {
                //upload request file to google cloud storage
                CloudStorageHelper helper = new CloudStorageHelper();
                String bucketName = session.getServletContext().getInitParameter(BUCKET_FOR_REQUESTS_PARAMETER);
                assert (bucketName != null);

                helper.createBucket(bucketName);
                String inputImageUrl = null;
                try {
                    while (file.hasNext()) {
                        FileItemStream item = file.next();
                        inputImageUrl = helper.uploadFile(item, bucketName);
                    }
                } catch (IOException | ServletException |FileUploadException e) {
                    e.printStackTrace();
                }

                //put request data to Db
                DbPusher dbPusher = new DbPusher();
                long requestId = dbPusher.add(
                        new OcrRequest.Builder()
                                .inputImageUrl(inputImageUrl)
                                .languages(languages)
                                .pdfResultUrl(response.getPdfResultUrl())
                                .status(response.getStatus().name())
                                .textResult(response.getTextResult())
                                .build());
                logger.log(Level.INFO, "data saved in DB, entity id = " + requestId);
            }
        });
    }

    private PdfBuilderInputData getOcrResult(ExecutorService threadPool)
            throws InterruptedException, java.util.concurrent.ExecutionException {

        OcrCallableTask ocrCallableTask = new OcrCallableTask(file, languages);
        final Future<PdfBuilderInputData> pdfDataFuture = threadPool.submit(ocrCallableTask);
        PdfBuilderInputData pdfData = pdfDataFuture.get();
        logger.log(Level.INFO, "ocr result obtained");
        return pdfData;
    }

    private PdfBuilderOutputData makePdf(ExecutorService threadPool, PdfBuilderInputData ocrResult)
            throws InterruptedException, java.util.concurrent.ExecutionException {

        PdfBuilderCallableTask pdfBuilderCallableTask = new PdfBuilderCallableTask(ocrResult, session);
        final Future<PdfBuilderOutputData> pdfBuilderOutputDataFuture = threadPool.submit(pdfBuilderCallableTask);

        PdfBuilderOutputData pdfBuilderOutputData = pdfBuilderOutputDataFuture.get();

        logger.log(Level.INFO, "pdf file generation finished.");
        return pdfBuilderOutputData;
    }
}
