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

    //todo use max height and width to save space - create and add reduseSize method call after reorganizePoints();
    public static final float maxHeightAllowed = PageSize.A4.getHeight();
    public static final float maxWidthAllowed = PageSize.A4.getWidth();

    private float mHeight;
    private float mWidth;

    /**
     * currently list of paragraphs. May also be list of blocks. See implementation of ocrForData method.
     */
    private List<TextUnit> text;

    /**
     * @param text ocr result
     */
    public PdfBuilderInputData(List<TextUnit> text) {

        this.text = text;
        reorganizePoints();
    }

    private void reorganizePoints() {
        if (text.size() > 0) {
            float minLlx = text.get(0).getLlx();
            float maxLly = text.get(0).getLly();
            float maxUrx = text.get(0).getUrx();
            float minUry = text.get(0).getUry();

            for (TextUnit textUnit : text) {
                float llx = textUnit.getLlx();
                float lly = textUnit.getLly();
                float urx = textUnit.getUrx();
                float ury = textUnit.getUry();

                //detect all text boundaries
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

            //check if text goes out of boundaries
            float boundariesSize = (maxLly - minUry) / 8; //1/8 of image size
            float dx = boundariesSize - minLlx; //if dx > 0 - text out of boundaries (closed to edges or non visible,
            // otherwise text is too far from boundaries - image should be cut )
            float dy = boundariesSize - minUry; //if dy > 0 - text out of boundaries (closed to edges or non visible,
            // otherwise text is too far from boundaries - image should be cut )

            for (TextUnit textUnit : text) {
                float llx = textUnit.getLlx();
                float lly = textUnit.getLly();
                float urx = textUnit.getUrx();
                float ury = textUnit.getUry();

                textUnit.setUrx(urx + dx);
                textUnit.setUry(ury + dy);
                textUnit.setLlx(llx + dx);
                textUnit.setLly(lly + dy);
            }

            mWidth = maxUrx + dx + boundariesSize;
            mHeight = maxLly + dy + boundariesSize;
        }
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
}
