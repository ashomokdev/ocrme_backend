package ocrme_backend.servlets.ocr;

import ocrme_backend.datastore.gcloud_datastore.objects.OcrRequest;
import ocrme_backend.datastore.gcloud_storage.utils.CloudStorageHelper;
import ocrme_backend.file_builder.pdfbuilder.PdfBuilderInputData;
import ocrme_backend.file_builder.pdfbuilder.PdfBuilderOutputData;
import ocrme_backend.ocr.OCRProcessor;
import ocrme_backend.ocr.OcrProcessorImpl;

import javax.servlet.ServletException;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Arrays;
import java.util.List;
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
    private String gcsImageUri; //download uri of image, stored in google cloud storage
    public static final String BUCKET_FOR_REQUESTS_PARAMETER = "ocrme.bucket.request_images";


    public OcrRequestManager(String imageFilename, byte[] imageBytes, String[] languages, HttpSession session) {
        this.imageFilename = imageFilename;
        this.imageBytes = imageBytes;
        this.languages = languages;
        this.session = session;
    }

    /**
     * @param gcsImageUri download uri of image, stored in google cloud storage
     * @param languages
     * @param session
     */
    public OcrRequestManager(String gcsImageUri, String[] languages, HttpSession session) {
        this.gcsImageUri = gcsImageUri;
        this.languages = languages;
        this.session = session;
    }

    public OcrResponse process() throws IOException, ServletException {

        OcrResponse response = new OcrResponse();
        try {
            OcrData ocrResult = processForOcrResult();
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

    //fixme method saved  inputImageUrl in 2 different formats
    private void addToDb(OcrResponse response) {
        String inputImageUrl;
        if (gcsImageUri == null) {
            inputImageUrl = uploadRequestImageToStorage(imageFilename, imageBytes);
        } else {
            inputImageUrl = gcsImageUri;
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

    private OcrData processForOcrResult()
            throws InterruptedException, java.util.concurrent.ExecutionException, IOException, GeneralSecurityException {

        List<String> languagesList = null;
        if (languages != null) {
            languagesList = Arrays.asList(languages);
        }

        OCRProcessor processor = new OcrProcessorImpl();
        OcrData ocrData;
        if (gcsImageUri == null) {
            ocrData = processor.ocrForData(imageBytes, languagesList);
        } else {
            ocrData = processor.ocrForData(gcsImageUri, languagesList);
        }
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
