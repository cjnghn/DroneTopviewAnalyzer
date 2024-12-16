package videosegment;

import georeference.GeoPoint;
import tracking.model.BoundingBox;
import java.util.List;

public record GeoreferencedDetection(
    int trackId,
    double confidence,
    int classId,
    GeoPoint center,
    List<GeoPoint> corners,
    BoundingBox originalBbox
) {
    public GeoreferencedDetection {
        corners = List.copyOf(corners);  // 불변 리스트로 만듦
    }
}