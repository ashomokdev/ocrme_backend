package ocrme_backend.servlets.ocr;

import org.apache.commons.fileupload.FileItemIterator;
import org.apache.commons.fileupload.FileItemStream;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.servlet.ServletFileUpload;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.security.GeneralSecurityException;

/**
 * Created by iuliia on 5/17/17.
 * run for test
 * curl -i -X POST -H "Content-Type: multipart/form-data" -F "file=@/home/iuliia/Documents/items/obaby/IMG_8771.JPG" https://imagetotext-149919.appspot.com/ocr_request
 * curl -i -X POST -H "Content-Type: multipart/form-data" -F "file=@/home/iuliia/Documents/idea_projects/ocr_me/ocrmeGVisionAppEngine/src/test/resources/test_imgs/rus.jpg" http://localhost:8080/ocr_request
 * curl -i -X POST -H "Content-Type: multipart/form-data" -F "file=@/home/iuliia/Documents/idea_projects/ocr_me/ocrmeGVisionAppEngine/src/test/resources/test_imgs/rus.jpg" http://localhost:8080/ocr_request?language=ru
 */
public class OcrRequestServlet extends HttpServlet {

    @Override
    public void doPost(HttpServletRequest req, HttpServletResponse response) {

        try {
            OcrResponse ocrResponse = doTask(req);

            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            response.getWriter().print(ocrResponse.getTextResult());
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
        FileItemIterator file = extractFile(req);
        String[] languages = req.getParameterValues("language");

        OcrRequestManager manager = new OcrRequestManager(file, languages, req.getSession());
        OcrResponse response = manager.processForResult();
        return response;
    }


    private  FileItemIterator extractFile(HttpServletRequest req) throws IOException, FileUploadException {
        ServletFileUpload upload = new ServletFileUpload();
        FileItemIterator it = upload.getItemIterator(req);
        return it;
    }
}
