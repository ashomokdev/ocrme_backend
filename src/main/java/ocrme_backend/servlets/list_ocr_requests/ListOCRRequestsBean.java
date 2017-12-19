package ocrme_backend.servlets.list_ocr_requests;

import javax.annotation.Nullable;

/**
 * Created by iuliia on 12/18/17.
 */
public class ListOCRRequestsBean {
    private String userToken;

    private @Nullable String startCursor;

    public String getUserToken() {
        return userToken;
    }

    @Nullable
    public String getStartCursor() {
        return startCursor;
    }
}
