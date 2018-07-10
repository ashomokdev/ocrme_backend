package ocrme_backend.servlets.translate.translate;


import ocrme_backend.servlets.ocr.OcrResult;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;

public class TranslateResult implements Serializable {
    private String sourceLanguageCode;
    private String targetLanguageCode;
    private String textResult;
    private String timeStamp;
    private Long id;

    private TranslateResult(Builder builder) {
        this.sourceLanguageCode = builder.sourceLanguageCode;
        this.targetLanguageCode = builder.targetLanguageCode;
        this.textResult = builder.textResult;
        this.id = builder.id;
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

    public String getTextResult() {
        return textResult;
    }

    public String getTimeStamp() {
        return timeStamp;
    }

    public Long getId() {
        return id;
    }

    @Override
    public String toString() {
        return "TranslateResult{" +
                "sourceLanguageCode='" + sourceLanguageCode + '\'' +
                ", targetLanguageCode='" + targetLanguageCode + '\'' +
                ", textResult='" + textResult + '\'' +
                ", timeStamp='" + timeStamp + '\'' +
                ", id=" + id +
                '}';
    }

    public static class Builder {
        private String sourceLanguageCode;
        private String targetLanguageCode;
        private String textResult;
        private Long id;
        private String timeStamp;

        Builder sourceLanguageCode(String sourceLanguageCode){
            this.sourceLanguageCode = sourceLanguageCode;
            return this;
        }

        Builder targetLanguageCode(String targetLanguageCode){
            this.targetLanguageCode =targetLanguageCode;
            return this;
        }

         Builder textResult(String textResult){
            this.textResult = textResult;
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

        public TranslateResult build() {
            return new TranslateResult(this);
        }

    }
}
