package ocrme_backend.file_builder.pdfbuilder;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;

/**
 * Created by iuliia on 6/3/17.
 */
public interface PDFBuilder {
    String buildPdfFile(PdfBuilderInputData data);
    ByteArrayOutputStream buildPdfStream(PdfBuilderInputData data);
}
