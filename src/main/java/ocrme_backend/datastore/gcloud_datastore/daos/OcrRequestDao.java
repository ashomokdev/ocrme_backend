package ocrme_backend.datastore.gcloud_datastore.daos;

import ocrme_backend.datastore.gcloud_datastore.objects.OcrRequest;
import ocrme_backend.datastore.gcloud_datastore.objects.Result;

import java.sql.SQLException;

/**
 * Created by iuliia on 6/22/17.
 * CRUD operations
 */
public interface OcrRequestDao {
    Long create(OcrRequest ocrRequest) throws SQLException;

    OcrRequest read(Long ocrRequestId) throws SQLException;

    void update(OcrRequest ocrRequest) throws SQLException;

    void delete(Long ocrRequestId) throws SQLException;

    Result<OcrRequest> listOCRRequests(String startCursor) throws SQLException;

    Result<OcrRequest> listOCRRequestsByUser(String userId, String startCursor) throws SQLException;
}
