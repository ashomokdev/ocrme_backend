package ocrme_backend.utils;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseToken;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.logging.Logger;

import static java.util.logging.Level.INFO;
import static java.util.logging.Level.WARNING;

/**
 * Created by iuliia on 12/19/17.
 */
public class FirebaseAuthUtil {
    private static final Logger logger = Logger.getLogger(FirebaseAuthUtil.class.getName());

    @Nullable
    public static String getUserId(@Nullable String idTokenString) {
        String userId = null;
        FirebaseToken decodedToken = getUserDecodedToken(idTokenString);
        if (decodedToken != null) {
            userId = decodedToken.getUid();
        }
        logger.log(INFO, "User id: " + userId);
        return userId;
    }

    public @Nullable
    static String getUserEmail(@Nullable String idTokenString) {
        String email = null;
        FirebaseToken decodedToken = getUserDecodedToken(idTokenString);
        if (decodedToken != null) {
            email = decodedToken.getEmail();
        }
        logger.log(INFO, "User email: " + email);
        return email;
    }

    private @Nullable
    static FirebaseToken getUserDecodedToken(@Nullable String idTokenString) {
        FirebaseToken decodedToken = null;
        if (idTokenString != null) {
            try {
                initFirebase();
            } catch (Exception e) {
                //already initialized - ignore
            }
            try {
                decodedToken = FirebaseAuth.getInstance().verifyIdTokenAsync(idTokenString).get();
            } catch (Exception e) {
                e.printStackTrace();
                logger.log(WARNING, "error in getUserDecodedToken: " + e.getMessage());
            }
        }
        return decodedToken;
    }

    private static void initFirebase() throws IOException {
        FirebaseOptions options = new FirebaseOptions.Builder()
                .setCredentials(GoogleCredentials.getApplicationDefault())
                .setDatabaseUrl("https://ocrme-77a2b.firebaseio.com")
                .build();

        FirebaseApp.initializeApp(options);
    }

}
