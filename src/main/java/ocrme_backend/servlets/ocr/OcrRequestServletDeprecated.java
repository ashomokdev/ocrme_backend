package ocrme_backend.servlets.ocr;

import com.google.gson.Gson;
import org.apache.commons.fileupload.FileItemIterator;
import org.apache.commons.fileupload.FileItemStream;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.fileupload.util.Streams;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;

/**
 * Created by iuliia on 5/17/17.
 * run for test
 * curl -i -X POST -H "Content-Type: multipart/form-data" -F "file=@/home/iuliia/Documents/items/obaby/IMG_8771.JPG" https://imagetotext-149919.appspot.com/ocr_request
 * curl -i -X POST -H "Content-Type: multipart/form-data" -F "file=@/home/iuliia/Documents/idea_projects/ocr_me/ocrmeGVisionAppEngine/src/test/resources/test_imgs/rus.jpg" http://localhost:8080/ocr_request
 * curl -i -X POST -H "Content-Type: multipart/form-data" -F "file=@/home/iuliia/Documents/idea_projects/ocr_me/ocrmeGVisionAppEngine/src/test/resources/test_imgs/rus.jpg" http://localhost:8080/ocr_request?language=ru
 * curl -i -X POST -H "Content-Type: multipart/form-data" -F "file=@/home/iuliia/Documents/idea_projects/ocr_me/ocrmeGVisionAppEngine/src/test/resources/test_imgs/rus.jpg" https://imagetotext-149919.appspot.com/ocr_request?language=ru
 * <p>
 * takes image bytes, that is why deprecated - use OcrRequestServlet instead of this.
 * <p>
 * takes image bytes, that is why deprecated - use OcrRequestServlet instead of this.
 */

/**
 * takes image bytes, that is why deprecated - use OcrRequestServlet instead of this.
 */

/**
 * for usage uncommit in web.xml firstly
 */
@Deprecated
public class OcrRequestServletDeprecated extends HttpServlet {

    @Override
    public void doPost(HttpServletRequest req, HttpServletResponse response) {

        try {
            OcrResponse ocrResponse = doTask(req);

            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");

            String json = new Gson().toJson(ocrResponse);
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

    private OcrResponse doTask(HttpServletRequest req) throws IOException, GeneralSecurityException, FileUploadException, ServletException {
        InputImage file = extractFile(req);
        String[] languages = req.getParameterValues("language");

        OcrRequestManager manager = new OcrRequestManager(file.filename, file.bytes, languages, req.getSession());
        OcrResponse response = manager.process();
        return response;
    }


    private InputImage extractFile(HttpServletRequest req) throws IOException, FileUploadException {
        InputImage image = new InputImage();
        byte[] bytes = null;
        String filename = "default.jpg";
        ServletFileUpload upload = new ServletFileUpload();

        FileItemIterator it = upload.getItemIterator(req);

        while (it.hasNext()) {
            FileItemStream item = it.next();
            String fieldName = item.getFieldName();
            InputStream fieldValue = item.openStream();

            if ("file".equals(fieldName)) {
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                Streams.copy(fieldValue, out, true);
                bytes = out.toByteArray();
            }
            filename = item.getName();
        }
        if (bytes == null) {
            throw new FileUploadException("Can not get file");
        }

        image.bytes = bytes;
        image.filename = filename;

        return image;
    }

    private class InputImage {
        private byte[] bytes;
        private String filename;
    }
}
