package ocrme_backend.servlets.smart_crop;

import java.util.logging.Logger;

import static java.util.logging.Level.INFO;
import static java.util.logging.Level.WARNING;

/**
 * Created by iuliia on 12/18/17.
 */
class ContourEdgePointsRequestsManager {

    private final Logger logger = Logger.getLogger(ContourEdgePointsRequestsManager.class.getName());

    ContourEdgePointsResponse processForPost(String gcsImageUri) {
        ContourEdgePointsResponse response = new ContourEdgePointsResponse();
        try {
            PointF[] points = ContourEdgePointsProcessor.process(gcsImageUri);
            response.setPoints(points);
            response.setStatus(ContourEdgePointsResponse.Status.OK);

        } catch (Exception e) {
            e.printStackTrace();
            response.setStatus(ContourEdgePointsResponse.Status.UNKNOWN_ERROR);
            logger.log(WARNING, e.getMessage());
        }
        logger.log(INFO, "Response: " + response.toString());
        return response;
    }
}
