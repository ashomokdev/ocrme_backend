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

/**
 * Created by iuliia on 5/17/17.
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
                    String jsonResult = processor.doOCR(bytes);

                    response.getWriter().write(jsonResult);
                    response.setStatus(HttpServletResponse.SC_ACCEPTED);
                }
            }
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
