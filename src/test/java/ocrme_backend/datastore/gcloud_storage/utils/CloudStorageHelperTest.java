package ocrme_backend.datastore.gcloud_storage.utils;

import ocrme_backend.datastore.utils.FileProvider;
import org.apache.commons.fileupload.FileItemStream;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.UUID;
import java.util.logging.Handler;
import java.util.logging.Logger;
import java.util.logging.StreamHandler;

/**
 * Created by iuliia on 6/20/17.
 */
public class CloudStorageHelperTest {
    private static OutputStream logCapturingStream;
    private static StreamHandler customLogHandler;
    private String bucketName;
    private CloudStorageHelper helper = new CloudStorageHelper();


    @Before
    public void setUp() {

        bucketName = "bucket-fromtest" + UUID.randomUUID().toString();
        Logger logger = Logger.getLogger(CloudStorageHelper.class.getName());

        logCapturingStream = new ByteArrayOutputStream();
        Handler[] handlers = logger.getParent().getHandlers();
        customLogHandler = new StreamHandler(logCapturingStream, handlers[0].getFormatter());
        logger.addHandler(customLogHandler);
    }

    @After
    public void tearDown() {
        helper.deleteBucket(bucketName);
    }

    @Test
    public void createBucket() throws Exception {

        helper.createBucket(bucketName);
        final String expectedLogPart = "created";

        String capturedLog = getTestCapturedLog();
        Assert.assertTrue(capturedLog.contains(expectedLogPart));

    }

    @Test
    public void deleteBucket() throws Exception {
        helper.createBucket(bucketName);
        Thread.sleep(2000);
        helper.deleteBucket(bucketName);

        String capturedLog = getTestCapturedLog();
        Assert.assertTrue(capturedLog.contains("created"));
        Assert.assertTrue(capturedLog.contains("deleted"));
    }

    @Test
    public void uploadFile() throws Exception {
        helper.createBucket(bucketName);
        Thread.sleep(2000);
        FileItemStream file = FileProvider.getFile();
        String url = helper.uploadFile(file, bucketName);

        String capturedLog = getTestCapturedLog();
        Assert.assertTrue(capturedLog.contains("created"));
        Assert.assertTrue(capturedLog.contains("uploaded"));
    }

    public String getTestCapturedLog() throws IOException {
        customLogHandler.flush();
        return logCapturingStream.toString();
    }

}