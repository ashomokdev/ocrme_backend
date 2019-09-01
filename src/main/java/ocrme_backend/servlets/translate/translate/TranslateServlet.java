package ocrme_backend.servlets.translate.translate;

import com.google.gson.Gson;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;

/**
 * Created by iuliia on 5/22/17.
 * Run next to test in terminal
 * curl -H "Content-Type: application/json" -X POST -d '{"targetLang":"de", sourceLang":"de", "sourceText":"Mit Macht kommt große Verantwortung."}' https://imagetotext-149919.appspot.com/translate
 * curl -H "Content-Type: application/json" -X POST -d '{"targetLang":"ru", "sourceLang":"de", "sourceText":"Mit Macht kommt große Verantwortung."}' http://localhost:8080/translate
 * curl -H "Content-Type: application/json" -X POST -d '{"targetLang":"de", "sourceText":"Mit Macht kommt große Verantwortung."}' http://localhost:8080/translate
 */

public class TranslateServlet extends HttpServlet {

    @Override
    public void doPost(HttpServletRequest req, HttpServletResponse response) {

        try {
            BufferedReader reader = req.getReader();

            TranslateRequestBean requestBean =
                    new Gson().fromJson(reader, TranslateRequestBean.class);

            TranslateRequestManager manager = new TranslateRequestManager(req.getSession(), requestBean.getIdTokenString());
            TranslateResponse translateResponse =
                    manager.translate(
                            requestBean.getSourceLang(),
                            requestBean.getTargetLang(),
                            requestBean.getSourceText());

            String json = new Gson().toJson(translateResponse);

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
