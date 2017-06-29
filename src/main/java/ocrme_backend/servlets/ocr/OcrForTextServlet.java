package ocrme_backend.servlets.ocr;

import org.apache.commons.fileupload.FileItemIterator;
import org.apache.commons.fileupload.FileItemStream;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.fileupload.util.Streams;

import javax.annotation.Nullable;
import javax.servlet.AsyncContext;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Created by iuliia on 5/17/17.
 * run for test
 * curl -i -X POST -H "Content-Type: multipart/form-data" -F "file=@/home/iuliia/Documents/items/IMG_8204.JPG" https://imagetotext-149919.appspot.com/ocr_file
 */
public class OcrForTextServlet extends HttpServlet {
    @Override
    public void doPost(HttpServletRequest req, HttpServletResponse response) {

        try {
            byte[] file = extractFile(req);
            String[] languages = req.getParameterValues("language");

            //todo it is better to create a global
            // instance of ExecutorService and use it in all servlets.
            // https://stackoverflow.com/questions/11050186/tomcat-6-thread-pool-for-asynchronous-processing/11053152#11053152
            ExecutorService executor = Executors.newFixedThreadPool(2);

            Future<String> result = executor.submit(new OcrCallableTask(file, languages));
            // shutdown allows the executor to clean up its threads.
            // Also prevents more Callables/Runnables from being submitted.

            executor.shutdown();

            // The call to .get() will block until the executor has
            // completed executing the Callable.
            response.getWriter().write(result.get());
            response.setStatus(HttpServletResponse.SC_ACCEPTED);
            response.setContentType("text/html;charset=UTF-8");

        } catch (Exception e) {
            try {
                e.printStackTrace();
                response.getWriter().write(e.getMessage());
            } catch (IOException ioexception) {
                ioexception.printStackTrace();
            }
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        }
    }

//    @Override
//    public void doPost(HttpServletRequest req, HttpServletResponse response) {
//
//        try {
//            byte[] file = extractFile(req);
//            String[] languages = req.getParameterValues("language");
//
//            //todo it is better to create a global
//            // instance of ExecutorService and use it in all servlets.
//            // https://stackoverflow.com/questions/11050186/tomcat-6-thread-pool-for-asynchronous-processing/11053152#11053152
//            ExecutorService executor = Executors.newFixedThreadPool(2);
//
//            Future<String> result = executor.submit(new OcrCallableTask(file, languages));
//            // shutdown allows the executor to clean up its threads.
//            // Also prevents more Callables/Runnables from being submitted.
//
//            executor.shutdown();
//
//            // The call to .get() will block until the executor has
//            // completed executing the Callable.
//            response.getWriter().write(result.get());
//            response.setStatus(HttpServletResponse.SC_ACCEPTED);
//            response.setContentType("text/html;charset=UTF-8");
//
//        } catch (Exception e) {
//            try {
//                e.printStackTrace();
//                response.getWriter().write(e.getMessage());
//            } catch (IOException ioexception) {
//                ioexception.printStackTrace();
//            }
//            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
//        }
//    }


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
