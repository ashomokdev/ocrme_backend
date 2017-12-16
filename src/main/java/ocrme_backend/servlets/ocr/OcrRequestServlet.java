package ocrme_backend.servlets.ocr;

import com.google.gson.Gson;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;

/**
 * Created by iuliia on 5/22/17.
 * Run next to test in terminal
 * curl -H "Content-Type: application/json" -X POST -d '{"gcsImageUri":"gs://bucket-for-requests-test/2017-07-26-12-37-36-806-2017-07-26-12-37-36-806-ru.jpg", "languages":["ru"]}' https://imagetotext-149919.appspot.com/ocr_request
 * curl -H "Content-Type: application/json" -X POST -d '{"gcsImageUri":"gs://bucket-for-requests-test/2017-07-26-12-37-36-806-2017-07-26-12-37-36-806-ru.jpg", "languages":["ru"]}' http://localhost:8080/ocr_request
 * curl -H "Content-Type: application/json" -X POST -d '{"gcsImageUri":"gs://bucket-for-requests-test/2017-07-26-12-37-36-806-2017-07-26-12-37-36-806-ru.jpg"}' https://imagetotext-149919.appspot.com/ocr_request
 * curl -H "Content-Type: application/json" -X POST -d '{"gcsImageUri":"gs://bucket-for-requests-test/2017-07-26-12-37-36-806-2017-07-26-12-37-36-806-ru.jpg"}' http://localhost:8080/ocr_request
 */

//example "gs://bucket-for-requests-test/2017-07-26-12-37-36-806-2017-07-26-12-37-36-806-ru.jpg";

public class OcrRequestServlet extends HttpServlet {
    @Override
    public void doPost(HttpServletRequest req, HttpServletResponse response) {

        try {
            BufferedReader reader = req.getReader();
            OcrRequestBean requestBean = new Gson().fromJson(reader, OcrRequestBean.class);

            OcrRequestManager manager = new OcrRequestManager(
                    requestBean.getIdTokenString(),
                    requestBean.getGcsImageUri(),
                    requestBean.getLanguages(),
                    req.getSession());

            OcrResponse ocrResponse = manager.process();
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");

            String json = new Gson().toJson(ocrResponse);
            response.getWriter().print(json);
            response.getWriter().flush();
            response.setStatus(HttpServletResponse.SC_ACCEPTED);


        } catch (Exception e) {
            try {
                e.printStackTrace();
                response.getWriter().write(e.getMessage());
            } catch (IOException e1) {
                e1.printStackTrace();
            }
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        }
    }
}
