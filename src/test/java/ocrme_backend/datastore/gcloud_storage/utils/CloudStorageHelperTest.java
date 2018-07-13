package ocrme_backend.datastore.gcloud_storage.utils;

import com.google.cloud.storage.Blob;
import ocrme_backend.utils.FileProvider;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import javax.annotation.Nullable;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.UUID;
import java.util.logging.Handler;
import java.util.logging.Logger;
import java.util.logging.StreamHandler;

import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

/**
 * Created by iuliia on 6/20/17.
 */
public class CloudStorageHelperTest {
    private static OutputStream logCapturingStream;
    private static StreamHandler customLogHandler;
    private String bucketName;
    private CloudStorageHelper helper = spy(CloudStorageHelper.class);

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
    public void createBucket() {

        helper.createBucket(bucketName);
        final String expectedLogPart = "created";

        String capturedLog = getTestCapturedLog();
        Assert.assertTrue(capturedLog.contains(expectedLogPart));

    }

    @Test
    public void deleteBucket() throws Exception {
        helper.createBucket(bucketName);
        helper.deleteBucket(bucketName);

        String capturedLog = getTestCapturedLog();
        Assert.assertTrue(capturedLog.contains("created"));
        Assert.assertTrue(capturedLog.contains("deleted"));
    }

    /**
     * upload image file, represented as InputStream
     * @throws Exception
     */
    @Test
    public void uploadImageAsInputStream() throws Exception {
        helper.createBucket(bucketName);
        ByteArrayOutputStream stream = FileProvider.getImageAsStream();
        String url = helper.uploadFile(FileUtils.toInputStream(stream), "filename.jpg", bucketName);
        Assert.assertNotNull(url);
        Assert.assertNotEquals("", url);

        String capturedLog = getTestCapturedLog();
        Assert.assertTrue(capturedLog.contains("created"));
        Assert.assertTrue(capturedLog.contains("uploaded"));
    }


    /**
     * upload russian pdf, represented as byte[]
     * @throws Exception
     */
    @Test
    public void uploadRusPdfAsByteArray() throws Exception {
        helper.createBucket(bucketName);
        String url =  helper.uploadFile(FileProvider.getPdfAsStream().toByteArray(), "filename.pdf",bucketName);
        System.out.println("url: "+url);
        Assert.assertNotNull(url);
        Assert.assertNotEquals("", url);

        String capturedLog = getTestCapturedLog();
        Assert.assertTrue(capturedLog.contains("created"));
        Assert.assertTrue(capturedLog.contains("uploaded"));
    }

    /**
     * upload russian pdf, represented as InputStream
     * @throws Exception
     */
    @Test
    public void uploadRusPdfAsStream() throws Exception {
        helper.createBucket(bucketName);
        String url =  helper.uploadFile(FileUtils.toInputStream(FileProvider.getPdfAsStream()), "filename.pdf",bucketName);
        System.out.println("url: "+url);
        Assert.assertNotNull(url);
        Assert.assertNotEquals("", url);

        String capturedLog = getTestCapturedLog();
        Assert.assertTrue(capturedLog.contains("created"));
        Assert.assertTrue(capturedLog.contains("uploaded"));
    }

   private String getTestCapturedLog() {
        customLogHandler.flush();
        return logCapturingStream.toString();
    }

    @Test
    public void downloadFile() throws IOException {
        //parse real url and check
        String gcsImageUri = "gs://ocrme-77a2b.appspot.com/ocr_request_images/000c121b-357d-4ac0-a3f2-24e0f6d5cea185dffb40-e754-478f-b5b7-850fab211438.jpg";

        String filename = helper.parseGcsUriForFilename(gcsImageUri);
        String bucketName = helper.parseGcsUriForBucketName(gcsImageUri);

        Assert.assertEquals("ocr_request_images/000c121b-357d-4ac0-a3f2-24e0f6d5cea185dffb40-e754-478f-b5b7-850fab211438.jpg", filename);
        Assert.assertEquals("ocrme-77a2b.appspot.com", bucketName);

        //crete mock blob
        helper.createBucket(bucketName);
        ByteArrayOutputStream stream = FileProvider.getImageAsStream();
        String url = helper.uploadFile(FileUtils.toInputStream(stream), "filename.jpg", bucketName);
        Blob mockBlob = helper.uploadFileForBlob(
                stream.toByteArray(), "filename.jpg", "directoryName", bucketName);

        when(helper.getBlob(url)).thenReturn(mockBlob);

        byte[] bytes = helper.downloadFile(url);
        Assert.assertTrue(bytes.length>100);
    }
}