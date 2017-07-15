package ocrme_backend.servlets.ocr;

import ocrme_backend.datastore.gcloud_storage.utils.CloudStorageHelper;
import ocrme_backend.file_builder.pdfbuilder.PDFBuilder;
import ocrme_backend.file_builder.pdfbuilder.PDFBuilderImpl;
import ocrme_backend.file_builder.pdfbuilder.PDFData;
import ocrme_backend.ocr.OCRProcessor;
import ocrme_backend.ocr.OcrProcessorImpl;
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
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Paths;
import java.security.GeneralSecurityException;
import java.util.Arrays;

/**
 * Created by iuliia on 5/17/17.
 * run for test
 * curl -i -X POST -H "Content-Type: multipart/form-data" -F "file=@/home/iuliia/Documents/items/obaby/IMG_8771.JPG" https://imagetotext-149919.appspot.com/ocr_request
 * curl -i -X POST -H "Content-Type: multipart/form-data" -F "file=@/home/iuliia/Documents/items/obaby/IMG_8771.JPG" http://localhost:8080/ocr_request
 */
public class OcrRequestServlet extends HttpServlet {

    @Override
    public void doPost(HttpServletRequest req, HttpServletResponse response) {

        try {
            OcrResponse ocrResponse = doTask(req);

            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            response.getWriter().print(ocrResponse);
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
        FileItemStream file = extractFile(req);
        String[] languages = req.getParameterValues("language");

        OcrRequestManager manager = new OcrRequestManager(file, languages, req.getSession());
        OcrResponse response = manager.processForResult();
        return response;
    }



    private FileItemStream extractFile(HttpServletRequest req) throws IOException, FileUploadException {
        ServletFileUpload upload = new ServletFileUpload();

        FileItemIterator it = upload.getItemIterator(req);

        FileItemStream item = null;
        while (it.hasNext()) {
            item = it.next();
        }
        return item;
    }
}
