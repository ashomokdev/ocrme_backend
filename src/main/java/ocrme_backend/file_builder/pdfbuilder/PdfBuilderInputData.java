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


    //todo result pdf dont influence of sourceHeight and sourceWidth - this fix will also fix a bug with emty pdf in source image rotated
    /**
     * @param sourceHeight source/ocr file height
     * @param sourceWidth  source/ocr file width
     * @param text         ocr result
     */
    public PdfBuilderInputData(int sourceHeight, int sourceWidth, List<TextUnit> text) {

        this.mHeight = sourceHeight;
        this.mWidth = sourceWidth;
        this.text = text;

        cutImage();
        decreaseTextDimensions();
        increaseTextDimensions();
        invertSymmetrically();
    }

    private void increaseTextDimensions() {
        float maxDx = 0;
        float maxDy = 0;

        for (TextUnit unit : text) {
            float llx = unit.getLlx();
            float lly = unit.getLly();
            float urx = unit.getUrx();
            float ury = unit.getUry();

            float dx = 0;
            if (llx < 0) {
                dx = -llx;
            }

            if (dx > maxDx) {
                maxDx = dx;
            }

            if (urx < 0) {
                dx = -urx;
            }

            if (dx > maxDx) {
                maxDx = dx;
            }

            float dy = 0;
            if (lly < 0) {
                dy = -lly;
            }

            if (dy > maxDy) {
                maxDy = dy;
            }

            if (ury < 0) {
                dy = -ury;
            }

            if (dy > maxDy) {
                maxDy = dy;
            }
        }


        for (TextUnit unit : text) {
            float llx = unit.getLlx();
            float lly = unit.getLly();
            float urx = unit.getUrx();
            float ury = unit.getUry();

            unit.setUrx(urx + maxDx);
            unit.setUry(ury + maxDy);
            unit.setLlx(llx + maxDx);
            unit.setLly(lly + maxDy);
        }

        mHeight +=maxDy;
        mWidth +=maxDx;

//
//        //get boundaries size - 10% of image side
//        float textWidth = maxUrx - minLlx;
//        float textHeight = maxLly - minUry;
//
//        float maxSide = textHeight;
//        if (maxSide < textWidth) {
//            maxSide = textWidth;
//        }
//
//        float boundariesSize = maxSide / 20; //5% of image size will use as boundaries
    }

    /**
     * cut white borders of text if needed (if too wide)
     */
    private void cutImage() {
        //get all text llx, lly, urx, ury----------------------------------
        float minLlx = mWidth;
        float maxLly = 0;
        float maxUrx = 0;
        float minUry = mHeight;

        for (TextUnit textUnit : text) {
            float llx = textUnit.getLlx();
            float lly = textUnit.getLly();
            float urx = textUnit.getUrx();
            float ury = textUnit.getUry();

            if (llx < minLlx) {
                minLlx = llx;
            }
            if (lly > maxLly) {
                maxLly = lly;
            }
            if (urx > maxUrx) {
                maxUrx = urx;
            }
            if (ury < minUry) {
                minUry = ury;
            }
        }
        //--------------------------------------------------------------------

        //get boundaries size
        float textWidth = maxUrx - minLlx;
        float textHeight = maxLly - minUry;

        float maxSide = textHeight;
        if (maxSide < textWidth) {
            maxSide = textWidth;
        }

        float boundariesSize = maxSide / 10; //10% of image size will use as boundaries
        //--------------------------------------------------------------------------

        //cut from left side if needed-----------------------------------------
        float dLlx = minLlx - boundariesSize;
        boolean cutLlx = false;
        if (dLlx > 0) {
            //cut - decrease llx for each elem
            cutLlx = true;
        }

        //cut from bottom if needed--------------------------------------------
        float dLly = mHeight - maxLly - boundariesSize;
        boolean cutLly = false;
        if (dLly > 0) {
            //cut
            cutLly = true;
        }

        //cut from right side if needed-----------------------------------------
        float dUrx = mWidth - maxUrx - boundariesSize;
        boolean cutUrx = false;
        if (dUrx > 0) {
            //cut - decrease llx for each elem
            cutUrx = true;
        }

        //cut from top if needed--------------------------------------------
        float dUry = minUry - boundariesSize;
        boolean cutUry = false;
        if (dUry > 0) {
            cutUry = true;
        }

        float newSourceHeight = mHeight;
        float newSourceWidth = mWidth;

        for (int i = 0; i < text.size(); i++) {
            TextUnit textUnit = text.get(i);
            if (cutLlx) {
                float llx = textUnit.getLlx() - dLlx;
                textUnit.setLlx(llx);
                float urx = textUnit.getUrx() - dLlx;
                textUnit.setUrx(urx);
            }
            if (cutUry) {
                float ury = textUnit.getUry() - dUry;
                textUnit.setUry(ury);
                float lly = textUnit.getLly() - dUry;
                textUnit.setLly(lly);
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

        mHeight = (newSourceHeight > 0) ? newSourceHeight : mHeight;
        mWidth = (newSourceWidth > 0) ? newSourceWidth : mWidth;
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
     * decrease text if it bigger than default pdf size
     */
    private void decreaseTextDimensions() {
        if ((mHeight > maxHeightAllowed || mWidth > maxWidthAllowed)) {

            //By how much does mHeight exceed maxHeightAllowed?
            float heightCoefficient = mHeight * 100 / maxHeightAllowed;
            //By how much does mWidth exceed maxWidthAllowed?
            float widthCoefficient = mWidth * 100 / maxWidthAllowed;
            //measure by height.
            if (heightCoefficient > widthCoefficient) {
                for (TextUnit unit : text) {
                    unit.setLlx(unit.getLlx() * maxHeightAllowed / mHeight);
                    unit.setLly(unit.getLly() * maxHeightAllowed / mHeight);
                    unit.setUrx(unit.getUrx() * maxHeightAllowed / mHeight);
                    unit.setUry(unit.getUry() * maxHeightAllowed / mHeight);
                }
            } else {
                for (TextUnit unit : text) {
                    unit.setLlx(unit.getLlx() * maxWidthAllowed / mWidth);
                    unit.setLly(unit.getLly() * maxWidthAllowed / mWidth);
                    unit.setUrx(unit.getUrx() * maxWidthAllowed / mWidth);
                    unit.setUry(unit.getUry() * maxWidthAllowed / mWidth);
                }
            }

            mHeight = maxHeightAllowed;
            mWidth = maxWidthAllowed;
        }
    }

    /**
     * since Google Vision Api returns boundingPoly-s when Coordinates starts from top left corner,
     * but Itext uses coordinate system with bottom left start position -
     * we need invert the result for continue to work with itext.

     * @return text units inverted symmetrically by 0X coordinates.
     */
    private void invertSymmetrically() {
        for (TextUnit unit : text) {
            float wordHeight = unit.getUry() - unit.getLly();
            unit.setLly(mHeight - unit.getLly() - wordHeight);
            unit.setUry(mHeight - unit.getUry() + wordHeight);
        }
    }
}
