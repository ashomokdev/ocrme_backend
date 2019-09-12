package ocrme_backend.servlets.smart_crop;

import java.io.Serializable;
import java.util.Arrays;

/**
 * Created by iuliia on 12/18/17.
 */
public class ContourEdgePointsResponse implements Serializable {
    private PointF[] points;
    private Status status;

    public PointF[] getPoints() {
        return points;
    }

    public Status getStatus() {
        return status;
    }

    public void setPoints(PointF[] points) {
        this.points = points;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "ContourEdgePointsResponse{" +
                "points=" + Arrays.toString(points) +
                ", status=" + status +
                '}';
    }

    public enum Status {
        OK,
        UNKNOWN_ERROR
    }
}
