package ocrme_backend.file_builder.pdfbuilder;

import com.itextpdf.text.PageSize;

import java.util.List;

/**
 * Created by iuliia on 6/7/17.
 */

/**
 * Text and size we need to build PDF file from ocr result
 * A4 or smaller PDF allowed
 */
public class PDFData extends FileData {

    public static final float maxHeightAllowed = PageSize.A4.getHeight();
    public static final float maxWidthAllowed = PageSize.A4.getWidth();

    private float mHeight;
    private float mWidth;

    /**
     * currently list of paragraphs. May also be list of blocks. See implementation of ocrForData method.
     */
    private List<TextUnit> text;

    private boolean hasError;
    private String errorMessage;

    /**
     * @param sourceHeight source/ocr file height
     * @param sourceWidth  source/ocr file width
     * @param text         ocr result
     */
    public PDFData(int sourceHeight, int sourceWidth, List<TextUnit> text) {
        if (sourceHeight > maxHeightAllowed || sourceWidth > maxWidthAllowed) {
            mHeight = maxHeightAllowed;
            mWidth = maxWidthAllowed;
            this.text = decreaseTextDimensions(sourceHeight, sourceWidth, text);

        } else {
            mHeight = sourceHeight;
            mWidth = sourceWidth;
            this.text = text;
        }
        this.text = invertSymmetrically(this.text, mHeight);
    }

    public String getSimpleText() {
        StringBuilder simpleText = new StringBuilder();
        for (TextUnit unit : text) {
            simpleText.append(unit.getText());
        }
        return simpleText.toString();
    }

    public List<TextUnit> getText() {
        return text;
    }

    public float getmHeight() {
        return mHeight;
    }

    public float getmWidth() {
        return mWidth;
    }

    public boolean isHasError() {
        return hasError;
    }

    public void setHasError(boolean hasError) {
        this.hasError = hasError;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    /**
     * since text units have boundingPoly-s bigger, than current PDF size, we need recalculate boundingPoly-s.
     *
     * @param sourceHeight original text height
     * @param sourceWidth  original text width
     * @param text         original text units
     */
    private List<TextUnit> decreaseTextDimensions(int sourceHeight, int sourceWidth, List<TextUnit> text) {
        //By how much does sourceHeight exceed maxHeightAllowed?
        float heightCoefficient = sourceHeight * 100 / maxHeightAllowed;

        //By how much does sourceWidth exceed maxWidthAllowed?
        float widthCoefficient = sourceWidth * 100 / maxWidthAllowed;

        //measure by height. Height of source will set to maxHeightAllowed.
        if (heightCoefficient > widthCoefficient) {
            for (TextUnit unit : text) {
                unit.setLlx(unit.getLlx() * maxHeightAllowed / sourceHeight);
                unit.setLly(unit.getLly() * maxHeightAllowed / sourceHeight);
                unit.setUrx(unit.getUrx() * maxHeightAllowed / sourceHeight);
                unit.setUry(unit.getUry() * maxHeightAllowed / sourceHeight);
            }
        } else {
            for (TextUnit unit : text) {
                unit.setLlx(unit.getLlx() * maxWidthAllowed / sourceWidth);
                unit.setLly(unit.getLly() * maxWidthAllowed / sourceWidth);
                unit.setUrx(unit.getUrx() * maxWidthAllowed / sourceWidth);
                unit.setUry(unit.getUry() * maxWidthAllowed / sourceWidth);
            }
        }
        return text;
    }

    /**
     * since Google Vision Api returns boundingPoly-s when Coordinates starts from top left corner,
     * but Itext uses coordinate system with bottom left start position -
     * we need invert the result for continue to work with itext.
     *
     * @param text
     * @param mHeight
     * @return text units inverted symmetrically by 0X coordinates.
     */
    private List<TextUnit> invertSymmetrically(List<TextUnit> text, float mHeight) {
        for (TextUnit unit : text) {
            unit.setLly(mHeight - unit.getLly());
            unit.setUry(mHeight - unit.getUry());
        }
        return text;
    }
}
