package reader.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class TrackingResultDTO {
    private ModelConfig model;
    private TrackerConfig tracker;
    private VideoMetadata video;

    @JsonProperty("tracking_results")
    private List<FrameDTO> trackingResults;
}

