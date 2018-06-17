package ocrme_backend.datastore.gcloud_datastore.daos;

import ocrme_backend.datastore.gcloud_datastore.objects.OcrRequest;
import ocrme_backend.datastore.gcloud_datastore.objects.Result;

import java.sql.SQLException;
import java.util.List;

/**
 * Created by iuliia on 6/22/17.
 * CRUD operations
 */
public interface OcrRequestDao {
    Long create(OcrRequest ocrRequest) throws SQLException;

    OcrRequest read(Long ocrRequestId) throws SQLException;

    void update(OcrRequest ocrRequest) throws SQLException;

    void delete(Long ocrRequestId) throws SQLException;

    void delete(List<Long> ocrRequestIds);

    Result<OcrRequest> listOCRRequests(String startCursor) throws SQLException;

    /**
     * most recent get first
     *
     * @param userId
     * @param startCursor
     * @return
     * @throws SQLException
     */
    Result<OcrRequest> listOCRRequestsByUser(String userId, String startCursor) throws SQLException;
}
