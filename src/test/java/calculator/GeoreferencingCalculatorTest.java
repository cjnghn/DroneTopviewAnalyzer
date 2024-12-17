package calculator;

import domain.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class GeoreferencingCalculatorTest {
    private GeoreferencingCalculator calculator;
    private static final double DELTA = 0.0001;

    private static final double EARTH_RADIUS = 6371000.0;

    private static final double BASE_LATITUDE = 37.5;    // 기준 위도
    private static final double BASE_LONGITUDE = 127.0;  // 기준 경도
    private static final double BASE_ALTITUDE = 30.0;    // 기준 고도
    private static final double EAST_HEADING = 90.0;     // 동쪽 방향

    @BeforeEach
    void setUp() {
        calculator = GeoreferencingCalculator.builder()
            .videoWidth(2688)
            .videoHeight(1512)
            .fovDegrees(53.0)
            .build();
    }

    @Test
    void shouldGeoreferenceObjectAtCenter() {
        TrackedObject trackedObject = createTrackedObject(2688.0 / 2, 1512.0 / 2);
        FlightRecord flightRecord = createFlightRecord(BASE_LATITUDE, BASE_LONGITUDE, BASE_ALTITUDE, EAST_HEADING);

        GeoreferencedObject result = calculator.georeference(trackedObject, flightRecord);

        assertEquals(BASE_LATITUDE, result.getLocation().getLatitude(), DELTA);
        assertEquals(BASE_LONGITUDE, result.getLocation().getLongitude(), DELTA);
        assertEquals(BASE_ALTITUDE, result.getLocation().getAltitude(), DELTA);
    }

    @Test
    void shouldCalculateFrameCornersAtDifferentAltitudes() {
        double[] altitudes = {30.0, 100.0, 500.0};
        for (double altitude : altitudes) {
            FlightRecord flightRecord = createFlightRecord(BASE_LATITUDE, BASE_LONGITUDE, altitude, EAST_HEADING);

            List<GeoPoint> corners = calculator.getFrameCorners(flightRecord);

            assertEquals(4, corners.size());
            validateCorners(corners, flightRecord);
        }
    }

    @Test
    void shouldCalculateFrameCornersAtDifferentHeadings() {
        double[] headings = {0.0, 90.0, 180.0, 270.0}; // 북, 동, 남, 서
        for (double heading : headings) {
            FlightRecord flightRecord = createFlightRecord(BASE_LATITUDE, BASE_LONGITUDE, BASE_ALTITUDE, heading);

            List<GeoPoint> corners = calculator.getFrameCorners(flightRecord);

            assertEquals(4, corners.size());
            validateCorners(corners, flightRecord);
        }
    }

    @Test
    void shouldGeoreferenceObjectAtTopLeftPixel() {
        TrackedObject trackedObject = createTrackedObject(0.0, 0.0); // 화면 좌측 상단
        FlightRecord flightRecord = createFlightRecord(BASE_LATITUDE, BASE_LONGITUDE, BASE_ALTITUDE, EAST_HEADING);

        GeoreferencedObject result = calculator.georeference(trackedObject, flightRecord);

        System.out.println("result: " + result);
        // result: GeoreferencedObject(..., location=GeoPoint(latitude=37.50008776444895, longitude=126.9998033333292, altitude=30.0))

        assertTrue(result.getLocation().getLatitude() > BASE_LATITUDE);
        assertTrue(result.getLocation().getLongitude() > BASE_LONGITUDE);
    }

    @Test
    void shouldGeoreferenceObjectAtBottomRightPixel() { // 남서
        TrackedObject trackedObject = createTrackedObject(2688.0, 1512.0); // 화면 우측 하단
        FlightRecord flightRecord = createFlightRecord(BASE_LATITUDE, BASE_LONGITUDE, BASE_ALTITUDE, EAST_HEADING);

        GeoreferencedObject result = calculator.georeference(trackedObject, flightRecord);

        assertTrue(result.getLocation().getLatitude() < BASE_LATITUDE);
        assertTrue(result.getLocation().getLongitude() < BASE_LONGITUDE);
    }

    @Test
    void shouldHandleZeroAltitude() {
        TrackedObject trackedObject = createTrackedObject(2688.0 / 2, 1512.0 / 2);
        FlightRecord flightRecord = createFlightRecord(BASE_LATITUDE, BASE_LONGITUDE, 0.0, EAST_HEADING);

        GeoreferencedObject result = calculator.georeference(trackedObject, flightRecord);

        assertEquals(BASE_LATITUDE, result.getLocation().getLatitude(), DELTA);
        assertEquals(BASE_LONGITUDE, result.getLocation().getLongitude(), DELTA);
        assertEquals(0.0, result.getLocation().getAltitude(), DELTA);
    }

    private void validateCorners(List<GeoPoint> corners, FlightRecord flightRecord) {
        GeoPoint topLeft = corners.get(0);
        GeoPoint topRight = corners.get(1);
        GeoPoint bottomRight = corners.get(2);
        GeoPoint bottomLeft = corners.get(3);

        System.out.println("TopLeft: " + topLeft);
        System.out.println("TopRight: " + topRight);
        System.out.println("BottomRight: " + bottomRight);
        System.out.println("BottomLeft: " + bottomLeft);

        double diagonal1 = calculateDistance(topLeft, bottomRight);
        double diagonal2 = calculateDistance(topRight, bottomLeft);
        assertEquals(diagonal1, diagonal2, DELTA);

        // 고도 검증
        for (GeoPoint corner : corners) {
            assertEquals(flightRecord.getAscent(), corner.getAltitude(), DELTA);
        }
    }

    private TrackedObject createTrackedObject(double centerX, double centerY) {
        return TrackedObject.builder()
            .trackingId("1")
            .classId("person")
            .confidence(0.9)
            .boundingBox(new BoundingBox(centerX - 50, centerY - 50, centerX + 50, centerY + 50))
            .frameNumber(0)
            .timestamp(0.0)
            .build();
    }

    private FlightRecord createFlightRecord(double latitude, double longitude, double altitude, double heading) {
        return FlightRecord.builder()
            .elapsedTime(0.0)
            .latitude(latitude)
            .longitude(longitude)
            .ascent(altitude)
            .compassHeading(heading)
            .isVideo(true)
            .build();
    }

    private double calculateDistance(GeoPoint p1, GeoPoint p2) {
        double lat1 = Math.toRadians(p1.getLatitude());
        double lon1 = Math.toRadians(p1.getLongitude());
        double lat2 = Math.toRadians(p2.getLatitude());
        double lon2 = Math.toRadians(p2.getLongitude());

        double dLat = lat2 - lat1;
        double dLon = lon2 - lon1;

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(lat1) * Math.cos(lat2) * Math.sin(dLon / 2) * Math.sin(dLon / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return EARTH_RADIUS * c;
    }
}
