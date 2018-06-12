package ocrme_backend.servlets.ocr;

import ocrme_backend.datastore.gcloud_datastore.daos.OcrRequestDao;
import ocrme_backend.datastore.gcloud_datastore.daos.OcrRequestDaoImpl;
import ocrme_backend.datastore.gcloud_datastore.objects.OcrRequest;

import java.sql.SQLException;
import java.util.concurrent.ExecutorService;

/**
 * Created by iuliia on 7/13/17.
 * create or update OcrRequest in Db
 */
public class DbPusher {

    public long add(OcrRequest request){

        OcrRequestDao dao = new OcrRequestDaoImpl();
        try {
            return dao.create(request);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

}
