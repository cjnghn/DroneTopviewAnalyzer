package georeference;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class GeoreferencingCalculatorTest {
    private GeoreferencingCalculator calculator;
    private static final double DELTA = 0.0001d;

    @BeforeEach
    void setup() {
        calculator = new GeoreferencingCalculator.Builder(2688, 1512, 83.0).build();
    }

    @Test
    void shouldCalculateFrameCorners() {
        // Given
        GeoPoint dronePosition = new GeoPoint(37.5, 127.1);
        double altitude = 100.0;
        double heading = 90.0;

        // When
        List<GeoPoint> corners = calculator.getFrameCorners(dronePosition, altitude, heading);

        // Then
        assertEquals(4, corners.size());
        // 코너들이 시계방향으로 정렬되어 있는지 확인
        assertTrue(corners.get(0).latitude() > dronePosition.latitude());
        assertTrue(corners.get(2).latitude() < dronePosition.latitude());
    }

    @Test
    void shouldConvertPixelToGps() {
        // Given
        GeoPoint dronePosition = new GeoPoint(37.5, 127.1);
        double altitude = 100.0;
        double heading = 90.0;

        // When - 이미지 중심점에서의 GPS 좌표 계산
        GeoPoint centerGps = calculator.pixelToGps(
            2688.0 / 2, 1512.0 / 2,
            dronePosition, altitude, heading
        );

        // Then
        // 중심점이므로 드론 위치와 가까워야 함
        assertEquals(dronePosition.latitude(), centerGps.latitude(), 0.0001);
        assertEquals(dronePosition.longitude(), centerGps.longitude(), 0.0001);
    }

}