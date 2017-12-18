package ocrme_backend.datastore.gcloud_datastore.daos;

import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import ocrme_backend.datastore.gcloud_datastore.objects.OcrRequest;
import ocrme_backend.datastore.gcloud_datastore.objects.Result;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.sql.SQLException;
import java.util.Optional;
import java.util.logging.Handler;
import java.util.logging.Logger;
import java.util.logging.StreamHandler;

/**
 * Created by iuliia on 6/22/17.
 */
public class OcrRequestDaoImplTest {

    private static OutputStream logCapturingStream;
    private static StreamHandler customLogHandler;
    private final LocalServiceTestHelper helper =
            new LocalServiceTestHelper(new LocalDatastoreServiceTestConfig());
    OcrRequestDao dao = null;

    @Before
    public void setUp() {
        helper.setUp();
        dao = new OcrRequestDaoImpl();

        Logger logger = Logger.getLogger(OcrRequestDaoImpl.class.getName());

        logCapturingStream = new ByteArrayOutputStream();
        Handler[] handlers = logger.getParent().getHandlers();
        customLogHandler = new StreamHandler(logCapturingStream, handlers[0].getFormatter());
        logger.addHandler(customLogHandler);

    }

    @After
    public void tearDown() {
        helper.tearDown();
    }

    @Test
    public void create() throws Exception {

        addRequestToDb();
        Thread.sleep(2000);

        String capturedLog = getTestCapturedLog();
        Assert.assertTrue(capturedLog.contains("created"));
    }

    @Test
    public void readOCRRequest() throws Exception {
        String inputImageUrlExpected = "https://www.googleapis.com/download/storage/v1/b/bucket-fromtesta8d4835c-7b99-4a22-8458-f915b63bb4ac/o/2017-06-27-071400998-img.jpg?generation=1498547642256093&alt=media";
        String textResultExpected = "dummy text result";

        OcrRequestDao dao = new OcrRequestDaoImpl();
        Long id = addRequestToDb();

        OcrRequest obtained = dao.read(id);
        Assert.assertTrue(obtained.getId() == 1);
        Assert.assertTrue(obtained.getTextResult().getValue().equals(textResultExpected));
        Assert.assertTrue(obtained.getSourceImageUrl().equals(inputImageUrlExpected));
    }

    @Test
    public void updateOCRRequest() throws Exception {

        Long id = addRequestToDb();

        String textResultUpdated = "dummy text result updated";
        OcrRequest updated = new OcrRequest.Builder()
                .id(id)
                .textResult(Optional.ofNullable(textResultUpdated))
                .build();

        dao.update(updated);
        String capturedLog = getTestCapturedLog();
        Assert.assertTrue(capturedLog.contains("updated"));
    }

    @Test
    public void deleteOCRRequest() throws Exception {
        Long id = addRequestToDb();
        Result<OcrRequest> resultAdded = dao.listOCRRequests("");
        Assert.assertTrue(resultAdded.result.size() == 1);

        dao.delete(id);
        Result<OcrRequest> resultDeleted = dao.listOCRRequests("");
        Assert.assertTrue(resultDeleted.result.size() == 0);

        String capturedLog = getTestCapturedLog();
        Assert.assertTrue(capturedLog.contains("deleted"));
    }

    @Test
    public void listOCRRequests() throws Exception {

        addRequestToDb();
        addRequestToDb();
        addRequestToDb();

        Result<OcrRequest> result = dao.listOCRRequests("");
        Assert.assertTrue(result.result.size() == 3);
    }

    /**
     * test without cursor usage
     *
     * @throws Exception
     */
    @Test
    public void listOCRRequestsByUser() throws Exception {
        for (int i = 0; i < 20; i++) {
            addRequestToDb();
        }

        addRequestToDb("123");

        String startCursor = null;

        Result<OcrRequest> result =
                dao.listOCRRequestsByUser("1234dfgdgdg567890", startCursor);
        String cursor = result.cursor;
        Result<OcrRequest> result2 =
                dao.listOCRRequestsByUser("1234dfgdgdg567890", cursor);
        String cursor2 = result2.cursor;
        Result<OcrRequest> result3 =
                dao.listOCRRequestsByUser("1234dfgdgdg567890", cursor2);

        Result<OcrRequest> resultEmpty =
                dao.listOCRRequestsByUser("no user", startCursor);

        Result<OcrRequest> result4 =
                dao.listOCRRequestsByUser("123", startCursor);


        Assert.assertTrue(result.result.size() > 3);
        Assert.assertTrue(result2.result.size() > 3);
        Assert.assertTrue(result3.result.size() == 0);
        Assert.assertTrue(resultEmpty.result.size() == 0);
        Assert.assertTrue(result4.result.size() == 1);
    }


    private Long addRequestToDb() throws SQLException {
        return addRequestToDb("1234dfgdgdg567890");
    }

    private Long addRequestToDb(String createdById) throws SQLException {
        String inputImageUrl = "https://www.googleapis.com/download/storage/v1/b/bucket-fromtesta8d4835c-7b99-4a22-8458-f915b63bb4ac/o/2017-06-27-071400998-img.jpg?generation=1498547642256093&alt=media";
        String textResult = "dummy text result";

        OcrRequest request = new OcrRequest.Builder()
                .sourceImageUrl(inputImageUrl)
                .textResult(Optional.of(textResult))
                .createdBy("bieliaievays@gmail.com")
                .createdById(createdById)
                .build();

        OcrRequestDao dao = new OcrRequestDaoImpl();
        return dao.create(request);
    }

    private String getTestCapturedLog() throws IOException {
        customLogHandler.flush();
        return logCapturingStream.toString();
    }
}