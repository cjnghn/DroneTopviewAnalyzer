package domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor // 파라미터 없는 기본 생성자
@AllArgsConstructor // 모든 필드를 파라미터로 받는 생성자
public class FlightRecord {
    private Double elapsedTime;
    private LocalDateTime datetime;
    private Double latitude;
    private Double longitude;
    private Double ascent;
    private Double compassHeading;
    private Boolean isVideo;
}
