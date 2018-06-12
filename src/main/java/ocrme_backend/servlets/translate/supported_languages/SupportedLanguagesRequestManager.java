package ocrme_backend.servlets.translate.supported_languages;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import javax.net.ssl.HttpsURLConnection;
import javax.servlet.http.HttpSession;
import java.io.*;
import java.net.URL;
import java.util.Properties;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import static java.util.logging.Level.WARNING;

/**
 * Created by iuliia on 8/31/17.
 */
public class SupportedLanguagesRequestManager {

    private String subscriptionKey;
    private final Logger logger = Logger.getLogger(SupportedLanguagesRequestManager.class.getName());

    //todo move to pom
    static String host = "https://api.cognitive.microsofttranslator.com";
    static String path = "/languages?api-version=3.0&scope=translation";
    public static final String SECRET_KEY_PARAMETER = "bing.key";
    public static final String SECRET_KEYS_FILE_PATH = "/WEB-INF/secret_data/secret.properties";

    //todo use DI framework (Dagger) and inject session
    /**
     * @param session
     */
    public SupportedLanguagesRequestManager(HttpSession session) throws IOException {

        Properties props = new Properties();
        props.load(session.getServletContext().getResourceAsStream(SECRET_KEYS_FILE_PATH));
        subscriptionKey = props.getProperty(SECRET_KEY_PARAMETER);

        if (subscriptionKey == null || subscriptionKey.isEmpty()) {
            logger.log(WARNING, "subscriptionKey was not obtained");
        }
    }

    public String GetLanguages() throws Exception {

        URL url = new URL(host + path);
        HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
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

    public static String prettify(String json_text) {
        JsonParser parser = new JsonParser();
        JsonObject json = parser.parse(json_text).getAsJsonObject();

        JsonObject translation = json.getAsJsonObject("translation");
        translation.entrySet().stream()
                .map(entry -> new SupportedLanguagesResponse.Language(entry.getKey(),
                        entry.getValue().getAsJsonObject().get("name").getAsString()))
                .collect(Collectors.toList());

        Gson gson = new GsonBuilder().setPrettyPrinting().create();

        return gson.toJson(json);
    }

    public SupportedLanguagesResponse getSupportedLanguages() {

        SupportedLanguagesResponse response = new SupportedLanguagesResponse();
        try {

            String languages = prettify(GetLanguages());
//            List<Language> languages = Translator.getSupportedLanguages(inputLanguage);
//            response.setSupportedLanguages(languages);
            response.setStatus(SupportedLanguagesResponse.Status.OK);
        } catch (Exception e) {
            e.printStackTrace();
            response.setStatus(SupportedLanguagesResponse.Status.UNKNOWN_ERROR);
        }
        return response;
    }
}
