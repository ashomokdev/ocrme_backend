package ocrme_backend.servlets.list_ocr_requests;

import com.google.gson.Gson;

import javax.servlet.ServletException;
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
    protected void doPost(HttpServletRequest req, HttpServletResponse response) throws ServletException, IOException {
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
