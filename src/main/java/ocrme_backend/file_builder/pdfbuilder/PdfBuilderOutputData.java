package ocrme_backend.file_builder.pdfbuilder;

/**
 * Created by iuliia on 7/18/17.
 */
public class PdfBuilderOutputData {

    private Status status;
    private String url;

    public enum Status {
        OK,
        PDF_CAN_NOT_BE_CREATED_LANGUAGE_NOT_SUPPORTED,
        PDF_CAN_NOT_BE_CREATED_EMPTY_DATA
    }


    public Status getStatus() {
        return status;
    }

    public String getUrl() {
        return url;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
