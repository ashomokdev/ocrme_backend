package ocrme_backend.ocr;

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.ArrayMap;
import com.google.api.services.vision.v1.Vision;
import com.google.api.services.vision.v1.VisionScopes;
import com.google.api.services.vision.v1.model.*;
import com.google.common.collect.ImmutableList;
import ocrme_backend.file_builder.pdfbuilder.PDFData;
import ocrme_backend.file_builder.pdfbuilder.TextUnit;

import javax.annotation.Nullable;
import java.io.IOException;
import java.math.BigDecimal;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by iuliia on 7/11/17.
 */
public class OcrProcessorImpl implements OCRProcessor {

    private static final String APPLICATION_NAME = "ashomokdev-ocr_me/1.0";
    private final Vision vision;
    private static Logger logger;

    public OcrProcessorImpl() throws IOException, GeneralSecurityException {
        vision = getVisionService();
        logger = Logger.getLogger(OcrProcessorImpl.class.getName());
    }

    /**
     * Connects to the Vision API using Application Default Credentials.
     */
    private static Vision getVisionService() throws IOException, GeneralSecurityException {
        GoogleCredential credential =
                GoogleCredential.getApplicationDefault().createScoped(VisionScopes.all());
        com.google.api.client.json.JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
        return new Vision.Builder(GoogleNetHttpTransport.newTrustedTransport(), jsonFactory, credential)
                .setApplicationName(APPLICATION_NAME)
                .build();
    }

    @Override
    public String ocrForText(byte[] image) throws IOException {
        return ocrForText(image, null);
    }

    @Override
    public String ocrForText(byte[] image, @Nullable List<String> languages) throws IOException {

        BatchAnnotateImagesResponse batchResponse = ocrForResponce(image, languages);
        return extractText(batchResponse);
    }

    @Override
    public PDFData ocrForData(byte[] image) throws IOException {
        return ocrForData(image,null);
    }

    @Override
    public PDFData ocrForData(byte[] image, @Nullable List<String> languages) throws IOException {

        BatchAnnotateImagesResponse batchResponse = ocrForResponce(image, languages);

        List<TextUnit> data;
        ImageDimensions imageDimensions;
        PDFData result;
        try {
            data = extractData(batchResponse);
            imageDimensions = extractImageDimensions(batchResponse);
            result = new PDFData(imageDimensions.height, imageDimensions.width, data);
        } catch (Exception e) {
            e.printStackTrace();
            data = new ArrayList<>();
            int defaultHeight = 100;
            int defaultWidth = 100;
            result = new PDFData(defaultHeight, defaultWidth, data);
            result.setHasError(true);
            result.setErrorMessage(e.getMessage());
        }
        return result;
    }

    private ImageDimensions extractImageDimensions(BatchAnnotateImagesResponse batchResponse) {
        ImageDimensions result = new ImageDimensions();

        ArrayMap fullTextAnnotation = (ArrayMap) batchResponse.getResponses().get(0)
                .getOrDefault("fullTextAnnotation", null);
        ArrayList pages = (ArrayList) fullTextAnnotation.get("pages");
        ArrayMap pageProperties = (ArrayMap) pages.get(0);
        int width = ((BigDecimal) pageProperties.get("width")).intValue();
        int height = ((BigDecimal) pageProperties.get("height")).intValue();

        result.width = width;
        result.height = height;
        return result;
    }

    private BatchAnnotateImagesResponse ocrForResponce(byte[] image, @Nullable List<String> languages) throws IOException {
        AnnotateImageRequest request =
                new AnnotateImageRequest()
                        .setImage(new Image().encodeContent(image))
                        .setFeatures(ImmutableList.of(
                                new Feature()
                                        .setType("DOCUMENT_TEXT_DETECTION")
                                        .setMaxResults(1)));

        if (languages != null && languages.size() > 0) {
            ImageContext imageContext = new ImageContext();
            imageContext.setLanguageHints(languages);
            request.setImageContext(imageContext);
        }

        Vision.Images.Annotate annotate =
                vision.images()
                        .annotate(new BatchAnnotateImagesRequest().setRequests(ImmutableList.of(request)));
        // Due to a bug: requests to Vision API containing large images fail when GZipped.
        annotate.setDisableGZipContent(true);

        BatchAnnotateImagesResponse batchResponse = annotate.execute();
        assert batchResponse.getResponses().size() == 1;

        return batchResponse;
    }


    private String extractText(BatchAnnotateImagesResponse response) {
        String message = "";

        List<EntityAnnotation> texts = response.getResponses().get(0).getTextAnnotations();
        if (texts != null && texts.size() > 0) {
            message += texts.get(0).getDescription();
        }
        return message;
    }

    private List<TextUnit> extractData(BatchAnnotateImagesResponse response) throws Exception {
        List<TextUnit> data = new ArrayList<>();
        try {
            for (AnnotateImageResponse res : response.getResponses()) {
                if (null != res.getError()) {
                    String errorMessage = res.getError().getMessage();
                    logger.log(Level.WARNING, "AnnotateImageResponse ERROR: " + errorMessage);
                    throw new Exception("AnnotateImageResponse ERROR: " + errorMessage);
                } else {
                    List<EntityAnnotation> texts = response.getResponses().get(0).getTextAnnotations();
                    if (texts != null && texts.size() > 0) {
                        for (int i = 1; i < texts.size(); i++) {//exclude first text - it contains all text of the page

                            String blockText = texts.get(i).getDescription();
                            BoundingPoly poly = texts.get(i).getBoundingPoly();

                            float llx = (poly.getVertices().get(0).getX() + poly.getVertices().get(3).getX()) / 2;
                            float lly = (poly.getVertices().get(0).getY() + poly.getVertices().get(1).getY()) / 2;
                            float urx = (poly.getVertices().get(2).getX() + poly.getVertices().get(1).getX()) / 2;
                            float ury = (poly.getVertices().get(2).getY() + poly.getVertices().get(3).getY()) / 2;

                            data.add(new TextUnit(blockText, llx, lly, urx, ury));
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
        return data;
    }

    private static final class ImageDimensions {
        private int width;
        private int height;
    }
}
