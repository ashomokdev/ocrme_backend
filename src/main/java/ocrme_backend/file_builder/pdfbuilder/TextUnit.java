package ocrme_backend.file_builder.pdfbuilder;

/**
 * Created by iuliia on 6/3/17.
 */
public class TextUnit {
    private String text;

    //    X of top left point
    private float llx;

    //    Y of top left point
    private float lly;

    //    X of bottom right point
    private float urx;

    //    Y of bottom right point
    private float ury;

    public TextUnit(String text, float llx, float lly, float urx, float ury) {
        this.text = text;
        this.llx = llx;
        this.lly = lly;
        this.urx = urx;
        this.ury = ury;
    }

    public String getText() {
        return text;
    }


    float getLlx() {
        return llx;
    }

    public void setLlx(float llx) {
        this.llx = llx;
    }

    float getLly() {
        return lly;
    }

    public void setLly(float lly) {
        this.lly = lly;
    }

    float getUrx() {
        return urx;
    }

    public void setUrx(float urx) {
        this.urx = urx;
    }

    float getUry() {
        return ury;
    }

    public void setUry(float ury) {
        this.ury = ury;
    }
}
