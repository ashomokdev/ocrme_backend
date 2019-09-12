package ocrme_backend.servlets.smart_crop;
//alternatives https://stackoverflow.com/questions/603283/what-is-the-best-java-image-processing-library-approach

import marvin.image.MarvinImage;
import marvin.plugin.MarvinImagePlugin;
import marvin.util.MarvinAttributes;
import marvin.util.MarvinPluginLoader;
import ocrme_backend.datastore.gcloud_storage.utils.CloudStorageHelper;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;

public class ContourEdgePointsProcessor {

    public static PointF[] process(String gcsImageUri) throws Exception {
        if (gcsImageUri == null || gcsImageUri.isEmpty()) {
            throw new Exception("Error. gcsImageUri == null or empty");
        } else {
            byte[] imageBytes = new CloudStorageHelper().downloadFile(gcsImageUri);
            if (imageBytes != null && imageBytes.length>0) {
                BufferedImage bufferedImage = createImageFromBytes(imageBytes);
                MarvinImage marvinImage = new MarvinImage(bufferedImage);

                // Load plug-in
                MarvinImagePlugin moravec = MarvinPluginLoader.loadImagePlugin("org.marvinproject.image.corner.moravec");
                MarvinAttributes attr = new MarvinAttributes();
                // Process and save output image
                moravec.setAttribute("threshold", 2000);
                moravec.process(marvinImage, null, attr);
                Point[] boundaries = boundaries(attr);


//                //
//                marvinImage = showCorners(marvinImage, boundaries, 12);
//                MarvinImageIO.saveImage(marvinImage, "out.jpg");


                return transformPoints(boundaries);
            } else {
                throw new Exception("Error. Can not download image bytes from Google Cloud using link " + gcsImageUri);
            }
        }
    }

    private static MarvinImage showCorners(MarvinImage image, Point[] points, int rectSize){
        MarvinImage ret = image.clone();
        for(Point p:points){
            ret.fillRect(p.x-(rectSize/2), p.y-(rectSize/2), rectSize, rectSize, Color.red);
        }
        return ret;
    }

    private static PointF[] transformPoints(Point[] boundaries) {
        PointF[] result = new PointF[boundaries.length];
        for (int i = 0; i < boundaries.length; i++) {
            PointF pointF = new PointF(boundaries[i].x, boundaries[i].y);
            result[i] = pointF;
        }
        return result;
    }

    private static BufferedImage createImageFromBytes(byte[] imageData) {
        ByteArrayInputStream bais = new ByteArrayInputStream(imageData);
        try {
            return ImageIO.read(bais);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static Point[] boundaries(MarvinAttributes attr) {
        Point upLeft = new Point(-1, -1);
        Point upRight = new Point(-1, -1);
        Point bottomLeft = new Point(-1, -1);
        Point bottomRight = new Point(-1, -1);
        double ulDistance = 9999, blDistance = 9999, urDistance = 9999, brDistance = 9999;
        double tempDistance = -1;
        int[][] cornernessMap = (int[][]) attr.get("cornernessMap");

        for (int x = 0; x < cornernessMap.length; x++) {
            for (int y = 0; y < cornernessMap[0].length; y++) {
                if (cornernessMap[x][y] > 0) {
                    if ((tempDistance = Point.distance(x, y, 0, 0)) < ulDistance) {
                        upLeft.x = x;
                        upLeft.y = y;
                        ulDistance = tempDistance;
                    }
                    if ((tempDistance = Point.distance(x, y, cornernessMap.length, 0)) < urDistance) {
                        upRight.x = x;
                        upRight.y = y;
                        urDistance = tempDistance;
                    }
                    if ((tempDistance = Point.distance(x, y, 0, cornernessMap[0].length)) < blDistance) {
                        bottomLeft.x = x;
                        bottomLeft.y = y;
                        blDistance = tempDistance;
                    }
                    if ((tempDistance = Point.distance(x, y, cornernessMap.length, cornernessMap[0].length)) < brDistance) {
                        bottomRight.x = x;
                        bottomRight.y = y;
                        brDistance = tempDistance;
                    }
                }
            }
        }
        return new Point[]{upLeft, upRight, bottomRight, bottomLeft};
    }

}
