package processor;

import domain.FlightRecord;
import domain.VideoSegment;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class VideoSegmentProcessorTest {
    private VideoSegmentProcessor processor;
    private LocalDateTime baseTime;

    @BeforeEach
    void setUp() {
        processor = new VideoSegmentProcessor();
        baseTime = LocalDateTime.of(2024, 1, 1, 10, 0);
    }


    @Test
    void testExtractSegments() {
        // given
        List<FlightRecord> logs = Arrays.asList(
            createFlightRecord(1.0, false),
            createFlightRecord(2.0, true),
            createFlightRecord(3.0, true),
            createFlightRecord(4.0, true),
            createFlightRecord(5.0, false),
            createFlightRecord(6.0, true),
            createFlightRecord(7.0, true)
        );

        // when
        List<VideoSegment> segments = processor.extractSegments(logs);

        // then
        assertEquals(2, segments.size());

        // 첫 번째 세그먼트 검증
        VideoSegment firstSegment = segments.get(0);
        assertEquals(2.0, firstSegment.getStartElapseTime());
        assertEquals(4.0, firstSegment.getEndElapseTime());
        assertEquals(3, firstSegment.getFlightRecords().size());

        // 두 번째 세그먼트 검증
        VideoSegment secondSegment = segments.get(1);
        assertEquals(6.0, secondSegment.getStartElapseTime());
        assertEquals(7.0, secondSegment.getEndElapseTime());
        assertEquals(2, secondSegment.getFlightRecords().size());
    }


    private FlightRecord createFlightRecord(double elapsed, boolean isVideo) {
        return FlightRecord.builder()
            .elapsedTime(elapsed)
            .datetime(baseTime.plusSeconds((long)elapsed))
            .latitude(37.5)
            .longitude(127.5)
            .ascent(100.0)
            .compassHeading(180.0)
            .isVideo(isVideo)
            .build();
    }
}