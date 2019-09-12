package ocrme_backend.servlets.smart_crop;

import ocrme_backend.utils.FileProvider;
import org.junit.Assert;
import org.junit.Test;

public class ContourEdgePointsProcessorTest {

    @Test
    public void process() throws Exception {
        PointF[] result = ContourEdgePointsProcessor.process(
                FileProvider.uploadTestImageForGsUri("big_image.jpg"));
        Assert.assertTrue(result.length > 0);
    }
}