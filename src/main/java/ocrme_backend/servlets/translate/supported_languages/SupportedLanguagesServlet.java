package ocrme_backend.servlets.translate.supported_languages;

import com.google.gson.Gson;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Created by iuliia
 * Run next to test in terminal
 * curl http://localhost:8080/supported_languages?device_language_code=de
 * curl http://localhost:8080/supported_languages
 * curl https://imagetotext-149919.appspot.com/supported_languages?device_language_code=de
 */

public class SupportedLanguagesServlet extends HttpServlet {
    @Override
    public void doGet(HttpServletRequest req, HttpServletResponse response) {
        try {
            SupportedLanguagesRequestManager manager = new SupportedLanguagesRequestManager(req.getSession());
            SupportedLanguagesResponse supportedLanguages = manager.getSupportedLanguages();
            String json = new Gson().toJson(supportedLanguages);

            response.setContentType("text/html;charset=UTF-8");
            response.getWriter().write(json);
            response.setStatus(HttpServletResponse.SC_ACCEPTED);

        } catch (Exception e) {
            try {
                response.getWriter().write(e.getMessage());
            } catch (IOException e1) {
                e1.printStackTrace();
            }
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        }
    }
}
