package georeference;

import java.util.List;
import java.util.ArrayList;

public class GeoreferencingCalculator {
    private static final double EARTH_RADIUS = 6371000.0; // meters

    private final int videoWidth;
    private final int videoHeight;
    private final double fovRad;
    private final double fovTan;
    private final double angleOffset;

    public static class Builder {
        private final int videoWidth;
        private final int videoHeight;
        private final double fovDegrees;

        public Builder(int videoWidth, int videoHeight, double fovDegrees) {
            this.videoWidth = videoWidth;
            this.videoHeight = videoHeight;
            this.fovDegrees = fovDegrees;
        }

        public GeoreferencingCalculator build() {
            return new GeoreferencingCalculator(this);
        }
    }

    private GeoreferencingCalculator(Builder builder) {
        this.videoWidth = builder.videoWidth;
        this.videoHeight = builder.videoHeight;
        this.fovRad = Math.toRadians(builder.fovDegrees);
        this.fovTan = Math.tan(fovRad);
        this.angleOffset = Math.toDegrees(Math.atan((double) videoHeight / videoWidth));
    }

    public List<GeoPoint> getFrameCorners(GeoPoint dronePosition, double altitude, double headingDegrees) {
        double diagonalDistance = altitude * fovTan;
        double distance = diagonalDistance / 2;
        double bearing = (headingDegrees - 90) % 360;

        double[] bearings = {
            (bearing - angleOffset + 180) % 360 - 180,  // 좌상단
            (bearing + angleOffset + 180) % 360 - 180,  // 우상단
            (bearing - angleOffset) % 360 - 180,        // 우하단
            (bearing + angleOffset) % 360 - 180         // 좌하단
        };

        List<GeoPoint> corners = new ArrayList<>();
        for (double cornerBearing : bearings) {
            corners.add(calculateDestination(dronePosition, distance, cornerBearing));
        }

        return corners;
    }

    public GeoPoint pixelToGps(double pixelX, double pixelY, GeoPoint dronePosition,
                               double altitude, double headingDegrees) {
        // 중심점 기준으로 정규화
        double[] normalized = {
            pixelY - videoHeight / 2.0,
            pixelX - videoWidth / 2.0
        };

        // 중심점으로부터의 상대적 거리 계산
        double distanceFromCenter = Math.sqrt(
            Math.pow(videoWidth / 2.0 - pixelX, 2) +
            Math.pow(videoHeight / 2.0 - pixelY, 2)
        );
        double diagonalDistancePixels = Math.sqrt(videoWidth * videoWidth + videoHeight * videoHeight);

        // 실제 거리 계산
        double diagonalDistanceMeters = altitude * fovTan;
        double distance = (distanceFromCenter / diagonalDistancePixels) * diagonalDistanceMeters;

        // 각도 계산
        double angle = Math.toDegrees(Math.atan2(normalized[0], normalized[1] == 0 ? 0.000001 : normalized[1]));
        if (normalized[1] >= 0) angle += 180;

        // 최종 방향 계산
        double bearing = (headingDegrees - 90 + angle) % 360;

        return calculateDestination(dronePosition, distance, bearing);
    }

    private GeoPoint calculateDestination(GeoPoint start, double distance, double bearing) {
        double angularDistance = distance / EARTH_RADIUS;
        double bearingRad = Math.toRadians(bearing);
        double latRad = Math.toRadians(start.latitude());
        double lonRad = Math.toRadians(start.longitude());

        double newLatRad = Math.asin(
            Math.sin(latRad) * Math.cos(angularDistance) +
            Math.cos(latRad) * Math.sin(angularDistance) * Math.cos(bearingRad)
        );

        double newLonRad = lonRad + Math.atan2(
            Math.sin(bearingRad) * Math.sin(angularDistance) * Math.cos(latRad),
            Math.cos(angularDistance) - Math.sin(latRad) * Math.sin(newLatRad)
        );

        return new GeoPoint(
            Math.toDegrees(newLatRad),
            Math.toDegrees(newLonRad)
        );
    }
}
