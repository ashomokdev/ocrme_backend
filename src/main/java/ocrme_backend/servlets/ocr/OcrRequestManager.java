package ocrme_backend.servlets.ocr;

import ocrme_backend.datastore.gcloud_datastore.objects.OcrRequest;
import ocrme_backend.datastore.gcloud_storage.utils.CloudStorageHelper;
import ocrme_backend.file_builder.pdfbuilder.PdfBuilderInputData;
import ocrme_backend.file_builder.pdfbuilder.PdfBuilderOutputData;
import ocrme_backend.ocr.OCRProcessor;
import ocrme_backend.ocr.OcrProcessorImpl;

import javax.annotation.Nullable;
import javax.servlet.ServletException;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;


import static java.util.logging.Level.INFO;
import static java.util.logging.Level.WARNING;
import static ocrme_backend.utils.FirebaseAuthUtil.getUserEmail;
import static ocrme_backend.utils.FirebaseAuthUtil.getUserId;


/**
 * Created by iuliia on 7/13/17.
 */
public class OcrRequestManager {
    private @Nullable
    String idTokenString;
    private String imageFilename;
    private byte[] imageBytes;
    private List<String> languages;
    private HttpSession session;
    private final Logger logger = Logger.getLogger(OcrRequestManager.class.getName());
    private String gcsImageUri; //download uri of image, stored in google cloud storage
    public static final String BUCKET_FOR_REQUEST_IMAGES_PARAMETER = "ocrme.bucket.request_images";
    public static final String DIR_FOR_REQUEST_IMAGES_PARAMETER = "ocrme.dir.request_images";

    @Deprecated
    public OcrRequestManager(String imageFilename, byte[] imageBytes, String[] languages, HttpSession session) {
        this.imageFilename = imageFilename;
        this.imageBytes = imageBytes;
        this.session = session;

        this.languages = new ArrayList<>();
        if (languages != null) {
            this.languages = Arrays.asList(languages);
        }
    }

    /**
     * @param gcsImageUri download uri of image, stored in google cloud storage
     * @param languages
     * @param session
     */
    @Deprecated
    public OcrRequestManager(String gcsImageUri, String[] languages, HttpSession session) {
        this(null, gcsImageUri, languages, session);
    }

    /**
     * @param idTokenString for associate request with user. Docs https://developers.google.com/identity/sign-in/android/backend-auth
     * @param gcsImageUri   download uri of image, stored in google cloud storage
     * @param languages
     * @param session
     */
    public OcrRequestManager(String idTokenString, String gcsImageUri, String[] languages, HttpSession session) {
        this.idTokenString = idTokenString;
        this.gcsImageUri = gcsImageUri;
        this.session = session;
        this.languages = new ArrayList<>();
        if (languages != null) {
            this.languages = Arrays.asList(languages);
        }
    }

    public OcrResponse process() throws IOException, ServletException {

        OcrResponse response = new OcrResponse();
        try {
            OcrData ocrResult = processForOcrResult();
            OcrData.Status ocrStatus = ocrResult.getStatus();
            switch (ocrStatus) {
                case OK:
                    response.setStatus(OcrResponse.Status.OK);
                    writeResponse(ocrResult, response);
                    break;
                case TEXT_NOT_FOUND:
                    response.setStatus(OcrResponse.Status.TEXT_NOT_FOUND);
                    break;
                case INVALID_LANGUAGE_HINTS:
                    response.setStatus(OcrResponse.Status.INVALID_LANGUAGE_HINTS);
                    break;
                case UNKNOWN_ERROR:
                    response.setStatus(OcrResponse.Status.UNKNOWN_ERROR);
                    break;
                default:
                    logger.log(INFO, "Unexpected status received.");
                    response.setStatus(OcrResponse.Status.UNKNOWN_ERROR);
                    break;
            }
        } catch (Exception e) {
            response.setStatus(OcrResponse.Status.UNKNOWN_ERROR);
            e.printStackTrace();
        } finally {
            addToDb(response);
        }
        logger.log(INFO, "Response: " + response.toString());
        return response;
    }

    //todo refactor for better reading
    private void writeResponse(OcrData data, OcrResponse response) {
        PdfBuilderOutputData pdfBuilderOutputData = writeOcrResult(data, response);
        writeStatus(response, pdfBuilderOutputData);
    }

