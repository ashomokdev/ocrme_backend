package ocrme_backend.servlets.list_ocr_requests;

import ocrme_backend.datastore.gcloud_datastore.daos.OcrRequestDao;
import ocrme_backend.datastore.gcloud_datastore.daos.OcrRequestDaoImpl;
import ocrme_backend.datastore.gcloud_datastore.objects.OcrRequest;
import ocrme_backend.datastore.gcloud_datastore.objects.Result;

import java.sql.SQLException;

/**
 * Created by iuliia on 12/19/17.
 */
public class DbDataReceiver {

    public Result<OcrRequest> listOCRRequestsByUser(String userId, String startCursor) throws SQLException {
        OcrRequestDao dao = new OcrRequestDaoImpl();
        return dao.listOCRRequestsByUser(userId, startCursor);
    }
}
