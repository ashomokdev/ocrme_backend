package ocrme_backend.ocr;

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.vision.v1.Vision;
import com.google.api.services.vision.v1.VisionScopes;
import com.google.api.services.vision.v1.model.*;
import com.google.common.collect.ImmutableList;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.List;

/**
 * Created by iuliia on 5/17/17.
 */
public class OCRProcessorImpl implements OCRProcessor{
    private final Vision vision;
    private static final String APPLICATION_NAME = "ashomokdev-ocr_me/1.0";

    public OCRProcessorImpl() throws IOException, GeneralSecurityException {

        vision =  getVisionService();
    }

    /**
     * Connects to the Vision API using Application Default Credentials.
     */
    public static Vision getVisionService() throws IOException, GeneralSecurityException {
        GoogleCredential credential =
                GoogleCredential.getApplicationDefault().createScoped(VisionScopes.all());
        com.google.api.client.json.JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
        return new Vision.Builder(GoogleNetHttpTransport.newTrustedTransport(), jsonFactory, credential)
                .setApplicationName(APPLICATION_NAME)
                .build();
    }

    @Override
    public String doOCR(byte[] image) throws IOException {
        AnnotateImageRequest request =
                new AnnotateImageRequest()
                        .setImage(new Image().encodeContent(image))
                        .setFeatures(ImmutableList.of(
                                new Feature()
                                        .setType("TEXT_DETECTION")
                                        .setMaxResults(1)));
        Vision.Images.Annotate annotate =
                vision.images()
                        .annotate(new BatchAnnotateImagesRequest().setRequests(ImmutableList.of(request)));
        // Due to a bug: requests to Vision API containing large images fail when GZipped.
        annotate.setDisableGZipContent(true);

        BatchAnnotateImagesResponse batchResponse = annotate.execute();
        assert batchResponse.getResponses().size() == 1;
        String recognizedText = convertResponseToString(batchResponse);
        return recognizedText;
    }

    private String convertResponseToString(BatchAnnotateImagesResponse response) {
        String message = "";

        List<EntityAnnotation> texts = response.getResponses().get(0).getTextAnnotations();
        if (texts != null && texts.size() > 0) {
            message += texts.get(0).getDescription();
        }
        return message;
    }
}
