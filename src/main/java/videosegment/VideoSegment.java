package videosegment;

import flight.FlightRecord;

import java.time.LocalDateTime;
import java.util.List;

public class VideoSegment {

    private final LocalDateTime startTime;
    private final LocalDateTime endTime;
    private final List<FlightRecord> flightRecords;

    public VideoSegment(
            LocalDateTime startTime,
            LocalDateTime endTime,
            List<FlightRecord> flightRecords) {
        if (startTime == null || endTime == null) {
            throw new IllegalArgumentException("Start time and end time cannot be null");
        }
        if (flightRecords == null || flightRecords.isEmpty()) {
            throw new IllegalArgumentException("Flight records cannot be null or empty");
        }
        if (endTime.isBefore(startTime)) {
            throw new IllegalArgumentException("End time cannot be before start time");
        }

        this.startTime = startTime;
        this.endTime = endTime;
        this.flightRecords = List.copyOf(flightRecords); // 불변 리스트로 만듦
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public List<FlightRecord> getFlightRecords() {
        return flightRecords;
    }

    public int getDataPointCount() {
        return flightRecords.size();
    }
}
