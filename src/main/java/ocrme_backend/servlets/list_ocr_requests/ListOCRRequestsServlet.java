package ocrme_backend.servlets.list_ocr_requests;

import com.google.gson.Gson;
import ocrme_backend.servlets.ocr.OcrRequestBean;
import ocrme_backend.servlets.ocr.OcrRequestManager;
import ocrme_backend.servlets.ocr.OcrResponse;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;

/**
 * Created by iuliia on 12/18/17.
 */
public class ListOCRRequestsServlet extends HttpServlet {
    @Override
    public void doPost(HttpServletRequest req, HttpServletResponse response) {

        try {
            BufferedReader reader = req.getReader();
            ListOCRRequestsBean requestBean = new Gson().fromJson(reader, ListOCRRequestsBean.class);

            ListOCRRequestsManager manager = new ListOCRRequestsManager(
                    requestBean.getUserToken(),
                    requestBean.getStartCursor());

            ListOCRResponse listOCRResponse = manager.process();
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");

            String json = new Gson().toJson(listOCRResponse);
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
