package ocrme_backend.datastore.gcloud_storage.utils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;

/**
 * Created by iuliia on 7/21/17.
 */
public class FileUtils {
    public static InputStream toInputStream(ByteArrayOutputStream baos) {
        byte[] array = baos.toByteArray();
        InputStream is = new ByteArrayInputStream(array);
        return is;
    }
}
