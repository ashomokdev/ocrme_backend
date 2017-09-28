package ocrme_backend.servlets.ocr;

/**
 * Created by iuliia on 9/28/17.
 */
public class OcrRequestBean {
    private String[] languages;

    private String gcsImageUri;

    public String[] getLanguages() {
        return languages;
    }

    public String getGcsImageUri() {
        return gcsImageUri;
    }

}
