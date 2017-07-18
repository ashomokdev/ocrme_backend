package ocrme_backend.file_builder.pdfbuilder;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfWriter;

import javax.annotation.Nullable;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpSession;
import java.io.File;
import java.io.FileOutputStream;
import java.util.List;

/**
 * Created by iuliia on 6/2/17.
 */
//todo
// https://stackoverflow.com/questions/7355025/create-pdf-with-java
// http://www.techsagar.com/top-3-open-source-java-libraries-for-creating-and-manipulating-pdf-documents/

public class PDFBuilderImpl implements PDFBuilder {

    public static final String FONT_PATH = "/WEB-INF/fonts/FreeSans.ttf";
    private final HttpSession session;
    public static final String uploadsDir = "/temp/";

    public PDFBuilderImpl(HttpSession session) {
        this.session = session;
    }

    @Nullable
    @Override
    public String buildPDF(PDFData data) {

        String filename = generateFileName();
        String path = createTempFile(filename);

        Document document = new Document(new Rectangle(data.getmWidth(), data.getmHeight())); //specified size  Document doc = new Document(new Rectangle(570, 924f)); http://developers.itextpdf.com/question/how-add-text-inside-rectangle
        try {
            PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(path));
            document.open();
            addMetaData(document);
            addContent(writer, data.getText());
            document.close();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

        return path;
    }

    /**
     * add simple string to Pdf file
     *
     * @param path of pdf file
     */
    public void addSimpleContent(String path) {
        Document document = new Document(); //specified size  Document doc = new Document(new Rectangle(570, 924f)); http://developers.itextpdf.com/question/how-add-text-inside-rectangle
        try {
            PdfWriter.getInstance(document, new FileOutputStream(path));
            document.open();

            Paragraph p = new Paragraph("Simple string", new Font(Font.FontFamily.HELVETICA, 22));
            p.setAlignment(Element.ALIGN_CENTER);
            document.add(p);

            document.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void addContent(PdfWriter writer, List<TextUnit> textUnits) throws Exception {
        PdfContentByte contentByte = writer.getDirectContent();
        contentByte.saveState();
        contentByte.beginText();

        contentByte.setRGBColorFill(0x00, 0x00, 0x00);


        ServletContext context = session.getServletContext();
        String fontPath = context.getRealPath(FONT_PATH);

        assert fontPath != null;
        Font bf = FontFactory.getFont(fontPath, BaseFont.IDENTITY_H, true);

        for (TextUnit text : textUnits) {
            float llx = text.getLlx();
            float lly = text.getLly();
            float urx = text.getUrx();
            float ury = text.getUry();

            float fontSize = getMaxFontSize(bf, text.getText(), urx - llx);
            contentByte.setFontAndSize(bf.getCalculatedBaseFont(true), fontSize);

            contentByte.setTextMatrix(llx, lly);
            contentByte.showText(text.getText());
        }
        contentByte.endText();
        contentByte.restoreState();
    }

//    private void addContent(PdfWriter writer, List<TextUnit> textUnits) throws Exception {
//        PdfContentByte contentByte = writer.getDirectContent();
//        contentByte.saveState();
//        contentByte.beginText();
//
//        for (TextUnit text : textUnits) {
//            ColumnText ct = new ColumnText(contentByte);
//
//            float llx = text.getLlx();
//            float lly = text.getLly();
//            float urx = text.getUrx();
//            float ury = text.getUry();
//
//            ct.setSimpleColumn(new Rectangle(llx, lly, urx, ury));
//            ct.addElement(
//                    new Paragraph(text.getText()));
//            ct.go();
//        }
//        contentByte.endText();
//        contentByte.restoreState();
//
//    }

    //todo can it be calculated more accurate?
    private float getMaxFontSize(Font bf, String text, float width) {
        int textWidth = bf.getCalculatedBaseFont(true).getWidth(text);
        return (1000 * width) / textWidth;
    }

    // iText allows to add metadata to the PDF which can be viewed in your AdobeReader under File -> Properties
    private void addMetaData(Document document) {
        document.addTitle("Ocr result");
        document.addAuthor("OCRme");
    }

    /**
     * Generate unique filename for PDF file.
     *
     * @return filename
     */
    private String generateFileName() {
        String sessionId = session.getId();
        return sessionId + ".pdf";
    }

    /**
     * create file in temp directory
     *
     * @param filename
     * @return file path
     */
    @Nullable
    public String createTempFile(String filename) {
        try {
            String realPathtoUploads = session.getServletContext().getRealPath(uploadsDir);
            if (!new File(realPathtoUploads).exists()) {
                new File(realPathtoUploads).mkdir();
            }

            File file = new File(realPathtoUploads, filename);
            file.createNewFile(); // if file already exists will do nothing
            new FileOutputStream(file, false);
            return file.getPath();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}

