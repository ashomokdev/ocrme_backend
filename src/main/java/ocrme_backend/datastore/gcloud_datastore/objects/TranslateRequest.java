package ocrme_backend.datastore.gcloud_datastore.objects;


import com.google.appengine.api.datastore.Text;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Optional;

public class TranslateRequest implements Serializable {

    // [START keys]
    public static final String SOURCE_LANGUAGE_CODE = "sourceLanguageCode";
    public static final String TARGET_LANGUAGE_CODE = "targetLanguageCode";
    public static final String SOURCE_TEXT = "sourceText";
    public static final String TARGET_TEXT = "targetText";
    public static final String ID = "id";
    public static final String TIME_STAMP = "timeStamp";
    public static final String CREATED_BY_ID = "createdById";
    public static final String CREATED_BY = "createdBy";
    public static final String STATUS = "status";
    // [END keys]

    // [START translateRequest]
    private String sourceLanguageCode;
    private String targetLanguageCode;
    private Text sourceText;
    private Text targetText;
    private Long id;
    private String timeStamp;
    private String createdBy;
    private String createdById;
    private String status;
    // [END translateRequest]
    

    private TranslateRequest(TranslateRequest.Builder builder) {
        this.sourceLanguageCode = builder.sourceLanguageCode;
        this.targetLanguageCode = builder.targetLanguageCode;
        this.sourceText = builder.sourceText;
        this.targetText = builder.targetText;
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

    public String getSourceLanguageCode() {
        return sourceLanguageCode;
    }

    public String getTargetLanguageCode() {
        return targetLanguageCode;
    }

    public Text getSourceText() {
        return sourceText;
    }

    public Text getTargetText() {
        return targetText;
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
        return "TranslateRequest{" +
                "sourceLanguageCode='" + sourceLanguageCode + '\'' +
                ", targetLanguageCode='" + targetLanguageCode + '\'' +
                ", sourceText='" + sourceText + '\'' +
                ", targetText='" + targetText + '\'' +
                ", id=" + id +
                ", timeStamp='" + timeStamp + '\'' +
                ", createdBy='" + createdBy + '\'' +
                ", createdById='" + createdById + '\'' +
                ", status='" + status + '\'' +
                '}';
    }

    public static class Builder {
        private String sourceLanguageCode;
        private String targetLanguageCode;
        private Text sourceText;
        private Text targetText;
        private Long id;
        private String timeStamp;
        private String createdBy;
        private String createdById;
        private String status;

        public TranslateRequest.Builder sourceLanguageCode(String sourceLanguageCode) {
            this.sourceLanguageCode = sourceLanguageCode;
            return this;
        }

        public TranslateRequest.Builder targetLanguageCode(String targetLanguageCode) {
            this.targetLanguageCode = targetLanguageCode;
            return this;
        }

        public TranslateRequest.Builder sourceText(Optional<String> sourceText) {
            this.sourceText = new Text(sourceText.orElse(""));
            return this;
        }


        public TranslateRequest.Builder targetText(Optional<String>  targetText) {
            this.targetText = new Text(targetText.orElse(""));
            return this;
        }

        public TranslateRequest.Builder status(String status) {
            this.status = status;
            return this;
        }

        public TranslateRequest.Builder id(Long id) {
            this.id = id;
            return this;
        }

        public TranslateRequest.Builder createdBy(String createdBy) {
            this.createdBy = createdBy;
            return this;
        }

        public TranslateRequest.Builder createdById(String createdById) {
            this.createdById = createdById;
            return this;
        }

        public TranslateRequest.Builder timeStamp(String timeStamp) {
            this.timeStamp = timeStamp;
            return this;
        }

        public TranslateRequest build() {
            return new TranslateRequest(this);
        }
    }
}
