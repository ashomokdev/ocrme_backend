package ocrme_backend.datastore.gcloud_datastore.daos;
import ocrme_backend.datastore.gcloud_datastore.objects.Result;
import ocrme_backend.datastore.gcloud_datastore.objects.TranslateRequest;

import java.sql.SQLException;
import java.util.List;

/**
 * Created by iuliia on 6/22/17.
 * CRUD operations
 */
public interface TranslateRequestDao {
    Long create(TranslateRequest TranslateRequest) throws SQLException;

    TranslateRequest read(Long TranslateRequestId) throws SQLException;

    void update(TranslateRequest TranslateRequest) throws SQLException;

    void delete(Long TranslateRequestId) throws SQLException;

    void delete(List<Long> TranslateRequestIds);

    Result<TranslateRequest> listTranslateRequests(String startCursor) throws SQLException;

    /**
     * most recent get first
     *
     * @param userId
     * @param startCursor
     * @return
     * @throws SQLException
     */
    Result<TranslateRequest> listTranslateRequestsByUser(String userId, String startCursor) throws SQLException;
}
