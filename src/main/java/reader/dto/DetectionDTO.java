package reader.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class DetectionDTO {
    private String tid;
    private double[] bbox;
    private double conf;
    private String cid;
}
