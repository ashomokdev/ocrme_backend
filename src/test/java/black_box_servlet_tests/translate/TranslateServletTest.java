package black_box_servlet_tests.translate;

import com.google.gson.Gson;
import ocrme_backend.datastore.gcloud_datastore.daos.TranslateRequestDaoImpl;
import ocrme_backend.servlets.ocr.OcrRequestBean;
import ocrme_backend.servlets.ocr.OcrResponse;
import ocrme_backend.servlets.translate.translate.TranslateRequestBean;
import ocrme_backend.servlets.translate.translate.TranslateResponse;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;

import static ocrme_backend.servlets.translate.translate.TranslateResponse.Status.OK;
import static ocrme_backend.utils.FileProvider.getImageUri;

public class TranslateServletTest {

    public static final String localHttpPost = "http://localhost:8080/translate";
    public static final String remoteHttpPost = "https://3-dot-ocrme-77a2b.appspot.com/translate";

    @Test
    public void testServletLocal() throws IOException {
        testServlet(localHttpPost);
    }

    @Test
    public void testServletProd() throws IOException {
        testServlet(remoteHttpPost);
    }

    private void testServlet(String url) throws IOException {
        HttpPost httppost = new HttpPost(url);
        Gson gson = new Gson();
        HttpClient httpClient = HttpClientBuilder.create().build();

        TranslateRequestBean translateRequestBean = new TranslateRequestBean();
        translateRequestBean.setIdTokenString(null);
        translateRequestBean.setSourceLang("en");
        translateRequestBean.setTargetLang("ru");
        translateRequestBean.setSourceText("Hello");

        StringEntity postingString = new StringEntity(gson.toJson(translateRequestBean));
        httppost.setEntity(postingString);
        httppost.setHeader("Content-type", "application/json");
        HttpResponse response = httpClient.execute(httppost);

        HttpEntity entity = response.getEntity();
        if (entity != null) {
            String retSrc = EntityUtils.toString(entity);

            TranslateResponse translateResponse = new Gson().fromJson(retSrc, TranslateResponse.class);
            Assert.assertEquals(OK, translateResponse.getStatus());
            Assert.assertNotNull(translateResponse.getTranslateResult());
            Assert.assertTrue(translateResponse.getTranslateResult().getTextResult().length() > 2);
        }
    }
}
