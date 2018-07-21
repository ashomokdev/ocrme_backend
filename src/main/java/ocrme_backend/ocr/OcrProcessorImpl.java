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
import java.util.Collections;
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
    private static Logger logger;
    private final Vision vision;

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

    private static int getExifOrientation(EntityAnnotation ea) {
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

    @SuppressWarnings("Duplicates")
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
                    EntityAnnotation first_word = texts.get(1);
                    int orientation;
                    try {
                        orientation = getExifOrientation(first_word);
                    } catch (NullPointerException e) {
                        try {
                            orientation = getExifOrientation(texts.get(2));
                        } catch (NullPointerException e1) {
                            orientation = EXIF_ORIENTATION_NORMAL;
                        }
                    }
                    logger.log(Level.FINE, "orientation: " + orientation);

                    // Calculate the center
                    float centerX = 0, centerY = 0;
                    for (Vertex vertex : first_word.getBoundingPoly().getVertices()) {
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
                                poly = invertSymmetricallyBy0X(centerY, poly);
                                llx = getLlx(poly);
                                lly = getLly(poly);
                                urx = getUrx(poly);
                                ury = getUry(poly);
                            } else if (orientation == EXIF_ORIENTATION_90_DEGREE) {
                                //invert by x
                                poly = rotate(centerX, centerY, poly, Math.toRadians(-90));
                                poly = invertSymmetricallyBy0Y(centerX, poly);
                                llx = getLlx(poly);
                                lly = getLly(poly);
                                urx = getUrx(poly);
                                ury = getUry(poly);
                            } else if (orientation == EXIF_ORIENTATION_180_DEGREE) {
                                poly = rotate(centerX, centerY, poly, Math.toRadians(-180));
                                poly = invertSymmetricallyBy0Y(centerX, poly);
                                llx = getLlx(poly);
                                lly = getLly(poly);
                                urx = getUrx(poly);
                                ury = getUry(poly);
                            } else if (orientation == EXIF_ORIENTATION_270_DEGREE) {
                                //invert by x
                                poly = rotate(centerX, centerY, poly, Math.toRadians(-270));
                                poly = invertSymmetricallyBy0Y(centerX, poly);
                                llx = getLlx(poly);
                                lly = getLly(poly);
                                urx = getUrx(poly);
                                ury = getUry(poly);
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


    private float getLlx(BoundingPoly poly) {
        try {
            List<Vertex> vertices = poly.getVertices();

            ArrayList<Float> xs = new ArrayList<>();
            for (Vertex v : vertices) {
                float x = 0;
                if (v.getX() != null) {
                    x = v.getX();
                }
                xs.add(x);
            }

            Collections.sort(xs);
            float llx = (xs.get(0) + xs.get(1)) / 2;
            return llx;
        } catch (Exception e) {
            return 0;
        }
    }

    private float getLly(BoundingPoly poly) {
        try {
            List<Vertex> vertices = poly.getVertices();

            ArrayList<Float> ys = new ArrayList<>();
            for (Vertex v : vertices) {
                float y = 0;
                if (v.getY() != null) {
                    y = v.getY();
                }
                ys.add(y);
            }

            Collections.sort(ys);
            float lly = (ys.get(0) + ys.get(1)) / 2;
            return lly;
        } catch (Exception e) {
            return 0;
        }
    }

    private float getUrx(BoundingPoly poly) {
        try {
            List<Vertex> vertices = poly.getVertices();

            ArrayList<Float> xs = new ArrayList<>();
            for (Vertex v : vertices) {
                float x = 0;
                if (v.getX() != null) {
                    x = v.getX();
                }
                xs.add(x);
            }

            Collections.sort(xs);
            float urx = (xs.get(xs.size() - 1) + xs.get(xs.size() - 2)) / 2;
            return urx;
        } catch (Exception e) {
            return 0;
        }
    }

    private float getUry(BoundingPoly poly) {
        try {
            List<Vertex> vertices = poly.getVertices();

            ArrayList<Float> ys = new ArrayList<>();
            for (Vertex v : vertices) {
                float y = 0;
                if (v.getY() != null) {
                    y = v.getY();
                }
                ys.add(y);
            }

            Collections.sort(ys);
            float ury = (ys.get(ys.size() - 1) + ys.get(ys.size() - 2)) / 2;
            return ury;
        } catch (Exception e) {
            return 0;
        }
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
    private BoundingPoly invertSymmetricallyBy0X(float centerY, BoundingPoly poly) {

        List<Vertex> vertices = poly.getVertices();
        for (Vertex v : vertices) {
            if (v.getY() != null) {
                v.setY((int) (centerY + (centerY - v.getY())));
            }
        }
        return poly;
    }

    /**
     * @param centerX
     * @param poly
     * @return text units inverted symmetrically by 0Y coordinates.
     */
    private BoundingPoly invertSymmetricallyBy0Y(float centerX, BoundingPoly poly) {
        List<Vertex> vertices = poly.getVertices();
        for (Vertex v : vertices) {
            if (v.getX() != null) {
                v.setX((int) (centerX + (centerX - v.getX())));
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
