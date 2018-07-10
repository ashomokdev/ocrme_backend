package ocrme_backend.servlets.translate.translate;

import ocrme_backend.datastore.gcloud_datastore.daos.OcrRequestDao;
import ocrme_backend.datastore.gcloud_datastore.daos.OcrRequestDaoImpl;
import ocrme_backend.datastore.gcloud_datastore.daos.TranslateRequestDao;
import ocrme_backend.datastore.gcloud_datastore.daos.TranslateRequestDaoImpl;
import ocrme_backend.datastore.gcloud_datastore.objects.OcrRequest;
import ocrme_backend.datastore.gcloud_datastore.objects.TranslateRequest;

import java.sql.SQLException;

/**
 * Created by iuliia on 7/13/17.
 * create or update OcrRequest in Db
 */
public class DbPusher {

    public long add(TranslateRequest request) {

        TranslateRequestDao dao = new TranslateRequestDaoImpl();
        try {
            return dao.create(request);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

}
