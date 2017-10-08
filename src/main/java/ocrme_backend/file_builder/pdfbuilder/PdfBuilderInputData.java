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
public class PdfBuilderInputData {

    public static final float maxHeightAllowed = PageSize.A4.getHeight();
    public static final float maxWidthAllowed = PageSize.A4.getWidth();

    private float mHeight;
    private float mWidth;

    /**
     * currently list of paragraphs. May also be list of blocks. See implementation of ocrForData method.
     */
    private List<TextUnit> text;

    //todo cut pdf in needed

    /**
     * @param sourceHeight source/ocr file height
     * @param sourceWidth  source/ocr file width
     * @param text         ocr result
     */
    public PdfBuilderInputData(int sourceHeight, int sourceWidth, List<TextUnit> text) {
        //get all text llx, lly, urx, ury----------------------------------
        float minLlx = sourceWidth;
        float minLly = sourceHeight;
        float maxUrx = 0;
        float maxUry = 0;

        for (TextUnit textUnit : text) {
            float llx = textUnit.getLlx();
            float lly = textUnit.getLly();
            float urx = textUnit.getUrx();
            float ury = textUnit.getUry();

            if (llx < minLlx) {
                minLlx = llx;
            }
            if (lly < minLly) {
                minLly = lly;
            }
            if (urx > maxUrx) {
                maxUrx = urx;
            }
            if (ury > maxUry) {
                maxUry = ury;
            }
        }
        //--------------------------------------------------------------------

        //todo edit
        //get boundaries size - 10% of image side
        float textWidth = maxUrx - minLlx;
        float textHeight = maxUry - minLly;

        float maxSide = textHeight;
        if (maxSide < textWidth) {
            maxSide = textWidth;
        }

        float boundariesSize = maxSide / 20; //5% of image size will use as boundaries
        //--------------------------------------------------------------------------

        //cut from left side if needed-----------------------------------------
        float dLlx = minLlx - boundariesSize;
        boolean cutLlx = false;
        if (dLlx > 0) {
            //cut - decrease llx for each elem
            cutLlx = true;
        }

        //cut from bottom if needed--------------------------------------------
        float dLly = minLly - boundariesSize;
        boolean cutLly = false;
        if (dLly > 0) {
            //cut
            cutLly = true;
        }

        //cut from right side if needed-----------------------------------------
        float dUrx = sourceWidth - maxUrx - boundariesSize;
        boolean cutUrx = false;
        if (dUrx > 0) {
            //cut - decrease llx for each elem
            cutUrx = true;
        }

        //cut from top if needed--------------------------------------------
        float dUry =  sourceHeight -maxUry - boundariesSize;
        boolean cutUry = false;
        if (dUry > 0) {
            cutUry = true;
        }

        int newSourceHeight = sourceHeight;
        int newSourceWidth = sourceWidth;

        for (int i = 0; i < text.size(); i++) {
            TextUnit textUnit = text.get(i);
            if (cutLlx) {
                float llx = textUnit.getLlx() - dLlx;
                textUnit.setLlx(llx);
                float urx = textUnit.getUrx() - dLlx;
                textUnit.setUrx(urx);
            }
            if (cutUry) {
                float lly = textUnit.getLly() - dLly;
                textUnit.setLly(lly);
                float ury = textUnit.getUry() - dLly;
                textUnit.setUry(ury);
            }
            text.set(i, textUnit);
        }
        if (cutLlx) {
            newSourceWidth -= dLlx;
        }
        if (cutLly) {
            newSourceHeight -= dLly;
        }
        if (cutUrx) {
            newSourceWidth -= dUrx;
        }
        if (cutUry) {
            newSourceHeight -= dUry;
        }
        //cut image END--------------------------------------------------------

        if ((mHeight > maxHeightAllowed || mWidth > maxWidthAllowed)) {

            mHeight = maxHeightAllowed;
            mWidth = maxWidthAllowed;
            this.text = decreaseTextDimensions(
                    mHeight, mWidth, maxHeightAllowed, maxWidthAllowed, text);
        } else {
            mHeight = (newSourceHeight > 0) ? newSourceHeight : sourceHeight;
            mWidth = (newSourceWidth > 0) ? newSourceWidth : sourceWidth;
            this.text = text;
        }
//        this.text = invertSymmetrically(this.text, mHeight);
    }

    /**
     * @return Simple text without new-line symbols.
     */
    public String getSimpleText() {
        StringBuilder simpleText = new StringBuilder();
        for (TextUnit unit : text) {
            simpleText.append(unit.getText()).append(" ");
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

    /**
     * since text units have boundingPoly-s bigger, than current PDF size, we need recalculate boundingPoly-s.
     *  @param sourceHeight original text height
     * @param sourceWidth  original text width
     * @param mHeight
     * @param mWidth
     * @param text         original text units
     */
    private List<TextUnit> decreaseTextDimensions(
            float sourceHeight, float sourceWidth, float mHeight, float mWidth, List<TextUnit> text) {
        //By how much does sourceHeight exceed mHeight?
        float heightCoefficient = sourceHeight * 100 / mHeight;
        //By how much does sourceWidth exceed mWidth?
        float widthCoefficient = sourceWidth * 100 / mWidth;
        //measure by height. Height of source will set to mHeight.
        if (heightCoefficient > widthCoefficient) {
            for (TextUnit unit : text) {
                unit.setLlx(unit.getLlx() * mHeight / sourceHeight);
                unit.setLly(unit.getLly() * mHeight / sourceHeight);
                unit.setUrx(unit.getUrx() * mHeight / sourceHeight);
                unit.setUry(unit.getUry() * mHeight / sourceHeight);
            }
        } else {
            for (TextUnit unit : text) {
                unit.setLlx(unit.getLlx() * mWidth / sourceWidth);
                unit.setLly(unit.getLly() * mWidth / sourceWidth);
                unit.setUrx(unit.getUrx() * mWidth / sourceWidth);
                unit.setUry(unit.getUry() * mWidth / sourceWidth);
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
            float wordHeight = unit.getUry() - unit.getLly();
            unit.setLly(mHeight - unit.getLly() - wordHeight);
            unit.setUry(mHeight - unit.getUry() + wordHeight);
        }
        return text;
    }
}
