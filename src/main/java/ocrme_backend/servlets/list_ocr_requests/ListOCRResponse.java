package ocrme_backend.servlets.list_ocr_requests;

import ocrme_backend.servlets.ocr.OcrResult;

import java.io.Serializable;
import java.util.List;

/**
 * Created by iuliia on 12/18/17.
 */
public class ListOCRResponse implements Serializable {
    String endCursor;
    List<OcrResult> requestList;
    private Status status;

    public String getEndCursor() {
        return endCursor;
    }

    public void setEndCursor(String endCursor) {
        this.endCursor = endCursor;
    }

    public List<OcrResult> getRequestList() {
        return requestList;
    }

    public void setRequestList(List<OcrResult> requestList) {
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

    public enum Status {
        OK,
        USER_NOT_FOUND,
        UNKNOWN_ERROR
    }
}
