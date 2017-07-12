package ocrme_backend.datastore.utils;

/**
 * Created by iuliia on 6/12/17.
 */
public class ImageFile {
    private byte[] file;
    private int width;
    private int height;

    public ImageFile(byte[] file, int width, int height) {
        this.file = file;
        this.width = width;
        this.height = height;
    }

    public byte[] getFile() {
        return file;
    }

    public void setFile(byte[] file) {
        this.file = file;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }
}
