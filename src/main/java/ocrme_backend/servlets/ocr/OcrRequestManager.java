package ocrme_backend.servlets.ocr;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseToken;
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
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

import static java.util.logging.Level.INFO;
import static java.util.logging.Level.WARNING;

/**
 * Created by iuliia on 7/13/17.
 */
public class OcrRequestManager {
    private @Nullable
    String idTokenString;
    private String imageFilename;
    private byte[] imageBytes;
    private String[] languages;
    private HttpSession session;
    private final Logger logger = Logger.getLogger(OcrRequestManager.class.getName());
    private String gcsImageUri; //download uri of image, stored in google cloud storage
    public static final String BUCKET_FOR_REQUEST_IMAGES_PARAMETER = "ocrme.bucket.request_images";
    public static final String DIR_FOR_REQUEST_IMAGES_PARAMETER = "ocrme.dir.request_images";

    @Deprecated
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
        this.languages = languages;
        this.session = session;
    }

    public OcrResponse process() throws IOException, ServletException {

        OcrResponse response = new OcrResponse();
        try {
            OcrData ocrResult = processForOcrResult();
            OcrData.Status ocrStatus = ocrResult.getStatus();
            switch (ocrStatus) {
                case OK:
                    response.setStatus(OcrResponse.Status.OK);
                    doStaff(ocrResult, response);
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

    private void doStaff(OcrData ocrResult, OcrResponse response) {
        String simpleTextResult = ocrResult.getSimpleText();
        response.setTextResult(simpleTextResult);

        PdfBuilderOutputData pdfBuilderOutputData = makePdf(ocrResult.getPdfBuilderInputData());
        String pdfGsUrl = pdfBuilderOutputData.getGsUrl();
        String pdfMediaUrl = pdfBuilderOutputData.getMediaUrl();
        response.setPdfResultGsUrl(pdfGsUrl);
        response.setPdfResultMediaUrl(pdfMediaUrl);
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
        String sourceImageUrl = getSourceImageUrl();

        String userId = getUserId(idTokenString);
        String email = getUserEmail(idTokenString);

        //put request data to Db
        DbPusher dbPusher = new DbPusher();
        long requestId = dbPusher.add(
                new OcrRequest.Builder()
                        .sourceImageUrl(sourceImageUrl)
                        .languages(languages)
                        .createdById(userId)
                        .createdBy(email)
                        .pdfResultUrl(response.getPdfResultGsUrl())
                        .status(response.getStatus().name())
                        .textResult(Optional.ofNullable(response.getTextResult()))
                        .build());
        logger.log(WARNING, "data saved in DB, entity id = " + requestId);
    }

    public @Nullable
    String getUserId(@Nullable String idTokenString) {
        String userId = null;
        FirebaseToken decodedToken = getUserDecodedToken(idTokenString);
        if (decodedToken != null) {
            userId = decodedToken.getUid();
        }
        logger.log(INFO, "User id: " + userId);
        return userId;
    }

    public @Nullable
    String getUserEmail(@Nullable String idTokenString) {
        String email = null;
        FirebaseToken decodedToken = getUserDecodedToken(idTokenString);
        if (decodedToken != null) {
            email = decodedToken.getEmail();
        }
        logger.log(INFO, "User email: " + email);
        return email;
    }

    private @Nullable
    FirebaseToken getUserDecodedToken(@Nullable String idTokenString) {
        FirebaseToken decodedToken = null;
        if (idTokenString != null) {
            try {
                initFirebase();
            } catch (Exception e) {
                //already initialized - ignore
            }
            try {
                decodedToken = FirebaseAuth.getInstance().verifyIdTokenAsync(idTokenString).get();
            } catch (Exception e) {
                e.printStackTrace();
                logger.log(WARNING, "error in getUserDecodedToken: " + e.getMessage());
            }
        }
        return decodedToken;
    }

    private void initFirebase() throws IOException {
        FirebaseOptions options = new FirebaseOptions.Builder()
                .setCredentials(GoogleCredentials.getApplicationDefault())
                .setDatabaseUrl("https://imagetotext-149919.firebaseio.com")
                .build();

        FirebaseApp.initializeApp(options);
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
