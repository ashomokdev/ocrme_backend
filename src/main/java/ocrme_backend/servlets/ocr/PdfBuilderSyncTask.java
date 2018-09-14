package ocrme_backend.servlets.ocr;

import com.google.cloud.storage.Blob;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.parser.PdfTextExtractor;
import ocrme_backend.datastore.gcloud_storage.utils.CloudStorageHelper;
import ocrme_backend.file_builder.pdfbuilder.PDFBuilder;
import ocrme_backend.file_builder.pdfbuilder.PDFBuilderImpl;
import ocrme_backend.file_builder.pdfbuilder.PdfBuilderInputData;
import ocrme_backend.file_builder.pdfbuilder.PdfBuilderOutputData;
import ocrme_backend.file_builder.pdfbuilder.PdfBuilderOutputData.Status;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.servlet.http.HttpSession;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by iuliia on 7/13/17.
 * builds pdf and save in Google cloud storage
 */

//todo inject session using di framework
class PdfBuilderSyncTask {
    public static final String BUCKET_FOR_PDFS_PARAMETER = "ocrme.bucket";
    public static final String DIRECTORY_FOR_PDFS_PARAMETER = "ocrme.dir.pdf";
    private static Logger logger;
    @Nonnull
    private HttpSession session;



    //Use PdfBuilderInputData(pdf with searchable text) OR String gcsImageUri(image pdf)
    @Nullable
    private PdfBuilderInputData data;
    @Nullable
    private String gcsImageUri;



    PdfBuilderSyncTask(PdfBuilderInputData data, HttpSession session) {
        this.data = data;
        this.session = session;
        logger = Logger.getLogger(PdfBuilderSyncTask.class.getName());
    }

    PdfBuilderSyncTask(String gcsImageUri, HttpSession session) {
        this.gcsImageUri = gcsImageUri;
        this.session = session;
        logger = Logger.getLogger(PdfBuilderSyncTask.class.getName());
    }

    PdfBuilderOutputData execute() {
        PdfBuilderOutputData result = new PdfBuilderOutputData();
        try {
            FileUploadedResult fileUploadedResult = buildPdf();
            result.setGsUrl(fileUploadedResult.gsLink);
            result.setMediaUrl(fileUploadedResult.mediaLink);
            result.setStatus(Status.OK);
            logger.log(Level.INFO, "pdf generated, url for download: " + fileUploadedResult.mediaLink);
        } catch (TextNotFoundException e) {
            result.setStatus(Status.PDF_CAN_NOT_BE_CREATED_EMPTY_DATA);
            logger.log(Level.INFO, "pdf not generated, empty data");
        } catch (LanguageNotSupportedException e) {
            result.setStatus(Status.PDF_CAN_NOT_BE_CREATED_LANGUAGE_NOT_SUPPORTED);
            logger.log(Level.INFO, "pdf not generated, language not supported");
        } catch (Exception e) {
            result.setStatus(Status.UNKNOWN_ERROR);
            logger.log(Level.WARNING, "pdf not generated, unknown error");
            e.printStackTrace();
        }
        return result;
    }

    private FileUploadedResult buildPdf() throws Exception {
        if (gcsImageUri != null) {
            return buildImagePdf(gcsImageUri);
        } else if (data != null) {
            return buildTextPdf(data);
        } else {
            throw new Exception("Error: Wrong input data for building Pdf.");
        }
    }

    private FileUploadedResult buildImagePdf(String gcsImageUri) throws Exception {
        if (gcsImageUri == null || gcsImageUri.isEmpty()) {
            throw new Exception("Error. gcsImageUri == null or empty");
        } else {
            byte[] imageBytes = new CloudStorageHelper().downloadFile(gcsImageUri);
            if (imageBytes != null) {
                PDFBuilderImpl pdfBuilder = new PDFBuilderImpl(session);
                ByteArrayOutputStream outputStream = pdfBuilder.buildPdfStream(imageBytes);

                if (outputStream == null) {
                    throw new Exception("Can not build pdf from image for some reason");
                }
                return uploadToGoogleStorage(outputStream.toByteArray());
            }
        }
        return null;
    }

    private FileUploadedResult buildTextPdf(PdfBuilderInputData data) throws TextNotFoundException, LanguageNotSupportedException {
        if (data.getText() == null || data.getText().size() == 0) {
            throw new TextNotFoundException();
        }
        PDFBuilder pdfBuilder = new PDFBuilderImpl(session);
        ByteArrayOutputStream outputStream = pdfBuilder.buildPdfStream(data);

        if (outputStream != null && isFileEmpty(outputStream)) {
            throw new LanguageNotSupportedException();
        }
        return uploadToGoogleStorage(outputStream.toByteArray());
    }


    private FileUploadedResult uploadToGoogleStorage(byte[] file) {
        String fileName = "file.pdf";
        FileUploadedResult result;
        try {
            CloudStorageHelper helper = new CloudStorageHelper();
            String bucketName = session.getServletContext().getInitParameter(BUCKET_FOR_PDFS_PARAMETER);
            String directoryName = session.getServletContext().getInitParameter(DIRECTORY_FOR_PDFS_PARAMETER);
            helper.createBucket(bucketName);
            Blob blob = helper.uploadFileForBlob(file, fileName, directoryName, bucketName);
            result = new FileUploadedResult(
                    "gs://" + blob.getBucket() + "/" + blob.getName(),
                    blob.getMediaLink()
            );
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
        return result;
    }


    /**
     * check if pdf file contains any text
     *
     * @param outputStream
     * @return
     */
    private boolean isFileEmpty(ByteArrayOutputStream outputStream) {
        String allText = "";
        try {
            PdfReader reader = new PdfReader(outputStream.toByteArray());

            for (int page = 1; page <= 1; page++) {
                allText = PdfTextExtractor.getTextFromPage(reader, page);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return (allText == null) || allText.length() < 1;
    }

    private class TextNotFoundException extends Exception {
    }

    private class LanguageNotSupportedException extends Exception {
    }

    private class FileUploadedResult {
        String gsLink;
        String mediaLink;

        FileUploadedResult(String gsLink, String mediaLink) {
            this.gsLink = gsLink;
            this.mediaLink = mediaLink;
        }
    }
}
