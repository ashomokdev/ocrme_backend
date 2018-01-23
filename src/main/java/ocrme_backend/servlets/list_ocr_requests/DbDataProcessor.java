package ocrme_backend.servlets.list_ocr_requests;

import ocrme_backend.datastore.gcloud_datastore.daos.OcrRequestDao;
import ocrme_backend.datastore.gcloud_datastore.daos.OcrRequestDaoImpl;
import ocrme_backend.datastore.gcloud_datastore.objects.OcrRequest;
import ocrme_backend.datastore.gcloud_datastore.objects.Result;

import java.sql.SQLException;
import java.util.List;

/**
 * Created by iuliia on 12/19/17.
 */
public class DbDataProcessor {

    public  static Result<OcrRequest> listOCRRequestsByUser(String userId, String startCursor) throws SQLException {
        OcrRequestDao dao = new OcrRequestDaoImpl();
        return dao.listOCRRequestsByUser(userId, startCursor);
    }

    public static void delete(List<Long> ocrRequestIds) {
        OcrRequestDao dao = new OcrRequestDaoImpl();
        dao.delete(ocrRequestIds);
    }
}
