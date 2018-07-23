package ocrme_backend.servlets.ocr;

import ocrme_backend.datastore.gcloud_datastore.objects.OcrRequest;
import ocrme_backend.file_builder.pdfbuilder.PdfBuilderInputData;
import ocrme_backend.file_builder.pdfbuilder.PdfBuilderOutputData;
import ocrme_backend.ocr.OCRProcessor;
import ocrme_backend.ocr.OcrProcessorImpl;

import javax.annotation.Nullable;
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
class OcrRequestManager {
    private final Logger logger = Logger.getLogger(OcrRequestManager.class.getName());
    private @Nullable
    String idTokenString;
    private List<String> languages;
    private HttpSession session;
    private String gcsImageUri; //download uri of image, stored in google cloud storage

    /**
     * @param idTokenString for associate request with user. Docs https://developers.google.com/identity/sign-in/android/backend-auth
     * @param gcsImageUri   download uri of image, stored in google cloud storage
     * @param languages
     * @param session
     */
    OcrRequestManager(String idTokenString, String gcsImageUri, String[] languages,
                      HttpSession session) {
        this.idTokenString = idTokenString;
        this.gcsImageUri = gcsImageUri;
        this.session = session;
        this.languages = new ArrayList<>();
        if (languages != null) {
            this.languages = Arrays.asList(languages);
        }
    }

    OcrResponse process() {
        OcrResponse response = new OcrResponse();
        try {
            OcrData ocrData = processForOcrResult();
            OcrData.Status ocrStatus = ocrData.getStatus();
            if (ocrStatus.equals(OcrData.Status.OK)) {
                response.setStatus(OcrResponse.Status.OK);

                PdfBuilderOutputData textPdfData =
                        makePdf(ocrData.getPdfBuilderInputData());
                String textPdfGsUrl = textPdfData.getGsUrl();
                String textPdfMediaUrl = textPdfData.getMediaUrl();

                PdfBuilderOutputData imagePdfData = makePdf(gcsImageUri);
                String imagePdfGsUrl = imagePdfData.getGsUrl();
                String imagePdfMediaUrl = imagePdfData.getMediaUrl();

                OcrResult ocrResult = new OcrResult.Builder()
                        .textResult(ocrData.getSimpleText())
                        .pdfResultMediaUrl(textPdfMediaUrl)
                        .pdfResultGsUrl(textPdfGsUrl)
                        .pdfImageResultGsUrl(imagePdfGsUrl)
                        .pdfImageResultMediaUrl(imagePdfMediaUrl)
                        .languages(languages)
                        .sourceImageUrl(gcsImageUri)
                        .build();

                response.setOcrResult(ocrResult);
                writePdfStatus(response, textPdfData, imagePdfData);

            } else {
                processAsDefective(ocrStatus, response);
            }
        } catch (Exception e) {
            response.setStatus(OcrResponse.Status.UNKNOWN_ERROR);
            e.printStackTrace();
            logger.log(WARNING, "Error: " + e.getMessage());
        } finally {
            addToDb(response);
        }
        logger.log(INFO, "Response: " + response.toString());
        return response;
    }

    private void processAsDefective(OcrData.Status ocrStatus, OcrResponse response) {
        switch (ocrStatus) {
            case OK:
                response.setStatus(OcrResponse.Status.OK);
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
    }

    private void writePdfStatus(OcrResponse response,
                                PdfBuilderOutputData textPdfData,
                                PdfBuilderOutputData imagePdfData) {

        PdfBuilderOutputData.Status textPdfStatus = textPdfData.getStatus();
        PdfBuilderOutputData.Status imagePdfStatus = imagePdfData.getStatus();

        if (textPdfStatus.equals(PdfBuilderOutputData.Status.OK) &&
                imagePdfStatus.equals(PdfBuilderOutputData.Status.OK)) {
            response.setStatus(OcrResponse.Status.OK);
        } else if (textPdfStatus.equals(PdfBuilderOutputData.Status.PDF_CAN_NOT_BE_CREATED_LANGUAGE_NOT_SUPPORTED) ||
                imagePdfStatus.equals(PdfBuilderOutputData.Status.PDF_CAN_NOT_BE_CREATED_LANGUAGE_NOT_SUPPORTED)) {
            response.setStatus(OcrResponse.Status.PDF_CAN_NOT_BE_CREATED_LANGUAGE_NOT_SUPPORTED);
        } else if (textPdfStatus.equals(PdfBuilderOutputData.Status.PDF_CAN_NOT_BE_CREATED_EMPTY_DATA) ||
                imagePdfStatus.equals(PdfBuilderOutputData.Status.PDF_CAN_NOT_BE_CREATED_EMPTY_DATA)) {
            response.setStatus(OcrResponse.Status.TEXT_NOT_FOUND);
        } else if (textPdfStatus.equals(PdfBuilderOutputData.Status.UNKNOWN_ERROR) ||
                imagePdfStatus.equals(PdfBuilderOutputData.Status.UNKNOWN_ERROR)) {
            response.setStatus(OcrResponse.Status.UNKNOWN_ERROR);
        }
        else {
            response.setStatus(OcrResponse.Status.UNKNOWN_ERROR);
            logger.log(WARNING, "Unexpected status received.");
        }
    }

    private void addToDb(OcrResponse response) {

        String userId = getUserId(idTokenString);
        String email = getUserEmail(idTokenString);

        Optional<OcrResult> ocrResultOptional = Optional.ofNullable(response.getOcrResult());
        OcrResult dummyOcrResult = new OcrResult.Builder().build();
        OcrResult ocrResult = ocrResultOptional.orElse(dummyOcrResult);

        //put request data to Db
        long requestId = new DbPusher().add(
                new OcrRequest.Builder()
                        .sourceImageUrl(ocrResult.getSourceImageUrl())
                        .languages(ocrResult.getLanguages())
                        .createdById(userId)
                        .createdBy(email)
                        .pdfResultGsUrl(ocrResult.getPdfResultGsUrl())
                        .pdfResultMediaUrl(ocrResult.getPdfResultMediaUrl())
                        .pdfImageResultGsUrl(ocrResult.getPdfImageResultGsUrl())
                        .pdfImageResultMediaUrl(ocrResult.getPdfImageResultMediaUrl())
                        .status(response.getStatus().name())
                        .textResult(Optional.ofNullable(ocrResult.getTextResult()))
                        .timeStamp(ocrResult.getTimeStamp())
                        .build());

        logger.log(INFO, "OcrReques data saved in DB, entity id = " + requestId);
    }

    private OcrData processForOcrResult() throws IOException, GeneralSecurityException {
        OCRProcessor processor = new OcrProcessorImpl();
        OcrData ocrData = processor.ocrForData(gcsImageUri, languages);
        logger.log(INFO, "ocr result obtained");
        return ocrData;
    }

    private PdfBuilderOutputData makePdf(PdfBuilderInputData ocrResult) {
        PdfBuilderSyncTask pdfBuilderSyncTask = new PdfBuilderSyncTask(ocrResult, session);
        PdfBuilderOutputData pdfBuilderOutputData = pdfBuilderSyncTask.execute();

        logger.log(INFO, "text pdf file generation finished.");
        return pdfBuilderOutputData;
    }


    private PdfBuilderOutputData makePdf(String gcsImageUri) {
        PdfBuilderSyncTask pdfBuilderSyncTask = new PdfBuilderSyncTask(gcsImageUri, session);
        PdfBuilderOutputData pdfBuilderOutputData = pdfBuilderSyncTask.execute();

        logger.log(INFO, "image pdf file generation finished.");
        return pdfBuilderOutputData;
    }
}