    private void writeStatus(OcrResponse response, PdfBuilderOutputData pdfBuilderOutputData) {
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
            case UNKNOWN_ERROR:
                response.setStatus(OcrResponse.Status.UNKNOWN_ERROR);
                break;
            default:
                response.setStatus(OcrResponse.Status.UNKNOWN_ERROR);
                logger.log(INFO, "Unexpected status received.");
                break;
        }
    }

    private PdfBuilderOutputData writeOcrResult(OcrData data, OcrResponse response) {
        PdfBuilderOutputData pdfBuilderOutputData = makePdf(data.getPdfBuilderInputData());
        String pdfGsUrl = pdfBuilderOutputData.getGsUrl();
        String pdfMediaUrl = pdfBuilderOutputData.getMediaUrl();

        String sourceImageUrl = getSourceImageUrl();
        OcrResult ocrResult = new OcrResult.Builder()
                .textResult(data.getSimpleText())
                .pdfResultMediaUrl(pdfMediaUrl)
                .pdfResultGsUrl(pdfGsUrl)
                .languages(languages)
                .sourceImageUrl(sourceImageUrl)
                .build();

        response.setOcrResult(ocrResult);
        return pdfBuilderOutputData;
    }

    //upload request imageBytes file to google cloud storage
    private String uploadRequestImageToStorage(String filename, byte[] file) {
        String url = "";
        try {
            CloudStorageHelper helper = new CloudStorageHelper();
            String bucketName = session.getServletContext().getInitParameter(BUCKET_FOR_REQUEST_IMAGES_PARAMETER);
            String directoryName = session.getServletContext().getInitParameter(DIR_FOR_REQUEST_IMAGES_PARAMETER);
            helper.createBucket(bucketName);
            url = helper.uploadFile(file, filename, directoryName, bucketName);
        } catch (IOException | ServletException e) {
            e.printStackTrace();
        }
        return url;
    }

    private void addToDb(OcrResponse response) {

        Optional<OcrResult> ocrResultOptional = Optional.ofNullable(response.getOcrResult());

        String userId = getUserId(idTokenString);
        String email = getUserEmail(idTokenString);

        //put request data to Db
        DbPusher dbPusher = new DbPusher();
        OcrResult dummyOcrResult = new OcrResult.Builder().build();
        long requestId = dbPusher.add(
                new OcrRequest.Builder()
                        .sourceImageUrl(ocrResultOptional.orElse(dummyOcrResult).getSourceImageUrl())
                        .languages(ocrResultOptional.orElse(dummyOcrResult).getLanguages())
                        .createdById(userId)
                        .createdBy(email)
                        .pdfResultGsUrl(ocrResultOptional.orElse(dummyOcrResult).getPdfResultGsUrl())
                        .pdfResultMediaUrl(ocrResultOptional.orElse(dummyOcrResult).getPdfResultMediaUrl())
                        .status(response.getStatus().name())
                        .textResult(Optional.ofNullable(ocrResultOptional.orElse(dummyOcrResult).getTextResult()))
                        .timeStamp(ocrResultOptional.orElse(dummyOcrResult).getTimeStamp())
                        .build());


        logger.log(WARNING, "data saved in DB, entity id = " + requestId);
    }

    private String getSourceImageUrl() {
        String inputImageUrl;
        if (gcsImageUri == null) {
            inputImageUrl = uploadRequestImageToStorage(imageFilename, imageBytes);
        } else {
            inputImageUrl = gcsImageUri;
        }
        return inputImageUrl;
    }

    private OcrData processForOcrResult()
            throws InterruptedException, java.util.concurrent.ExecutionException, IOException, GeneralSecurityException {

        OCRProcessor processor = new OcrProcessorImpl();
        OcrData ocrData;
        if (gcsImageUri == null) {
            ocrData = processor.ocrForData(imageBytes, languages);
        } else {
            ocrData = processor.ocrForData(gcsImageUri, languages);
        }
        logger.log(INFO, "ocr result obtained");
        return ocrData;
    }

    private PdfBuilderOutputData makePdf(PdfBuilderInputData ocrResult) {
        PdfBuilderSyncTask pdfBuilderSyncTask = new PdfBuilderSyncTask(ocrResult, session);
        PdfBuilderOutputData pdfBuilderOutputData = pdfBuilderSyncTask.execute();

        logger.log(INFO, "pdf file generation finished.");
        return pdfBuilderOutputData;
    }
}
