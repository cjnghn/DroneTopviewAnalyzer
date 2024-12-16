// src/test/java/interpolation/FlightRecordInterpolatorTest.java
package interpolation;

import flight.FlightRecord;
import tracking.model.VideoMetadata;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class FlightRecordInterpolatorTest {
    final double tolerance = 0.01d;

    private VideoMetadata videoMetadata;
    private FlightRecordInterpolator interpolator;

    @BeforeEach
    void setUp() {
        videoMetadata = new VideoMetadata(
            "test_video",
            1920,
            1080,
            30.0,  // 30 FPS
            300    // 10초 분량
        );
        interpolator = new FlightRecordInterpolator(videoMetadata);
    }

    @Test
    void shouldInterpolateSimpleRecords() {
        // Given
        LocalDateTime startTime = LocalDateTime.of(2024, 1, 1, 10, 0, 0);
        List<FlightRecord> records = List.of(
            new FlightRecord(0, startTime, 37.0, 127.0, 45.0, 0,true),
            new FlightRecord(1000, startTime.plusSeconds(1), 37.1, 127.1, 46.0, 90,true)
        );

        // When
        List<FlightRecord> interpolated = interpolator.interpolateToFrames(records);

        // Then
        assertEquals(30, interpolated.size());  // 1초 분량의 30fps = 30 프레임

        // 첫 프레임과 마지막 프레임 확인
        assertEquals(37.0, interpolated.get(0).latitude(), tolerance);
        assertEquals(37.1, interpolated.get(29).latitude(), tolerance);

        // 중간 프레임 확인 (15번째 프레임 = 0.5초 지점)
        FlightRecord midFrame = interpolated.get(14);
        assertEquals(37.05, midFrame.latitude(), tolerance);
        assertEquals(127.05, midFrame.longitude(), tolerance);
        assertEquals(45.5, midFrame.compassHeading(), tolerance);
    }

    @Test
    void shouldHandleNonLinearTimeIntervals() {
        // Given
        LocalDateTime startTime = LocalDateTime.of(2024, 1, 1, 10, 0, 0);
        List<FlightRecord> records = List.of(
            new FlightRecord(0, startTime, 37.0, 127.0, 45.0, 0,true),
            new FlightRecord(500, startTime.plus(Duration.ofMillis(500)), 37.05, 127.05, 45.5, 0,true),
            new FlightRecord(1000, startTime.plusSeconds(1), 37.1, 127.1, 46.0, 0,true)
        );

        // When
        List<FlightRecord> interpolated = interpolator.interpolateToFrames(records);

        // Then
        assertEquals(30, interpolated.size());
        assertTrue(interpolated.stream().allMatch(FlightRecord::isVideo));
    }

    @Test
    void shouldThrowExceptionForInsufficientRecords() {
        // Given
        LocalDateTime startTime = LocalDateTime.of(2024, 1, 1, 10, 0, 0);
        List<FlightRecord> records = List.of(
            new FlightRecord(0, startTime, 37.0, 127.0, 45.0, 0,true)
        );

        // Then
        assertThrows(IllegalArgumentException.class, () ->
            interpolator.interpolateToFrames(records)
        );
    }
}