package ocrme_backend.servlets.smart_crop;

import com.google.gson.Gson;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;

/**
 * Created by iuliia on 12/18/17.
 */

public class ContourEdgePointsServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse response) {
        try {
            BufferedReader reader = req.getReader();
            ContourEdgePointsBean requestBean = new Gson().fromJson(reader, ContourEdgePointsBean.class);

            ContourEdgePointsRequestsManager manager = new ContourEdgePointsRequestsManager();

            ContourEdgePointsResponse contourEdgePointsResponse = manager.processForPost(
                    requestBean.getGcsImageUri());
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");

            String json = new Gson().toJson(contourEdgePointsResponse);
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
