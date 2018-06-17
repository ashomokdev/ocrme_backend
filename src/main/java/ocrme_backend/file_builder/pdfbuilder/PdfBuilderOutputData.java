package ocrme_backend.file_builder.pdfbuilder;

/**
 * Created by iuliia on 7/18/17.
 */
public class PdfBuilderOutputData {

    private Status status;

    /**
     * google storage url, example "gs://imagetotext-149919.appspot.com/ru.pdf";
     */
    private String gsUrl;

    /**
     * media url (for downloading)
     * example https://www.googleapis.com/download/storage/v1/b/ocr_me_pdf_results_bucket/o/2017-10-04-18-06-40-130-2017-10-04-18-06-40-130-file.pdf?generation=1507140400342025&alt=media
     */
    private String mediaUrl;

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public String getGsUrl() {
        return gsUrl;
    }

    public void setGsUrl(String gsUrl) {
        this.gsUrl = gsUrl;
    }

    public String getMediaUrl() {
        return mediaUrl;
    }

    public void setMediaUrl(String mediaUrl) {
        this.mediaUrl = mediaUrl;
    }

    public enum Status {
        OK,
        PDF_CAN_NOT_BE_CREATED_LANGUAGE_NOT_SUPPORTED,
        PDF_CAN_NOT_BE_CREATED_EMPTY_DATA,
        UNKNOWN_ERROR
    }
}
