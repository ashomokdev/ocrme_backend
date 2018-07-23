package ocrme_backend.datastore.gcloud_datastore.daos;

import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import ocrme_backend.datastore.gcloud_datastore.objects.OcrRequest;
import ocrme_backend.datastore.gcloud_datastore.objects.Result;
import ocrme_backend.servlets.ocr.OcrResponse;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.sql.SQLException;
import java.util.ArrayList;
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
    private OcrRequestDao dao = null;

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
        Assert.assertEquals(1, (long) obtained.getId());
        Assert.assertEquals(obtained.getTextResult().getValue(), textResultExpected);
        Assert.assertEquals(obtained.getSourceImageUrl(), inputImageUrlExpected);
        Assert.assertEquals(obtained.getPdfImageResultGsUrl(), "mock pdfImageResultGsUrl");
        Assert.assertEquals(obtained.getPdfImageResultMediaUrl(), "mock pdfImageResultMediaUrl");
    }

    @Test
    public void updateOCRRequest() throws Exception {

        Long id = addRequestToDb();

        String textResultUpdated = "dummy text result updated";
        OcrRequest updated = new OcrRequest.Builder()
                .id(id)
                .textResult(Optional.of(textResultUpdated))
                .build();

        dao.update(updated);
        String capturedLog = getTestCapturedLog();
        Assert.assertTrue(capturedLog.contains("updated"));
    }

    @Test
    public void deleteOCRRequest() throws Exception {
        Long id = addRequestToDb();
        Result<OcrRequest> resultAdded = dao.listOCRRequests("");
        Assert.assertEquals(1, resultAdded.result.size());

        dao.delete(id);
        Result<OcrRequest> resultDeleted = dao.listOCRRequests("");
        Assert.assertEquals(0, resultDeleted.result.size());

        String capturedLog = getTestCapturedLog();
        Assert.assertTrue(capturedLog.contains("deleted"));
    }

    @Test
    public void listOCRRequests() throws Exception {

        addRequestToDb();
        addRequestToDb();
        addRequestToDb();

        Result<OcrRequest> result = dao.listOCRRequests("");
        Assert.assertEquals(3, result.result.size());
    }

    @Test
    public void listOCRRequestsByUser() throws Exception {

        for (int i = 0; i < 40; i++) {
            addRequestToDb(String.valueOf(i), "OK");
        }
        addRequestToDb("0", "UNKNOWN_ERROR");
        addRequestToDb("1", "OK");

        String startCursor = null;

        //START test status and result size
        Result<OcrRequest> resultFor0 =
                dao.listOCRRequestsByUser("0", startCursor);
        Result<OcrRequest> resultFor1 =
                dao.listOCRRequestsByUser("1", startCursor);
        Assert.assertEquals(1, resultFor0.result.size());
        Assert.assertEquals(2, resultFor1.result.size());
        //END test status and result size
    }

    @Test
    public void listOCRRequestsByUserTestCursor() throws Exception {
        for (int i = 0; i < 40; i++) {
            addRequestToDb("0", "OK");
            addRequestToDb("0", "UNKNOWN_ERROR");
        }

        Result<OcrRequest> resultFirstPart =
                dao.listOCRRequestsByUser("0", null);

        Assert.assertEquals(12, resultFirstPart.result.size());
        String endCursorFirst = resultFirstPart.cursor;
        Assert.assertNotNull(endCursorFirst);

        Result<OcrRequest> resultSecondPart =
                dao.listOCRRequestsByUser("0", endCursorFirst);
        Assert.assertEquals(12, resultSecondPart.result.size());
        String endCursorSecond = resultSecondPart.cursor;
        Assert.assertNotNull(endCursorSecond);
        Assert.assertNotEquals(endCursorSecond, endCursorFirst);
    }

    @Test
    public void listOCRRequestsByUserTestStatus() throws Exception {

        addRequestToDb("0", "OK");
        addRequestToDb("0", "OK");
        addRequestToDb("0", "UNKNOWN_ERROR");

        Result<OcrRequest> result =
                dao.listOCRRequestsByUser("0", null);

        for (int i = 0; i < result.result.size(); i++) {
            Assert.assertEquals(result.result.get(i).getStatus(), "OK");
        }

        Assert.assertEquals(2, result.result.size());
    }

    @Test
    public void listOCRRequestsByUserTestSequence() throws Exception {
        String userId = "123";
        ArrayList<Long> ids = new ArrayList<>();
        int count = 50;
        for (int i = 0; i < count; i++) {
            long id = addRequestToDb(userId);
            ids.add(id);
        }

        Result<OcrRequest> result =
                dao.listOCRRequestsByUser(userId, null);

        for (int i = 0; i < result.result.size(); i++) {
            Assert.assertEquals(ids.get(count-1-i), result.result.get(i).getId());
        }
    }

    private Long addRequestToDb() throws SQLException {
        return addRequestToDb("1234dfgdgdg567890");
    }


    private Long addRequestToDb(String createdById, String status) throws SQLException {
        String inputImageUrl = "https://www.googleapis.com/download/storage/v1/b/bucket-fromtesta8d4835c-7b99-4a22-8458-f915b63bb4ac/o/2017-06-27-071400998-img.jpg?generation=1498547642256093&alt=media";
        String textResult = "dummy text result";

        OcrRequest request = new OcrRequest.Builder()
                .sourceImageUrl(inputImageUrl)
                .textResult(Optional.of(textResult))
                .createdBy("bieliaievays@gmail.com")
                .createdById(createdById)
                .pdfImageResultGsUrl("mock pdfImageResultGsUrl")
                .pdfImageResultMediaUrl("mock pdfImageResultMediaUrl")
                .status(status)
                .pdfResultGsUrl("new value")
                .pdfResultMediaUrl("new value")
                .build();

        OcrRequestDao dao = new OcrRequestDaoImpl();
        return dao.create(request);
    }

    private Long addRequestToDb(String createdById) throws SQLException {
        return addRequestToDb(createdById, OcrResponse.Status.OK.name());
    }

    private String getTestCapturedLog() {
        customLogHandler.flush();
        return logCapturingStream.toString();
    }
}