package ocrme_backend.ocr;

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.ArrayMap;
import com.google.api.services.vision.v1.Vision;
import com.google.api.services.vision.v1.VisionScopes;
import com.google.api.services.vision.v1.model.*;
import com.google.common.collect.ImmutableList;
import ocrme_backend.file_builder.pdfbuilder.PdfBuilderInputData;
import ocrme_backend.file_builder.pdfbuilder.TextUnit;
import ocrme_backend.servlets.ocr.OcrData;
import org.apache.commons.fileupload.FileUploadException;

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
    public @Nullable
    String ocrForText(byte[] image) throws IOException {
        return ocrForText(image, null);
    }

    @Override
    public @Nullable
    String ocrForText(byte[] image, @Nullable List<String> languages) throws IOException {
        try {
            if (image == null) {
                throw new FileUploadException("Can not get file");
            }
            BatchAnnotateImagesResponse batchResponse = ocrForResponse(image, languages);
            return extractText(batchResponse);
        } catch (Exception e) {
            logger.log(Level.WARNING, "ERROR! See log below.");
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public OcrData ocrForData(byte[] image) throws IOException {
        return ocrForData(image, null);
    }

    @Override
    public OcrData ocrForData(byte[] image, @Nullable List<String> languages) throws IOException {
        OcrData data;
        try {
            if (image == null) {
                throw new FileUploadException("Can not get file");
            }
            BatchAnnotateImagesResponse batchResponse = ocrForResponse(image, languages);

            String simpleText = extractText(batchResponse);
            if (simpleText == null || simpleText.length() == 0) {
                data = new OcrData(null, null, OcrData.Status.TEXT_NOT_FOUND);
            } else {
                List<TextUnit> textUnits = extractData(batchResponse);
                ImageDimensions imageDimensions = extractImageDimensions(batchResponse);
                PdfBuilderInputData result = new PdfBuilderInputData(imageDimensions.height, imageDimensions.width, textUnits);
                data = new OcrData(result, simpleText, OcrData.Status.OK);
            }
        } catch (FileUploadException | IOException e) {
            e.printStackTrace();
            data = new OcrData(null, null, OcrData.Status.UNKNOWN_ERROR);
        } catch (AnnotateImageResponseException e) {
            e.printStackTrace();
            data = new OcrData(null, null, OcrData.Status.INVALID_LANGUAGE_HINTS);
        }
        return data;
    }

    @Override
    public OcrData ocrForData(String gcsImageUri) {
        return ocrForData(gcsImageUri, null);
    }

    @Override
    public OcrData ocrForData(String gcsImageUri, @Nullable List<String> languages) {
        OcrData data;
        try {
            if (gcsImageUri == null) {
                throw new FileUploadException("Can not get file");
            }
            BatchAnnotateImagesResponse batchResponse = ocrForResponse(gcsImageUri, languages);

            String simpleText = extractText(batchResponse);
            if (simpleText == null || simpleText.length() == 0) {
                data = new OcrData(null, null, OcrData.Status.TEXT_NOT_FOUND);
            } else {
                List<TextUnit> textUnits = extractData(batchResponse);
                ImageDimensions imageDimensions = extractImageDimensions(batchResponse);
                PdfBuilderInputData result = new PdfBuilderInputData(imageDimensions.height, imageDimensions.width, textUnits);
                data = new OcrData(result, simpleText, OcrData.Status.OK);
            }
        } catch (FileUploadException | IOException e) {
            e.printStackTrace();
            data = new OcrData(null, null, OcrData.Status.UNKNOWN_ERROR);
        } catch (AnnotateImageResponseException e) {
            e.printStackTrace();
            data = new OcrData(null, null, OcrData.Status.INVALID_LANGUAGE_HINTS);
        }
        return data;
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

    private BatchAnnotateImagesResponse ocrForResponse(byte[] image, @Nullable List<String> languages)
            throws IOException {
        AnnotateImageRequest request =
                new AnnotateImageRequest()
                        .setImage(new Image().encodeContent(image))
                        .setFeatures(ImmutableList.of(
                                new Feature()
                                        .setType("DOCUMENT_TEXT_DETECTION")
                                        .setMaxResults(1)));

        return getBatchAnnotateImagesResponse(languages, request);
    }

    /**
     * @param imageUrl  url from firebase storage, example https://firebasestorage.googleapis.com/v0/b/test-afc85.appspot.com/o/d2d7dfc0-662e-451a-86ae-1b1da98030b4?alt=media&token=e62047f3-4363-4417-9d85-327842ce3e87
     * @param languages
     * @return
     * @throws IOException
     */
    public BatchAnnotateImagesResponse ocrForResponse(String imageUrl, @Nullable List<String> languages)
            throws IOException {
        AnnotateImageRequest request =
                new AnnotateImageRequest()
                        .setImage(new Image().setSource(new ImageSource().setGcsImageUri(imageUrl)))
                        .setFeatures(ImmutableList.of(
                                new Feature()
                                        .setType("DOCUMENT_TEXT_DETECTION")
                                        .setMaxResults(1)));

        return getBatchAnnotateImagesResponse(languages, request);
    }

    private BatchAnnotateImagesResponse getBatchAnnotateImagesResponse(
            @Nullable List<String> languages, AnnotateImageRequest request) throws IOException {
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


    private String extractText(BatchAnnotateImagesResponse response) throws AnnotateImageResponseException {
        String message = "";
        AnnotateImageResponse res = response.getResponses().get(0);
        if (null != res.getError()) {
            String errorMessage = res.getError().getMessage();
            logger.log(Level.WARNING, "AnnotateImageResponse ERROR: " + errorMessage);
            throw new AnnotateImageResponseException("AnnotateImageResponse ERROR: " + errorMessage);
        } else {
            List<EntityAnnotation> texts = res.getTextAnnotations();
            if (texts != null && texts.size() > 0) {
                message += texts.get(0).getDescription();
            }
        }
        return message;
    }

    List<TextUnit> extractData(BatchAnnotateImagesResponse response) throws AnnotateImageResponseException {
        List<TextUnit> data = new ArrayList<>();

        for (AnnotateImageResponse res : response.getResponses()) {
            if (null != res.getError()) {
                String errorMessage = res.getError().getMessage();
                logger.log(Level.WARNING, "AnnotateImageResponse ERROR: " + errorMessage);
                throw new AnnotateImageResponseException("AnnotateImageResponse ERROR: " + errorMessage);
            } else {
                List<EntityAnnotation> texts = response.getResponses().get(0).getTextAnnotations();
                if (texts != null && texts.size() > 0) {
                    for (int i = 1; i < texts.size(); i++) {//exclude first text - it contains all text of the page

                        String blockText = texts.get(i).getDescription();
                        BoundingPoly poly = texts.get(i).getBoundingPoly();

                        try {
                            float llx = (poly.getVertices().get(0).getX() + poly.getVertices().get(3).getX()) / 2;
                            float lly = (poly.getVertices().get(0).getY() + poly.getVertices().get(1).getY()) / 2;
                            float urx = (poly.getVertices().get(2).getX() + poly.getVertices().get(1).getX()) / 2;
                            float ury = (poly.getVertices().get(2).getY() + poly.getVertices().get(3).getY()) / 2;

                            data.add(new TextUnit(blockText, llx, lly, urx, ury));
                        } catch (NullPointerException e) {
                            //ignore - some polys has not X or Y coordinate if text located closed to bounds.
                        }
                    }
                }
            }
        }
        return data;
    }

    private static final class ImageDimensions {
        private int width;
        private int height;
    }

    public class AnnotateImageResponseException extends Exception {
        AnnotateImageResponseException(String s) {
            super(s);
        }
    }
}
