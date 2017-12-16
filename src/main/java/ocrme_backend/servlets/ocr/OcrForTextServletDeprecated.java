package ocrme_backend.servlets.ocr;

import ocrme_backend.ocr.OCRProcessor;
import ocrme_backend.ocr.OcrProcessorImpl;
import org.apache.commons.fileupload.FileItemIterator;
import org.apache.commons.fileupload.FileItemStream;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.fileupload.util.Streams;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

/**
 * Created by iuliia on 5/17/17.
 * for test purposes only
 * run for test
 * curl -i -X POST -H "Content-Type: multipart/form-data" -F "file=@/home/iuliia/Documents/items/IMG_8204.JPG" https://imagetotext-149919.appspot.com/ocr_file
 */

@Deprecated
public class OcrForTextServlet extends HttpServlet {

    @Override
    public void doPost(HttpServletRequest req, HttpServletResponse response) {
        try {
            byte[] file = extractFile(req);
            String[] languages = req.getParameterValues("language");

            OCRProcessor processor = new OcrProcessorImpl();
            String jsonResult;

            if (languages == null || languages.length <= 0) { //run without languages - auto language will be used
                jsonResult = processor.ocrForText(file);
            } else {
                jsonResult = processor.ocrForText(file, Arrays.asList(languages));
            }
            response.setContentType("text/html;charset=UTF-8");
            response.getWriter().write(jsonResult);
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

    private byte[] extractFile(HttpServletRequest req) throws IOException, FileUploadException {
        byte[] bytes = null;
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
        }
        if (bytes == null) {
            throw new FileUploadException("Can not get file");
        }
        return bytes;
    }
}
