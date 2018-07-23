package ocrme_backend.servlets.list_ocr_requests;

import ocrme_backend.datastore.gcloud_datastore.objects.OcrRequest;
import ocrme_backend.datastore.gcloud_datastore.objects.Result;
import ocrme_backend.servlets.ocr.OcrResult;
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
class ListOCRRequestsManager {

    private final Logger logger = Logger.getLogger(ListOCRRequestsManager.class.getName());

    ListOCRRequestsManager() { }

    ListOCRResponse processForGet(String idTokenString, @Nullable String startCursor) {
        ListOCRResponse response = new ListOCRResponse();
        try {
            String userId = getUserId(idTokenString);
            if (userId == null) {
                response.setStatus(ListOCRResponse.Status.USER_NOT_FOUND);
            } else {
                Result<OcrRequest> result = DbDataProcessor.listOCRRequestsByUser(userId, startCursor);
                response.setStatus(ListOCRResponse.Status.OK);

                List<OcrResult> ocrResults = new ArrayList<>();
                for (OcrRequest item : result.result) {
                    ocrResults.add(OcrResult.Converter.convert(item));
                }
                response.setRequestList(ocrResults);

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

    private String getUserId(String idTokenString) {
        return FirebaseAuthUtil.getUserId(idTokenString);
    }

    void processForDelete(String[] ocrRequestIds) {
        try {
            List<Long> ids = new ArrayList<>();
            for (String id : ocrRequestIds) {
                Long longId = Long.parseLong(id);
                ids.add(longId);
            }
            DbDataProcessor.delete(ids);

        } catch (Exception e) {
            e.printStackTrace();
            logger.log(WARNING, e.getMessage());
            throw e;
        }
    }
}
