package ocrme_backend.file_builder.pdfbuilder;

import com.itextpdf.text.BadElementException;
import com.itextpdf.text.DocumentException;

import javax.annotation.Nullable;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * Created by iuliia on 6/3/17.
 */
public interface PDFBuilder {

    @Nullable
    ByteArrayOutputStream buildPdfStream(byte[] bytes) throws IOException, DocumentException;

    ByteArrayOutputStream buildPdfStream(PdfBuilderInputData data);
}
