package ocrme_backend.servlets.translate.supported_languages;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import javax.servlet.http.HttpSession;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Properties;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import static java.util.logging.Level.WARNING;

/**
 * Created by iuliia on 8/31/17.
 */
public class SupportedLanguagesRequestManager {

    public static final String SECRET_KEY_PARAMETER = "bing.key.parameter";
    public static final String SECRET_KEYS_FILE_PATH = "secret.key.filepath";
    private static String host = "https://api.cognitive.microsofttranslator.com";
    private static String path = "/languages?api-version=3.0";
    private static String params = "&scope=translation";
    private final Logger logger =
            Logger.getLogger(SupportedLanguagesRequestManager.class.getName());
    private String subscriptionKey;

    //todo use DI framework (Dagger) and inject session

    /**
     * @param session
     */
    public SupportedLanguagesRequestManager(HttpSession session) throws IOException {

        String secretKeyFilepath = session.getServletContext().getInitParameter(SECRET_KEYS_FILE_PATH);
        Properties props = new Properties();
        props.load(session.getServletContext().getResourceAsStream(secretKeyFilepath));

        String bingKeyParameter = session.getServletContext().getInitParameter(SECRET_KEY_PARAMETER);
        subscriptionKey = props.getProperty(bingKeyParameter);

        if (subscriptionKey == null || subscriptionKey.isEmpty()) {
            logger.log(WARNING, "subscriptionKey was not obtained");
        }
    }

    private String getLanguagesAsJson() throws Exception {

        URL url = new URL(host + path + params);
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

    private List<SupportedLanguagesResponse.Language> getLanguages(String jsonText) {
        JsonParser parser = new JsonParser();
        JsonObject json = parser.parse(jsonText).getAsJsonObject();

        JsonObject translation = json.getAsJsonObject("translation");

        return translation.entrySet().stream()
                .map(entry -> new SupportedLanguagesResponse.Language(entry.getKey(),
                        entry.getValue().getAsJsonObject().get("name").getAsString()))
                .collect(Collectors.toList());
    }

    public SupportedLanguagesResponse getSupportedLanguages() {

        SupportedLanguagesResponse response = new SupportedLanguagesResponse();
        try {
            List<SupportedLanguagesResponse.Language> languages =
                    getLanguages(getLanguagesAsJson());
            response.setSupportedLanguages(languages);
            response.setStatus(SupportedLanguagesResponse.Status.OK);
        } catch (Exception e) {
            e.printStackTrace();
            response.setStatus(SupportedLanguagesResponse.Status.UNKNOWN_ERROR);
        }
        return response;
    }
}
