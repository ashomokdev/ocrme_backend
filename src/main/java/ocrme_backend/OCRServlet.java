package ocrme_backend;

import ocrme_backend.ocr.OCRProcessor;
import ocrme_backend.ocr.OCRProcessorImpl;
import org.apache.commons.fileupload.FileItemIterator;
import org.apache.commons.fileupload.FileItemStream;
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
 * run for test
 * curl -i -X POST -H "Content-Type: multipart/form-data" -F "file=@/home/iuliia/Documents/items/IMG_8204.JPG" https://imagetotext-149919.appspot.com/ocr_file
 */
public class OCRServlet extends HttpServlet {

    @Override
    public void doPost(HttpServletRequest req, HttpServletResponse response) {

        ServletFileUpload upload = new ServletFileUpload();

        try {
            FileItemIterator it = upload.getItemIterator(req);

            while (it.hasNext()) {
                FileItemStream item = it.next();
                String fieldName = item.getFieldName();
                InputStream fieldValue = item.openStream();

                if ("file".equals(fieldName)) {
                    ByteArrayOutputStream out = new ByteArrayOutputStream();
                    Streams.copy(fieldValue, out, true);
                    byte[] bytes = out.toByteArray();

                    OCRProcessor processor = new OCRProcessorImpl();

                    String jsonResult;
                    String[] languages = req.getParameterValues("language");
                    if (languages == null || languages.length <= 0) //run without languages - auto language will be used
                    {
                        jsonResult = processor.ocrForText(bytes);
                    } else {
                        jsonResult = processor.ocrForText(bytes, Arrays.asList(languages));
                    }
                    response.setContentType("text/html;charset=UTF-8");
                    response.getWriter().write(jsonResult);
                    response.setStatus(HttpServletResponse.SC_ACCEPTED);
                }
            }
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
}
