package ocrme_backend.ocr;

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.vision.v1.Vision;
import com.google.api.services.vision.v1.VisionScopes;
import com.google.cloud.vision.spi.v1.ImageAnnotatorClient;
import com.google.cloud.vision.v1.*;
import com.google.protobuf.ByteString;
import ocrme_backend.file_builder.pdfbuilder.PDFData;
import ocrme_backend.file_builder.pdfbuilder.TextUnit;

import javax.annotation.Nullable;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by iuliia on 5/17/17.
 */
public class OCRProcessorImpl implements OCRProcessor {
    private final Vision vision;
    private static final String APPLICATION_NAME = "ashomokdev-ocr_me/1.0";

    public OCRProcessorImpl() throws IOException, GeneralSecurityException {

        vision = getVisionService();
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
    public String ocrForText(byte[] image) throws IOException {
        return ocrForText(image, null);
    }

    @Override
    public String ocrForText(byte[] image, @Nullable List<String> languages) throws IOException {
        String result = "";
        List<AnnotateImageRequest> requests = new ArrayList<>();

        ByteString imgBytes = ByteString.copyFrom(image);

        Image img = Image.newBuilder().setContent(imgBytes).build();
        Feature feat = Feature.newBuilder().setType(Feature.Type.TEXT_DETECTION).build();

        //add languages if needed
        ImageContext imageContext = null;
        if (languages != null && languages.size() > 0) {
            imageContext = ImageContext.newBuilder().addAllLanguageHints(languages).build();
        }

        AnnotateImageRequest request = null;
        if (imageContext != null) {
            request = AnnotateImageRequest.newBuilder()
                    .addFeatures(feat)
                    .setImage(img)
                    .setImageContext(imageContext)
                    .build();
        } else {
            request = AnnotateImageRequest.newBuilder()
                    .addFeatures(feat)
                    .setImage(img)
                    .build();
        }

        requests.add(request);

        BatchAnnotateImagesResponse response =
                ImageAnnotatorClient.create().batchAnnotateImages(requests);
        List<AnnotateImageResponse> responses = response.getResponsesList();

        for (AnnotateImageResponse res : responses) {
            if (res.hasError()) {
                result = res.getError().getMessage();
            } else {
                // For full list of available annotations, see http://g.co/cloud/vision/docs
                TextAnnotation annotation = res.getFullTextAnnotation();
                StringBuilder fullText = new StringBuilder();
                for (Page page : annotation.getPagesList()) {
                    StringBuilder pageText = new StringBuilder();
                    for (Block block : page.getBlocksList()) {
                        StringBuilder blockText = new StringBuilder();
                        for (Paragraph para : block.getParagraphsList()) {
                            StringBuilder paraText = new StringBuilder();
                            for (Word word : para.getWordsList()) {
                                StringBuilder wordText = new StringBuilder();
                                for (Symbol symbol : word.getSymbolsList()) {
                                    wordText.append(symbol.getText());
                                }
                                paraText.append(wordText);
                            }

                            blockText.append(paraText);
                        }
                        pageText.append(blockText);
                    }
                    fullText.append(pageText);
                }
                result = fullText.toString();
            }
        }
        return result;
    }

    @Override
    public PDFData ocrForData(byte[] image, int sourceHeight, int sourceWidth) throws IOException {
        return ocrForData(image, sourceHeight, sourceWidth, null);
    }

    @Override
    public PDFData ocrForData(byte[] image, int sourceHeight, int sourceWidth, @Nullable List<String> languages) throws IOException {
        List<AnnotateImageRequest> requests = new ArrayList<>();

        ByteString imgBytes = ByteString.readFrom(new ByteArrayInputStream(image));

        Image img = Image.newBuilder().setContent(imgBytes).build();

        Feature feat = Feature.newBuilder().setType(Feature.Type.TEXT_DETECTION).build();

        //add languages if needed
        ImageContext imageContext = null;
        if (languages != null && languages.size() > 0) {
            imageContext = ImageContext.newBuilder().addAllLanguageHints(languages).build();
        }

        AnnotateImageRequest request = null;
        if (imageContext != null) {
            request = AnnotateImageRequest.newBuilder()
                    .addFeatures(feat)
                    .setImage(img)
                    .setImageContext(imageContext)
                    .build();
        } else {
            request = AnnotateImageRequest.newBuilder()
                    .addFeatures(feat)
                    .setImage(img)
                    .build();
        }

        requests.add(request);

        BatchAnnotateImagesResponse response =
                ImageAnnotatorClient.create().batchAnnotateImages(requests);
        List<AnnotateImageResponse> responses = response.getResponsesList();

        List<TextUnit> data;
        PDFData result;
        try {
            data = getData(responses);
            result = new PDFData(sourceHeight, sourceWidth, data);
        } catch (Exception e) {
            data = new ArrayList<>();
            result = new PDFData(sourceHeight, sourceWidth, data);
            result.setHasError(true);
            result.setErrorMessage(e.getMessage());
        }
        return result;
    }

    private List<TextUnit> getData(List<AnnotateImageResponse> responses) throws Exception {
        List<TextUnit> data = new ArrayList<>();

        for (AnnotateImageResponse res : responses) {
            if (res.hasError()) {
                throw new Exception(res.getError().getMessage());

            } else {
                // For full list of available annotations, see http://g.co/cloud/vision/docs
                TextAnnotation annotation = res.getFullTextAnnotation();
                for (Page page : annotation.getPagesList()) {
                    for (Block block : page.getBlocksList()) {
                        StringBuilder blockText = new StringBuilder();
                        for (Paragraph para : block.getParagraphsList()) {
                            StringBuilder paraText = new StringBuilder();
                            for (Word word : para.getWordsList()) {
                                StringBuilder wordText = new StringBuilder();
                                for (Symbol symbol : word.getSymbolsList()) {
                                    wordText.append(symbol.getText());
                                }
                                paraText.append(wordText);
                            }
                            BoundingPoly poly = para.getBoundingBox();
                            float llx = (poly.getVertices(0).getX() + poly.getVertices(3).getX()) / 2;
                            float lly = (poly.getVertices(0).getY() + poly.getVertices(1).getY()) / 2;
                            float urx = (poly.getVertices(2).getX() + poly.getVertices(1).getX()) / 2;
                            float ury = (poly.getVertices(2).getY() + poly.getVertices(3).getY()) / 2;

                            data.add(new TextUnit(paraText.toString(), llx, lly, urx, ury));

                            blockText.append(paraText);
                        }
                    }
                }
            }
        }
        return data;
    }
}
