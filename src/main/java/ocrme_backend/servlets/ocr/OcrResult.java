package ocrme_backend.servlets.ocr;

import ocrme_backend.datastore.gcloud_datastore.objects.OcrRequest;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * Created by iuliia on 1/22/18.
 */
public class OcrResult implements Serializable {
    private String sourceImageUrl;
    private List<String> languages;
    private String textResult;
    private String pdfResultGsUrl;
    private String pdfResultMediaUrl;
    private Long id;
    private String timeStamp;

    @Override
    public String toString() {
        return "OcrResult{" +
                "sourceImageUrl='" + sourceImageUrl + '\'' +
                ", languages=" + languages +
                ", textResult='" + textResult + '\'' +
                ", pdfResultGsUrl='" + pdfResultGsUrl + '\'' +
                ", pdfResultMediaUrl='" + pdfResultMediaUrl + '\'' +
                ", id=" + id +
                ", timeStamp='" + timeStamp + '\'' +
                '}';
    }

    private OcrResult(Builder builder) {
        this.sourceImageUrl = builder.inputImageUrl;
        this.languages = builder.languages;
        this.textResult = builder.textResult;
        this.pdfResultGsUrl = builder.pdfResultGsUrl;
        this.pdfResultMediaUrl = builder.pdfResultMediaUrl;
        this.id = builder.id;
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

    public String getTextResult() {
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

    public String getTimeStamp() {
        return timeStamp;
    }


    public static class Builder {
        private String inputImageUrl;
        private List<String> languages;
        private String textResult;
        private String pdfResultGsUrl;
        private String pdfResultMediaUrl;
        private Long id;
        private String timeStamp;

        Builder sourceImageUrl(String inputImageUrl) {
            this.inputImageUrl = inputImageUrl;
            return this;
        }

        Builder languages(List<String> languages) {
            this.languages = languages;
            return this;
        }

        Builder textResult(String textResult) {
            this.textResult = textResult;
            return this;
        }

        Builder pdfResultGsUrl(String pdfResultGsUrl) {
            this.pdfResultGsUrl = pdfResultGsUrl;
            return this;
        }

        Builder pdfResultMediaUrl(String pdfResultMediaUrl) {
            this.pdfResultMediaUrl = pdfResultMediaUrl;
            return this;
        }

        Builder id(Long id) {
            this.id = id;
            return this;
        }

         Builder timeStamp(String timeStamp) {
            this.timeStamp = timeStamp;
            return this;
        }

        public OcrResult build() {
            return new OcrResult(this);
        }
    }

    public static class Converter {
        public static OcrResult convert(OcrRequest ocrRequest) {
            return new Builder()
                    .sourceImageUrl(ocrRequest.getSourceImageUrl())
                    .languages(ocrRequest.getLanguages())
                    .textResult(ocrRequest.getTextResult().getValue())
                    .pdfResultGsUrl(ocrRequest.getPdfResultGsUrl())
                    .pdfResultMediaUrl(ocrRequest.getPdfResultMediaUrl())
                    .id(ocrRequest.getId())
                    .timeStamp(ocrRequest.getTimeStamp())
                    .build();
        }
    }

}
