package videosegment;

import flight.FlightRecord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class VideoSegmentExtractorTest {
    private VideoSegmentExtractor extractor;
    private LocalDateTime baseTime;

    @BeforeEach
    void setUp() {
        extractor = new VideoSegmentExtractor();
        baseTime = LocalDateTime.now();
    }

    @Test
    void shouldExtractSingleSegment() {
        List<FlightRecord> records = List.of(
            new FlightRecord(0L, baseTime, 37.5, 127.1, 45.0, true),
            new FlightRecord(1000L, baseTime.plusSeconds(1), 37.6, 127.2, 46.0, true),
            new FlightRecord(2000L, baseTime.plusSeconds(2), 37.7, 127.3, 47.0, true)
        );

        List<VideoSegment> segments = extractor.extractSegments(records);

        assertEquals(1, segments.size());
        assertEquals(3, segments.get(0).getDataPointCount());
    }

    @Test
    void shouldExtractMultipleSegments() {
        List<FlightRecord> records = List.of(
            new FlightRecord(0L, baseTime, 37.5, 127.1, 45.0, true),
            new FlightRecord(1000L, baseTime.plusSeconds(1), 37.6, 127.2, 46.0, true),
            new FlightRecord(2000L, baseTime.plusSeconds(2), 37.7, 127.3, 47.0, false),
            new FlightRecord(3000L, baseTime.plusSeconds(3), 37.8, 127.4, 48.0, true),
            new FlightRecord(4000L, baseTime.plusSeconds(4), 37.9, 127.5, 49.0, true)
        );

        List<VideoSegment> segments = extractor.extractSegments(records);

        assertEquals(2, segments.size());
        assertEquals(2, segments.get(0).getDataPointCount());
        assertEquals(2, segments.get(1).getDataPointCount());
    }

    @Test
    void shouldReturnEmptyListForNullInput() {
        List<VideoSegment> segments = extractor.extractSegments(null);
        assertTrue(segments.isEmpty());
    }

    @Test
    void shouldHandleEmptyList() {
        List<VideoSegment> segments = extractor.extractSegments(List.of());
        assertTrue(segments.isEmpty());
    }
}
