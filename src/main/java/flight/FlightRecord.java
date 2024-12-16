package flight;

import java.time.LocalDateTime;

public record FlightRecord(
    long elapsedTimeMillis,
    LocalDateTime dateTime,
    double latitude,
    double longitude,
    double compassHeading,
    boolean isVideo
) {
    public FlightRecord {
        if (dateTime == null) {
            throw new IllegalArgumentException("DateTime cannot be null");
        }
    }
}