package processor;

import domain.FlightRecord;
import exception.ProcessingException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class FlightRecordInterpolatorTest {
    private FlightRecordInterpolator interpolator;
    private LocalDateTime baseTime;

    @BeforeEach
    void setUp() {
        interpolator = new FlightRecordInterpolator();
        baseTime = LocalDateTime.of(2024, 1, 1, 10, 0);
    }

    @Test
    void interpolateByFps_ShouldInterpolateCorrectly() {
        // given
        List<FlightRecord> records = Arrays.asList(
            createFlightRecord(0.0, 37.0, 127.0, 100.0, 90.0),
            createFlightRecord(1.0, 37.1, 127.1, 110.0, 100.0)
        );

        // when
        List<FlightRecord> interpolated = interpolator.interpolateByFps(records, 10.0); // 10fps = 0.1초 간격

        // then
        assertEquals(11, interpolated.size()); // 0.0 ~ 1.0초, 0.1초 간격 = 11개 포인트

        // 첫 포인트 검증
        assertEquals(0.0, interpolated.get(0).getElapsedTime(), 0.001);
        assertEquals(37.0, interpolated.get(0).getLatitude(), 0.001);

        // 중간 포인트 검증 (0.5초)
        assertEquals(0.5, interpolated.get(5).getElapsedTime(), 0.001);
        assertEquals(37.05, interpolated.get(5).getLatitude(), 0.001);
        assertEquals(127.05, interpolated.get(5).getLongitude(), 0.001);
        assertEquals(105.0, interpolated.get(5).getAscent(), 0.001);
        assertEquals(95.0, interpolated.get(5).getCompassHeading(), 0.001);

        // 마지막 포인트 검증
        assertEquals(1.0, interpolated.get(10).getElapsedTime(), 0.001);
        assertEquals(37.1, interpolated.get(10).getLatitude(), 0.001);
    }

    @Test
    void interpolateHeading_ShouldHandleCrossingZero() {
        // given
        List<FlightRecord> records = Arrays.asList(
            createFlightRecord(0.0, 37.0, 127.0, 100.0, 350.0),
            createFlightRecord(1.0, 37.0, 127.0, 100.0, 10.0)
        );

        // when
        List<FlightRecord> result = interpolator.interpolateByFps(records, 4.0); // 4fps = 0.25초 간격

        // then
        assertEquals(5, result.size());
        assertEquals(350.0, result.get(0).getCompassHeading(), 0.001);
        assertEquals(0.0, result.get(2).getCompassHeading(), 0.1); // 중간점은 0도에 가까워야 함
        assertEquals(10.0, result.get(4).getCompassHeading(), 0.001);
    }

    @Test
    void interpolateDateTime_ShouldInterpolateCorrectly() {
        // given
        List<FlightRecord> records = Arrays.asList(
            createFlightRecord(0.0, 37.0, 127.0, 100.0, 90.0),
            createFlightRecord(2.0, 37.0, 127.0, 100.0, 90.0)
        );

        // when
        List<FlightRecord> result = interpolator.interpolateByFps(records, 1.0); // 1fps = 1초 간격

        // then
        assertEquals(3, result.size());
        assertEquals(baseTime, result.get(0).getDatetime());
        assertEquals(baseTime.plusSeconds(1), result.get(1).getDatetime());
        assertEquals(baseTime.plusSeconds(2), result.get(2).getDatetime());
    }

    @Test
    void interpolateByFps_ShouldThrowException_WhenInputIsEmpty() {
        assertThrows(ProcessingException.class, () ->
            interpolator.interpolateByFps(Collections.emptyList(), 30.0)
        );
    }

    @Test
    void interpolateByFps_ShouldThrowException_WhenSingleRecord() {
        // given
        List<FlightRecord> records = Collections.singletonList(
            createFlightRecord(0.0, 37.0, 127.0, 100.0, 90.0)
        );

        // when & then
        assertThrows(ProcessingException.class, () ->
            interpolator.interpolateByFps(records, 30.0)
        );
    }

    @Test
    void interpolateAtTime_ShouldReturnBoundaryValues_WhenTimeOutOfRange() {
        // given
        List<FlightRecord> records = Arrays.asList(
            createFlightRecord(1.0, 37.0, 127.0, 100.0, 90.0),
            createFlightRecord(2.0, 37.1, 127.1, 110.0, 100.0)
        );

        // when
        FlightRecord beforeRange = interpolator.interpolateAtTime(records, 0.5);
        FlightRecord afterRange = interpolator.interpolateAtTime(records, 2.5);

        // then
        assertEquals(records.get(0).getLatitude(), beforeRange.getLatitude(), 0.001);
        assertEquals(records.get(1).getLatitude(), afterRange.getLatitude(), 0.001);
    }

    private FlightRecord createFlightRecord(double elapsedTime, double lat, double lon, double ascent, double heading) {
        return FlightRecord.builder()
            .elapsedTime(elapsedTime)
            .datetime(baseTime.plusSeconds((long)elapsedTime))
            .latitude(lat)
            .longitude(lon)
            .ascent(ascent)
            .compassHeading(heading)
            .isVideo(true)
            .build();
    }
}