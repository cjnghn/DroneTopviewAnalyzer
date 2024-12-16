package flight;

import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class FlightRecordTest {

    @Test
    void shouldCreateValidFlightRecord() {
        LocalDateTime now = LocalDateTime.now();
        FlightRecord record = new FlightRecord(1000L, now, 37.5, 127.1, 45.0, true);

        assertEquals(1000L, record.elapsedTimeMillis());
        assertEquals(now, record.dateTime());
        assertEquals(37.5, record.latitude());
        assertEquals(127.1, record.longitude());
        assertEquals(45.0, record.compassHeading());
        assertTrue(record.isVideo());
    }

    @Test
    void shouldThrowExceptionWhenDateTimeIsNull() {
        assertThrows(IllegalArgumentException.class, () ->
                new FlightRecord(1000L, null, 37.5, 127.1, 45.0, true)
        );
    }
}