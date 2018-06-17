package ocrme_backend.servlets.translate.translate;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.servlet.http.HttpSession;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.logging.Logger;

import static java.util.logging.Level.WARNING;

/**
 * DOCS https://docs.microsoft.com/en-us/azure/cognitive-services/translator/reference/v3-0-translate?tabs=curl
 * Created by iuliia on 8/31/17.
 */
public class TranslateRequestManager {

    public static final String SECRET_KEY_PARAMETER = "bing.key.parameter";
    public static final String SECRET_KEYS_FILE_PATH = "secret.key.filepath";
    private static String host = "https://api.cognitive.microsofttranslator.com";
    private static String path = "/translate?api-version=3.0";
    private final Logger logger =
            Logger.getLogger(TranslateRequestManager.class.getName());
    private String subscriptionKey;


    //todo use DI framework (Dagger) and inject session
    public TranslateRequestManager(HttpSession session) throws IOException {
        String secretKeyFilepath = session.getServletContext().getInitParameter(SECRET_KEYS_FILE_PATH);
        Properties props = new Properties();
        props.load(session.getServletContext().getResourceAsStream(secretKeyFilepath));

        String bingKeyParameter = session.getServletContext().getInitParameter(SECRET_KEY_PARAMETER);
        subscriptionKey = props.getProperty(bingKeyParameter);

        if (subscriptionKey == null || subscriptionKey.isEmpty()) {
            logger.log(WARNING, "subscriptionKey was not obtained");
        }
    }

    public TranslateResponse translate(
            @Nullable String sourceLanguageCode,
            @Nonnull String targetLanguageCode,
            String sourceText) throws MalformedURLException {

        String params = generateParams(sourceLanguageCode, targetLanguageCode);
        URL url = new URL(host + path + params);

        List<RequestBody> objList = new ArrayList<>();
        objList.add(new RequestBody(sourceText));
        String content = new Gson().toJson(objList);

        String jsonResult = postForTranslate(url, content);

        return generateTranslateResponse(jsonResult, sourceLanguageCode, targetLanguageCode);
    }

    private TranslateResponse generateTranslateResponse(
            @Nullable String jsonText,
            @Nullable String sourceLanguageCode,
            @Nonnull String targetLanguageCode) {
        TranslateResponse response = new TranslateResponse();
        if (jsonText != null) {
            try {
                JsonParser parser = new JsonParser();
                JsonArray jsonArray = parser.parse(jsonText).getAsJsonArray();
                JsonObject json = jsonArray.get(0).getAsJsonObject();

                if (sourceLanguageCode == null || sourceLanguageCode.isEmpty()) {
                    sourceLanguageCode = json
                            .getAsJsonObject("detectedLanguage")
                            .get("language")
                            .getAsString();
                }

                String text = json
                        .getAsJsonArray("translations")
                        .get(0).getAsJsonObject()
                        .get("text")
                        .getAsString();


                response.setSourceLanguageCode(sourceLanguageCode);
                response.setTargetLanguageCode(targetLanguageCode);
                response.setTextResult(text);
                response.setStatus(TranslateResponse.Status.OK);

            } catch (Exception e) {
                e.printStackTrace();
                response.setStatus(TranslateResponse.Status.UNKNOWN_ERROR);
            }
        } else {
            response.setStatus(TranslateResponse.Status.UNKNOWN_ERROR);
        }
        return response;
    }


    private String generateParams(@Nullable String sourceLanguageCode,
                                  @Nonnull String targetLanguageCode) {
        String params = "";

        if (sourceLanguageCode != null && !sourceLanguageCode.isEmpty()) {
            params = "&from=" + sourceLanguageCode;
        }
        params += "&to=" + targetLanguageCode;

        return params;
    }


    private @Nullable
    String postForTranslate(URL url, String content) {
        try {
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("Content-Length", content.length() + "");
            connection.setRequestProperty("Ocp-Apim-Subscription-Key", subscriptionKey);
            connection.setRequestProperty("X-ClientTraceId", java.util.UUID.randomUUID().toString());
            connection.setDoOutput(true);

            DataOutputStream wr = new DataOutputStream(connection.getOutputStream());
            byte[] encoded_content = content.getBytes("UTF-8");
            wr.write(encoded_content, 0, encoded_content.length);
            wr.flush();
            wr.close();

            StringBuilder response = new StringBuilder();
            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream(), "UTF-8"));
            String line;
            while ((line = in.readLine()) != null) {
                response.append(line);
            }
            in.close();

            return response.toString();
        } catch (Exception e) {
            return null;
        }
    }

    private String getLanguagesAsJson() throws Exception {

        URL url = new URL(host + path);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.setRequestProperty("Ocp-Apim-Subscription-Key", subscriptionKey);
        connection.setDoOutput(true);

        StringBuilder response = new StringBuilder();
        BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream(), "UTF-8"));

        String line;
        while ((line = in.readLine()) != null) {
            response.append(line);
        }
        in.close();

        return response.toString();
    }

    public static class RequestBody {
        String Text;

        public RequestBody(String text) {
            this.Text = text;
        }
    }
}
