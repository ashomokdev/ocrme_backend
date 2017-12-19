package ocrme_backend.servlets.list_ocr_requests;

import ocrme_backend.datastore.gcloud_datastore.objects.OcrRequest;
import ocrme_backend.datastore.gcloud_datastore.objects.Result;
import ocrme_backend.servlets.ocr.OcrResponse;

import java.io.Serializable;
import java.util.List;

/**
 * Created by iuliia on 12/18/17.
 */
public class ListOCRResponse implements Serializable {
    String endCursor;
    List<OcrRequest> requestList;
    private Status status;

    public enum Status {
        OK,
        USER_NOT_FOUND,
        UNKNOWN_ERROR
    }

    public String getEndCursor() {
        return endCursor;
    }

    public void setEndCursor(String endCursor) {
        this.endCursor = endCursor;
    }

    public List<OcrRequest> getRequestList() {
        return requestList;
    }

    public void setRequestList(List<OcrRequest> requestList) {
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
