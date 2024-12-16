package flight;

import java.time.LocalDateTime;

public record FlightRecord(
    long elapsedTimeMillis,
    LocalDateTime dateTime,
    double latitude,
    double longitude,
    double altitudeFeet,  // 추가된 고도 정보
    double compassHeading,
    boolean isVideo
) {
    private static final double FEET_TO_METERS = 0.3048;

    public FlightRecord {
        if (dateTime == null) {
            throw new IllegalArgumentException("DateTime cannot be null");
        }
    }

    public double getAltitudeMeters() {
        return altitudeFeet * FEET_TO_METERS;
    }
}
