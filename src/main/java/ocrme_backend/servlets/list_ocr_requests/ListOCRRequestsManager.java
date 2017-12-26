package ocrme_backend.servlets.list_ocr_requests;

import ocrme_backend.datastore.gcloud_datastore.objects.OcrRequest;
import ocrme_backend.datastore.gcloud_datastore.objects.Result;
import ocrme_backend.utils.FirebaseAuthUtil;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import static java.util.logging.Level.INFO;
import static java.util.logging.Level.WARNING;

/**
 * Created by iuliia on 12/18/17.
 */
public class ListOCRRequestsManager {
    private final String idTokenString;
    private final @Nullable
    String startCursor;
    private final Logger logger = Logger.getLogger(ListOCRRequestsManager.class.getName());

    public ListOCRRequestsManager(String idTokenString, @Nullable String startCursor) {
        this.idTokenString = idTokenString;
        this.startCursor = startCursor;
    }

    public ListOCRResponse process() {
        ListOCRResponse response = new ListOCRResponse();
        try {
            String userId = FirebaseAuthUtil.getUserId(idTokenString);
            if (userId == null) {
                response.setStatus(ListOCRResponse.Status.USER_NOT_FOUND);
            } else {
                DbDataReceiver dbDataReceiver = new DbDataReceiver();
                Result<OcrRequest> result = dbDataReceiver.listOCRRequestsByUser(userId, startCursor);
                response.setStatus(ListOCRResponse.Status.OK);

                List<ListOCRResponse.MyDoc> myDocs = new ArrayList<>();
                for (OcrRequest item : result.result) {
                    myDocs.add(new ListOCRResponse.MyDoc(item));
                }
                response.setRequestList(myDocs);

                response.setEndCursor(result.cursor);
            }
        } catch (Exception e) {
            e.printStackTrace();
            response.setStatus(ListOCRResponse.Status.UNKNOWN_ERROR);
            logger.log(WARNING, e.getMessage());
        }
        logger.log(INFO, "Response: " + response.toString());
        return response;
    }
}
