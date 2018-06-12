package ocrme_backend.servlets.translate_deprecated;

import com.google.gson.Gson;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Created by iuliia
 * Run next to test in terminal
 * curl http://localhost:8080/supported_languages?device_language_code=de
 * curl https://imagetotext-149919.appspot.com/supported_languages?device_language_code=de
 */

@Deprecated
public class SupportedLanguagesServletDeprecated extends HttpServlet {
    @Override
    public void doGet(HttpServletRequest req, HttpServletResponse response) {
        try {
            String deviceLanguageCode = req.getParameter("device_language_code");

            SupportedLanguagesResponseDeprecated supportedLanguages =
                    SupportedLanguagesRequestManagerDeprecated.getSupportedLanguages(deviceLanguageCode);
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
