package ocrme_backend.file_builder.pdfbuilder;

import java.io.ByteArrayOutputStream;

/**
 * Created by iuliia on 6/3/17.
 */
public interface PDFBuilder {
    String buildPdfFile(PdfBuilderInputData data);

    ByteArrayOutputStream buildPdfStream(PdfBuilderInputData data);
}
