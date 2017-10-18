package ocrme_backend.ocr;

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
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
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import static java.lang.Math.cos;
import static java.lang.Math.sin;

/**
 * Created by iuliia on 7/11/17.
 * docs https://cloud.google.com/vision/docs/languages
 */
public class OcrProcessorImpl implements OCRProcessor {

    private static final String APPLICATION_NAME = "ashomokdev-ocr_me/1.0";
    private static final int EXIF_ORIENTATION_NORMAL = 1;
    private static final int EXIF_ORIENTATION_270_DEGREE = 6;
    private static final int EXIF_ORIENTATION_90_DEGREE = 8;
    private static final int EXIF_ORIENTATION_180_DEGREE = 3;
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
                PdfBuilderInputData result = new PdfBuilderInputData(textUnits);
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
                PdfBuilderInputData result = new PdfBuilderInputData(textUnits);
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

    private BatchAnnotateImagesResponse ocrForResponse(byte[] image, @Nullable List<String> languages)
            throws IOException {
        AnnotateImageRequest request =
                new AnnotateImageRequest()
                        .setImage(new Image().encodeContent(image))
                        .setFeatures(ImmutableList.of(
                                new Feature()
                                        .setType("TEXT_DETECTION")
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
                                        .setType("TEXT_DETECTION")
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

    /**
     * 1        2       3      4         5            6           7          8
     * <p>
     * 888888  888888      88  88      8888888888  88                  88  8888888888
     * 88          88      88  88      88  88      88  88          88  88      88  88
     * 8888      8888    8888  8888    88          8888888888  8888888888          88
     * 88          88      88  88
     * 88          88  888888  888888
     *
     * @param ea The input EntityAnnotation must be NOT from the first EntityAnnotation of
     *           annotateImageResponse.getTextAnnotations(), because it is not affected by
     *           image orientation.
     * @return Exif orientation (1 or 3 or 6 or 8)
     */

    public static int getExifOrientation(EntityAnnotation ea) {
        List<Vertex> vertexList = ea.getBoundingPoly().getVertices();
        // Calculate the center
        float centerX = 0, centerY = 0;
        for (int i = 0; i < 4; i++) {
            centerX += vertexList.get(i).getX();
            centerY += vertexList.get(i).getY();
        }
        centerX /= 4;
        centerY /= 4;

        int x0 = vertexList.get(0).getX();
        int y0 = vertexList.get(0).getY();

        if (x0 < centerX) {
            if (y0 < centerY) {
                //       0 -------- 1
                //       |          |
                //       3 -------- 2
                return EXIF_ORIENTATION_NORMAL; // 1
            } else {
                //       1 -------- 2
                //       |          |
                //       0 -------- 3
                return EXIF_ORIENTATION_270_DEGREE; // 6
            }
        } else {
            if (y0 < centerY) {
                //       3 -------- 0
                //       |          |
                //       2 -------- 1
                return EXIF_ORIENTATION_90_DEGREE; // 8
            } else {
                //       2 -------- 3
                //       |          |
                //       1 -------- 0
                return EXIF_ORIENTATION_180_DEGREE; // 3
            }
        }
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
                if (texts.size() > 0) {

                    //get orientation
                    EntityAnnotation allText = texts.get(0); //all text of the page
                    int orientation;
                    try {
                        orientation = getExifOrientation(allText);
                    } catch (NullPointerException e) {
                        try {
                            orientation = getExifOrientation(texts.get(1));
                        } catch (NullPointerException e1) {
                            orientation = EXIF_ORIENTATION_NORMAL;
                        }
                    }
                    logger.log(Level.INFO, "orientation: " + orientation);

                    // Calculate the center
                    float centerX = 0, centerY = 0;
                    for (Vertex vertex : allText.getBoundingPoly().getVertices()) {
                        if (vertex.getX() != null) {
                            centerX += vertex.getX();
                        }
                        if (vertex.getY() != null) {
                            centerY += vertex.getY();
                        }
                    }
                    centerX /= 4;
                    centerY /= 4;


                    for (int i = 1; i < texts.size(); i++) {//exclude first text - it contains all text of the page

                        String blockText = texts.get(i).getDescription();
                        BoundingPoly poly = texts.get(i).getBoundingPoly();

                        try {
                            float llx = 0;
                            float lly = 0;
                            float urx = 0;
                            float ury = 0;
                            if (orientation == EXIF_ORIENTATION_NORMAL) {
                                poly = invertSymmetricallyByY(centerY, poly);
                                llx = (poly.getVertices().get(0).getX() + poly.getVertices().get(3).getX()) / 2;
                                lly = (poly.getVertices().get(0).getY() + poly.getVertices().get(1).getY()) / 2;
                                urx = (poly.getVertices().get(2).getX() + poly.getVertices().get(1).getX()) / 2;
                                ury = (poly.getVertices().get(2).getY() + poly.getVertices().get(3).getY()) / 2;
                            } else if (orientation == EXIF_ORIENTATION_90_DEGREE) {
                                poly = rotate(centerX, centerY, poly, Math.toRadians(-90));
                                llx = (poly.getVertices().get(1).getX() + poly.getVertices().get(2).getX()) / 2;
                                lly = (poly.getVertices().get(2).getY() + poly.getVertices().get(3).getY()) / 2;
                                urx = (poly.getVertices().get(0).getX() + poly.getVertices().get(3).getX()) / 2;
                                ury = (poly.getVertices().get(0).getY() + poly.getVertices().get(1).getY()) / 2;
                            }


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

    /**
     * rotate rectangular clockwise
     *
     * @param poly
     * @param theta the angle of rotation in radians
     * @return
     */
    public BoundingPoly rotate(float centerX, float centerY, BoundingPoly poly, double theta) {

        List<Vertex> vertexList = poly.getVertices();

        //rotate all vertices in poly
        for (Vertex vertex : vertexList) {
            float tempX = vertex.getX() - centerX;
            float tempY = vertex.getY() - centerY;

            // now apply rotation
            float rotatedX = (float) (centerX - tempX * cos(theta) + tempY * sin(theta));
            float rotatedY = (float) (centerX - tempX * sin(theta) - tempY * cos(theta));

            vertex.setX((int) rotatedX);
            vertex.setY((int) rotatedY);
        }
        return poly;
    }

    /**
     * since Google Vision Api returns boundingPoly-s when Coordinates starts from top left corner,
     * but Itext uses coordinate system with bottom left start position -
     * we need invert the result for continue to work with itext.
     *
     * @return text units inverted symmetrically by 0X coordinates.
     */
    private BoundingPoly invertSymmetricallyByY(float centerY, BoundingPoly poly) {

        List<Vertex> vertices = poly.getVertices();
        for (Vertex v : vertices) {
            if (v.getY() != null) {
                v.setY((int) (centerY + (centerY - v.getY())));
            }
        }
        return poly;
    }

    public class AnnotateImageResponseException extends Exception {
        AnnotateImageResponseException(String s) {
            super(s);
        }
    }
}
