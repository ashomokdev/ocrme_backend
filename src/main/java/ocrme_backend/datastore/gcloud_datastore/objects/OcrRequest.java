package ocrme_backend.datastore.gcloud_datastore.objects;

import com.google.appengine.api.datastore.Text;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Optional;

/**
 * Created by iuliia on 6/19/17.
 */
public class OcrRequest implements Serializable {

    // [START keys]
    public static final String SOURCE_IMAGE_URL = "sourceImageUrl";
    public static final String LANGUAGES = "languages";
    public static final String TEXT_RESULT = "textResult";
    public static final String PDF_RESULT_GS_URL = "pdfResultGsUrl";
    public static final String PDF_RESULT_MEDIA_URL = "pdfResultMediaUrl";
    public static final String ID = "id";
    public static final String TIME_STAMP = "timeStamp";
    public static final String CREATED_BY_ID = "createdById";
    public static final String CREATED_BY = "createdBy";
    public static final String STATUS = "status";
    // [END ocrRequest]
    // [START ocrRequest]
    private String sourceImageUrl;
    private List<String> languages;
    private Text textResult;
    private String pdfResultGsUrl;
    private String pdfResultMediaUrl;
    private Long id;
    private String timeStamp;
    private String createdBy;
    private String createdById;
    private String status;
    // [END keys]

    private OcrRequest(Builder builder) {
        this.sourceImageUrl = builder.inputImageUrl;
        this.languages = builder.languages;
        this.textResult = builder.textResult;
        this.pdfResultGsUrl = builder.pdfResultGsUrl;
        this.pdfResultMediaUrl = builder.pdfResultMediaUrl;
        this.id = builder.id;
        this.createdBy = builder.createdBy;
        this.createdById = builder.createdById;
        this.status = builder.status;
        String timeStamp = builder.timeStamp;
        if (timeStamp == null) {
            timeStamp = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss.SSS").format(new Date());
        }
        this.timeStamp = timeStamp;
    }

    public String getSourceImageUrl() {
        return sourceImageUrl;
    }

    public List<String> getLanguages() {
        return languages;
    }

    public Text getTextResult() {
        return textResult;
    }

    public String getPdfResultGsUrl() {
        return pdfResultGsUrl;
    }

    public String getPdfResultMediaUrl() {
        return pdfResultMediaUrl;
    }

    public Long getId() {
        return id;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public String getCreatedById() {
        return createdById;
    }

    public String getTimeStamp() {
        return timeStamp;
    }

    public String getStatus() {
        return status;
    }

    @Override
    public String toString() {
        return "OcrRequest{" +
                "sourceImageUrl='" + sourceImageUrl + '\'' +
                ", languages=" + languages +
                ", textResult=" + textResult +
                ", pdfResultGsUrl='" + pdfResultGsUrl + '\'' +
                ", pdfResultMediaUrl='" + pdfResultMediaUrl + '\'' +
                ", id=" + id +
                ", timeStamp='" + timeStamp + '\'' +
                ", createdBy='" + createdBy + '\'' +
                ", createdById='" + createdById + '\'' +
                ", status='" + status + '\'' +
                '}';
    }

    public static class Builder {
        private String inputImageUrl;
        private List<String> languages;
        private Text textResult;
        private String pdfResultGsUrl;
        private String pdfResultMediaUrl;
        private Long id;
        private String createdBy;
        private String createdById;
        private String timeStamp;
        private String status;

        public Builder sourceImageUrl(String inputImageUrl) {
            this.inputImageUrl = inputImageUrl;
            return this;
        }

        public Builder languages(List<String> languages) {
            this.languages = languages;
            return this;
        }

        public Builder textResult(Optional<String> textResult) {
            this.textResult = new Text(textResult.orElse(""));
            return this;
        }

        public Builder pdfResultGsUrl(String pdfResultGsUrl) {
            this.pdfResultGsUrl = pdfResultGsUrl;
            return this;
        }

        public Builder pdfResultMediaUrl(String pdfResultMediaUrl) {
            this.pdfResultMediaUrl = pdfResultMediaUrl;
            return this;
        }

        public Builder status(String status) {
            this.status = status;
            return this;
        }

        public Builder id(Long id) {
            this.id = id;
            return this;
        }

        public Builder createdBy(String createdBy) {
            this.createdBy = createdBy;
            return this;
        }

        public Builder createdById(String createdById) {
            this.createdById = createdById;
            return this;
        }

        public Builder timeStamp(String timeStamp) {
            this.timeStamp = timeStamp;
            return this;
        }

        public OcrRequest build() {
            return new OcrRequest(this);
        }
    }
}
