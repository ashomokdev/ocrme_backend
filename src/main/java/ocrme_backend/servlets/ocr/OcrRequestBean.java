package ocrme_backend.servlets.ocr;

/**
 * Created by iuliia on 9/28/17.
 */
public class OcrRequestBean {
    private String[] languages;

    private String gcsImageUri; //example "gs://ocrme-77a2b.appspot.com/ocr_request_images/000c121b-357d-4ac0-a3f2-24e0f6d5cea185dffb40-e754-478f-b5b7-850fab211438.jpg"

    private String idTokenString;

    public String[] getLanguages() {
        return languages;
    }

    public String getGcsImageUri() {
        return gcsImageUri;
    }

    public String getIdTokenString() {
        return idTokenString;
    }

    public void setLanguages(String[] languages) {
        this.languages = languages;
    }

    public void setGcsImageUri(String gcsImageUri) {
        this.gcsImageUri = gcsImageUri;
    }

    public void setIdTokenString(String idTokenString) {
        this.idTokenString = idTokenString;
    }

}
