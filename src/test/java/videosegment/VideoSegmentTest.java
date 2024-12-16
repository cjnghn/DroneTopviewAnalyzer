
package videosegment;

import flight.FlightRecord;
import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class VideoSegmentTest {

    @Test
    void shouldCreateValidVideoSegment() {
        LocalDateTime start = LocalDateTime.now();
        LocalDateTime end = start.plusSeconds(10);
        List<FlightRecord> records = List.of(
            new FlightRecord(0L, start, 37.5, 127.1, 45.0, true),
            new FlightRecord(1000L, end, 37.6, 127.2, 46.0, true)
        );

        VideoSegment segment = new VideoSegment(start, end, records);

        assertEquals(start, segment.getStartTime());
        assertEquals(end, segment.getEndTime());
        assertEquals(2, segment.getDataPointCount());
    }

    @Test
    void shouldThrowExceptionWhenEndTimeIsBeforeStartTime() {
        LocalDateTime start = LocalDateTime.now();
        LocalDateTime end = start.minusSeconds(10);
        List<FlightRecord> records = List.of(
            new FlightRecord(0L, start, 37.5, 127.1, 45.0, true)
        );

        assertThrows(IllegalArgumentException.class, () ->
            new VideoSegment(start, end, records)
        );
    }
}