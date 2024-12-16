package tracking;

import tracking.model.*;
import java.util.List;

public record TrackingResults(
    ModelConfig model,
    TrackerConfig tracker,
    VideoMetadata video,
    List<Frame> frames
) {
    public TrackingResults {
        frames = List.copyOf(frames);
    }
}