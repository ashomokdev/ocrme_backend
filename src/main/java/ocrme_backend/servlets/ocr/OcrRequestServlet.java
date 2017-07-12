package ocrme_backend.servlets.ocr;

import ocrme_backend.file_builder.pdfbuilder.PDFData;
import ocrme_backend.ocr.OCRProcessor;
import org.apache.commons.fileupload.FileItemIterator;
import org.apache.commons.fileupload.FileItemStream;
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
 * Created by iuliia on 6/27/17.
 */
public class OcrRequestServlet extends HttpServlet {

//    @Override
//    public void doPost(HttpServletRequest req, HttpServletResponse response) {
//
//        ServletFileUpload upload = new ServletFileUpload();
//
//        try {
//            FileItemIterator it = upload.getItemIterator(req);
//
//            while (it.hasNext()) {
//                FileItemStream item = it.next();
//                String fieldName = item.getFieldName();
//                InputStream fieldValue = item.openStream();
//
//                if ("file".equals(fieldName)) {
//                    ByteArrayOutputStream out = new ByteArrayOutputStream();
//                    Streams.copy(fieldValue, out, true);
//                    byte[] bytes = out.toByteArray();
//                    String[] languages = req.getParameterValues("language");
//
//                    OcrResponse ocrResponse = doTask(bytes, languages);
//
//                    response.setContentType("application/json");
//                    response.setCharacterEncoding("UTF-8");
//                    response.getWriter().print(ocrResponse);
//                    response.getWriter().flush();
//                    response.setStatus(HttpServletResponse.SC_ACCEPTED);
//                }
//            }
//        } catch (Exception e) {
//            try {
//                e.printStackTrace();
//                response.getWriter().write(e.getMessage());
//            } catch (IOException e1) {
//                e1.printStackTrace();
//            }
//            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
//        }
//    }
//
//    private OcrResponse doTask(byte[] image, String[] languages) throws IOException, GeneralSecurityException {
//        OCRProcessor processor = new OCRProcessorImpl();
//        PDFData data;
//        OcrResponse response = new OcrResponse();
////
////        BufferedImage bimg = ImageIO.read(new File(filePath));
////        int sourceWidth = bimg.getWidth();
////        int sourceHeight = bimg.getHeight();
////
////        if (languages == null || languages.length <= 0) //run without languages - auto language will be used
////        {
////            data = processor.ocrForData(bytes);
////        } else {
////            data = processor.ocrForData(bytes, Arrays.asList(languages));
////        }
//        return response;
//    }
//
//    @Override
//    protected boolean doRequest(final RequestResponseKey rrk) throws IOException, ServletException {
//        HttpServletRequest req = rrk.getRequest();
//        HttpServletResponse res = rrk.getResponse();
//
////        TimerManagerFactory.getTimerManagerFactory()
////                .getDefaultTimerManager().schedule
////                (new TimerListener() {
////                    public void timerExpired(Timer timer) {
////                        try {
////                            AbstractAsyncServlet.notify(rrk, null);
////                        } catch (Exception e) {
////                            e.printStackTrace();
////                        }
////                    }
////                }, 2000);
//        return true;
//
//    }
//
//    @Override
//    protected void doResponse(RequestResponseKey requestResponseKey, Object o) throws IOException, ServletException {
//
//    }
//
//    @Override
//    protected void doTimeout(RequestResponseKey requestResponseKey) throws IOException, ServletException {
//
//    }
}
