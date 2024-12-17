package reader.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ModelConfig {
    private String name;

    @JsonProperty("confidence_threshold")
    private double confidenceThreshold;

    private boolean nms;
}
