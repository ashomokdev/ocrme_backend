package ocrme_backend.servlets.ocr;

import ocrme_backend.datastore.gcloud_datastore.objects.OcrRequest;
import ocrme_backend.datastore.gcloud_storage.utils.CloudStorageHelper;
import ocrme_backend.file_builder.pdfbuilder.PdfBuilderInputData;
import ocrme_backend.file_builder.pdfbuilder.PdfBuilderOutputData;

import javax.servlet.ServletException;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by iuliia on 7/13/17.
 */
public class OcrRequestManager {
    private String imageFilename;
    private byte[] imageBytes;
    private String[] languages;
    private HttpSession session;
    private final Logger logger = Logger.getLogger(OcrRequestManager.class.getName());
    public static final String BUCKET_FOR_REQUESTS_PARAMETER = "ocrme.bucket.request_images";


    public OcrRequestManager(String imageFilename, byte[] imageBytes, String[] languages, HttpSession session) {
        this.imageFilename = imageFilename;
        this.imageBytes = imageBytes;
        this.languages = languages;
        this.session = session;
    }

    public OcrResponse process() throws IOException, ServletException {

        OcrResponse response = new OcrResponse();
        try {
            OcrData ocrResult = getOcrResult();
            String simpleTextResult = ocrResult.getSimpleText();
            response.setTextResult(simpleTextResult);

            PdfBuilderOutputData pdfBuilderOutputData = makePdf(ocrResult.getPdfBuilderInputData());

            String pdfUrl = pdfBuilderOutputData.getUrl();
            response.setPdfResultUrl(pdfUrl);
            PdfBuilderOutputData.Status status = pdfBuilderOutputData.getStatus();
            switch (status) {
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
                    logger.log(Level.INFO, "Unexpected status received.");
                    break;
            }

        } catch (Exception e) {
            response.setStatus(OcrResponse.Status.UNKNOWN_ERROR);
            e.printStackTrace();
        } finally {
            addToDb(response);
        }
        logger.log(Level.INFO, "Response: " + response.toString());
        return response;
    }

    //upload request imageBytes file to google cloud storage
    private String uploadRequestImageToStorage(String filename, byte[] file) {
        String url = "";
        try {
            CloudStorageHelper helper = new CloudStorageHelper();
            String bucketName = session.getServletContext().getInitParameter(BUCKET_FOR_REQUESTS_PARAMETER);
            helper.createBucket(bucketName);
            url = helper.uploadFile(file, filename, bucketName);
        } catch (IOException | ServletException e) {
            e.printStackTrace();
        }
        return url;
    }

    private void addToDb(OcrResponse response) {

        String inputImageUrl = uploadRequestImageToStorage(imageFilename, imageBytes);

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

    private OcrData getOcrResult()
            throws InterruptedException, java.util.concurrent.ExecutionException, IOException, GeneralSecurityException {

        OcrSyncTask ocrSyncTask = new OcrSyncTask(imageBytes, languages);
        OcrData ocrData = ocrSyncTask.execute();
        logger.log(Level.INFO, "ocr result obtained");
        return ocrData;
    }

    private PdfBuilderOutputData makePdf(PdfBuilderInputData ocrResult) {
        PdfBuilderSyncTask pdfBuilderSyncTask = new PdfBuilderSyncTask(ocrResult, session);
        PdfBuilderOutputData pdfBuilderOutputData = pdfBuilderSyncTask.execute();

        logger.log(Level.INFO, "pdf file generation finished.");
        return pdfBuilderOutputData;
    }
}
