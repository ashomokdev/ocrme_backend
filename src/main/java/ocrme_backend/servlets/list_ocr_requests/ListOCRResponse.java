package ocrme_backend.servlets.list_ocr_requests;

import ocrme_backend.datastore.gcloud_datastore.objects.OcrRequest;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;

/**
 * Created by iuliia on 12/18/17.
 */
public class ListOCRResponse implements Serializable {
    String endCursor;
    List<MyDoc> requestList;
    private Status status;

    public enum Status {
        OK,
        USER_NOT_FOUND,
        UNKNOWN_ERROR
    }

    public static class MyDoc {
        private String sourceImageUrl;
        private String[] languages;
        private String textResult;
        private String pdfResultGsUrl;
        private String pdfResultMediaUrl;
        private Long id;
        private String timeStamp;

        @Override
        public String toString() {
            return "OcrRequest{" +
                    "sourceImageUrl='" + sourceImageUrl + '\'' +
                    ", languages=" + Arrays.toString(languages) +
                    ", textResult='" + textResult + '\'' +
                    ", pdfResultGsUrl='" + pdfResultGsUrl + '\'' +
                    ", pdfResultMediaUrl='" + pdfResultMediaUrl + '\'' +
                    ", id=" + id +
                    ", timeStamp='" + timeStamp + '\'' +
                    '}';
        }

        public MyDoc(OcrRequest ocrRequest) {
            this.sourceImageUrl = ocrRequest.getSourceImageUrl();
            this.languages = ocrRequest.getLanguages();
            this.textResult = ocrRequest.getTextResult().getValue();
            this.pdfResultGsUrl = ocrRequest.getPdfResultGsUrl();
            this.pdfResultMediaUrl = ocrRequest.getPdfResultMediaUrl();
            this.id = ocrRequest.getId();
            this.timeStamp = ocrRequest.getTimeStamp();
        }
    }

    public String getEndCursor() {
        return endCursor;
    }

    public void setEndCursor(String endCursor) {
        this.endCursor = endCursor;
    }

    public List<MyDoc> getRequestList() {
        return requestList;
    }

    public void setRequestList(List<MyDoc> requestList) {
        this.requestList = requestList;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "ListOCRResponse{" +
                "endCursor='" + endCursor + '\'' +
                ", requestList=" + requestList +
                ", status=" + status +
                '}';
    }
}
