package black_box_servlet_tests.ocr;

import com.google.gson.Gson;
import ocrme_backend.datastore.gcloud_storage.utils.CloudStorageHelper;
import ocrme_backend.servlets.ocr.OcrRequestBean;
import ocrme_backend.servlets.ocr.OcrResponse;
import ocrme_backend.utils.FileProvider;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static ocrme_backend.servlets.ocr.OcrResponse.Status.OK;
import static ocrme_backend.utils.FileUtils.toInputStream;

/**
 * Created by iuliia on 5/22/17.
 * Run next to test in terminal
 * curl -H "Content-Type: application/json" -X POST -d '{"gcsImageUri":"gs://ocrme-77a2b.appspot.com/ocr_request_images/000c121b-357d-4ac0-a3f2-24e0f6d5cea185dffb40-e754-478f-b5b7-850fab211438.jpg", "languages":["ru"]}' https://imagetotext-149919.appspot.com/ocr_request
 * curl -H "Content-Type: application/json" -X POST -d '{"gcsImageUri":"gs://ocrme-77a2b.appspot.com/ocr_request_images/000c121b-357d-4ac0-a3f2-24e0f6d5cea185dffb40-e754-478f-b5b7-850fab211438.jpg", "languages":["ru"]}' http://localhost:8080/ocr_request
 * curl -H "Content-Type: application/json" -X POST -d '{"gcsImageUri":"gs://ocrme-77a2b.appspot.com/ocr_request_images/000c121b-357d-4ac0-a3f2-24e0f6d5cea185dffb40-e754-478f-b5b7-850fab211438.jpg"}' https://imagetotext-149919.appspot.com/ocr_request
 * curl -H "Content-Type: application/json" -X POST -d '{"gcsImageUri":"gs://ocrme-77a2b.appspot.com/ocr_request_images/000c121b-357d-4ac0-a3f2-24e0f6d5cea185dffb40-e754-478f-b5b7-850fab211438.jpg"}' http://localhost:8080/ocr_request
 */
public class OcrServletTest {

    public static final String localHttpPost = "http://localhost:8080/ocr_request";
    public static final String remoteHttpPost = "https://3-dot-ocrme-77a2b.appspot.com/ocr_request";
    private String imgUri;
    private String bucketName = System.getProperty("bucket-for-tests");

    @Test
    public void testServletLocal() throws IOException {
        testServlet(localHttpPost);
    }

    @Test
    public void testServletProd() throws IOException {
        testServlet(remoteHttpPost);
    }

    @Before
    public void uploadFilesToStorage() throws IOException {
        ByteArrayOutputStream imageAsStream = FileProvider.getSmallRuImageAsStream();
        CloudStorageHelper helper = new CloudStorageHelper();
        imgUri = helper.uploadFileForUri(toInputStream(imageAsStream), "filename.jpg", bucketName);
    }

    private void testServlet(String url) throws IOException {
        HttpPost httppost = new HttpPost(url);
        Gson gson = new Gson();
        HttpClient httpClient = HttpClientBuilder.create().build();

        OcrRequestBean ocrRequestBean = new OcrRequestBean();
        ocrRequestBean.setGcsImageUri(imgUri);

        StringEntity postingString = new StringEntity(gson.toJson(ocrRequestBean));
        httppost.setEntity(postingString);
        httppost.setHeader("Content-type", "application/json");
        HttpResponse response = httpClient.execute(httppost);

        HttpEntity entity = response.getEntity();
        if (entity != null) {
            String retSrc = EntityUtils.toString(entity);

            OcrResponse ocrResponse = new Gson().fromJson(retSrc, OcrResponse.class);
            Assert.assertEquals(OK, ocrResponse.getStatus());
            Assert.assertNotNull(ocrResponse.getOcrResult());
            Assert.assertTrue(ocrResponse.getOcrResult().getPdfResultGsUrl().length() > 2);
            Assert.assertTrue(ocrResponse.getOcrResult().getTextResult().length() > 2);
            Assert.assertTrue(ocrResponse.getOcrResult().getTextResult().contains("ы"));
            Assert.assertTrue(ocrResponse.getOcrResult().getPdfResultMediaUrl().length() > 2);

            Assert.assertTrue(ocrResponse.getOcrResult().getPdfImageResultGsUrl().length() >2);
            Assert.assertTrue(ocrResponse.getOcrResult().getPdfImageResultMediaUrl().length() >2);
        }
    }
}
