package domain;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class VideoSegment {
    private final double startElapseTime;
    private final double endElapseTime;
    private final List<FlightRecord> flightRecords;
}