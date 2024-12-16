package videosegment;

import flight.FlightRecord;

import java.util.ArrayList;
import java.util.List;

public class VideoSegmentExtractor {
    public List<VideoSegment> extractSegments(List<FlightRecord> records) {
        if (records == null || records.isEmpty()) {
            return List.of();
        }

        List<VideoSegment> segments = new ArrayList<>();
        List<FlightRecord> currentSegment = null;

        for (FlightRecord record : records) {
            if (record.isVideo()) {
                if (currentSegment == null) {
                    currentSegment = new ArrayList<>();
                }
                currentSegment.add(record);
            } else if (currentSegment != null) {
                addSegment(segments, currentSegment);
                currentSegment = null;
            }
        }

        if (currentSegment != null && !currentSegment.isEmpty()) {
            addSegment(segments, currentSegment);
        }

        return segments;
    }

    private void addSegment(List<VideoSegment> segments, List<FlightRecord> records) {
        segments.add(new VideoSegment(
                records.get(0).dateTime(),
                records.get(records.size() - 1).dateTime(),
                records
        ));
    }
}
