package calculator;

import domain.FlightRecord;
import domain.GeoPoint;
import domain.GeoreferencedObject;
import domain.TrackedObject;
import lombok.Builder;

import java.util.List;
import java.util.ArrayList;

/**
 * 드론 영상의 픽셀 좌표를 실제 GPS 좌표로 변환하는 계산기
 * 드론의 위치, 고도, 방향 및 카메라 FOV를 기반으로 계산
 */
public class GeoreferencingCalculator {
    private static final double EARTH_RADIUS = 6371000.0; // meters
    private final int videoWidth;
    private final int videoHeight;
    private final double fovRad;        // Field of View in radians
    private final double fovTan;        // tan(FOV) - 고도에 곱하면 대각선 거리가 됨
    private final double angleOffset;    // 화면 비율에 따른 대각선 각도
    private final double diagonalPixels; // 화면 대각선 픽셀 수

    @Builder
    public GeoreferencingCalculator(int videoWidth, int videoHeight, double fovDegrees) {
        this.videoWidth = videoWidth;
        this.videoHeight = videoHeight;
        this.fovRad = Math.toRadians(fovDegrees);
        this.fovTan = Math.tan(fovRad);

        // 화면의 가로세로 비율에 따른 대각선 각도 계산
        this.angleOffset = Math.toDegrees(Math.atan((double) videoHeight / videoWidth));
        this.diagonalPixels = Math.sqrt(videoWidth * videoWidth + videoHeight * videoHeight);
    }

    /**
     * 드론 카메라 프레임의 4개 모서리 GPS 좌표를 계산
     * @return 좌상단, 우상단, 우하단, 좌하단 순서의 좌표 목록
     */
    public List<GeoPoint> getFrameCorners(FlightRecord flightRecord) {
        GeoPoint dronePosition = createDroneGeoPoint(flightRecord);
        return getFrameCorners(
            dronePosition,
            flightRecord.getAscent(),
            flightRecord.getCompassHeading()
        );
    }

    private List<GeoPoint> getFrameCorners(GeoPoint dronePosition, double altitude, double headingDegrees) {
        // 고도와 FOV로부터 실제 거리 계산
        double diagonalDistance = altitude * fovTan;  // 대각선 전체 거리
        double distance = diagonalDistance / 2;       // 중심에서 모서리까지 거리

        // 각 모서리의 방향 계산 (드론 진행방향 기준)
        double[] bearings = {
            normalizeBearing(headingDegrees - angleOffset),      // 좌상단
            normalizeBearing(headingDegrees + angleOffset),      // 우상단
            normalizeBearing(headingDegrees + 180 - angleOffset),// 우하단
            normalizeBearing(headingDegrees + 180 + angleOffset) // 좌하단
        };

        // 각 방향에 대해 GPS 좌표 계산
        List<GeoPoint> corners = new ArrayList<>();
        for (double cornerBearing : bearings) {
            corners.add(calculateDestination(dronePosition, distance, cornerBearing));
        }
        return corners;
    }

    /**
     * 영상 내 픽셀 좌표를 실제 GPS 좌표로 변환
     */
    public GeoPoint pixelToGps(double pixelX, double pixelY, GeoPoint dronePosition,
                               double altitude, double headingDegrees) {
        // 1. 화면 중앙을 (0,0)으로 하는 좌표계로 변환
        // 스크린 좌표계는 좌상단이 (0,0)이므로, Y축은 반전시켜야 함
        double normalizedY = (videoHeight / 2.0 - pixelY) / (videoHeight / 2.0); // 위쪽이 양수
        double normalizedX = (pixelX - videoWidth / 2.0) / (videoWidth / 2.0);   // 오른쪽이 양수

        // 2. 중심으로부터의 각도 계산
        double angle;
        if (normalizedX != 0) {
            // atan(y/x)로 각도 계산
            angle = Math.toDegrees(Math.atan(normalizedY / normalizedX));
            if (normalizedX >= 0) {  // 오른쪽 반면에 있을 경우 180도 회전
                angle += 180;
            }
        } else {
            // x가 0일 때는 90도 또는 -90도
            angle = normalizedY > 0 ? 90 : -90;
        }

        // 3. 중심으로부터의 거리 계산
        double distanceFromCenter = Math.hypot(normalizedX, normalizedY);

        // 4. 실제 지상 거리 계산
        double diagonalDistance = altitude * fovTan;
        double distance = distanceFromCenter * diagonalDistance;

        // 5. 드론 진행방향을 고려한 최종 방향 계산
        double bearing = normalizeBearing(headingDegrees + angle);

        // 6. 최종 GPS 좌표 계산
        return calculateDestination(dronePosition, distance, bearing);
    }

    /**
     * 특정 위치에서 거리와 방향을 기준으로 도착 지점의 GPS 좌표를 계산
     */
    private GeoPoint calculateDestination(GeoPoint start, double distance, double bearing) {
        double angularDistance = distance / EARTH_RADIUS;
        double bearingRad = Math.toRadians(bearing);
        double latRad = Math.toRadians(start.getLatitude());
        double lonRad = Math.toRadians(start.getLongitude());

        // Haversine formula
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
            Math.toDegrees(newLonRad),
            start.getAltitude()
        );
    }

    private GeoPoint createDroneGeoPoint(FlightRecord record) {
        return new GeoPoint(
            record.getLatitude(),
            record.getLongitude(),
            record.getAscent()
        );
    }

    /**
     * 방향각을 0-360도 범위로 정규화
     */
    private double normalizeBearing(double bearing) {
        return (bearing % 360 + 360) % 360;
    }

    /**
     * 객체의 위치를 지리참조 정보로 변환
     */
    public GeoreferencedObject georeference(TrackedObject trackedObject, FlightRecord flightRecord) {
        GeoPoint objectLocation = pixelToGps(
            trackedObject.getBoundingBox().getCenterX(),
            trackedObject.getBoundingBox().getCenterY(),
            createDroneGeoPoint(flightRecord),
            flightRecord.getAscent(),
            flightRecord.getCompassHeading()
        );

        return GeoreferencedObject.builder()
            .trackedObject(trackedObject)
            .location(objectLocation)
            .build();
    }
}