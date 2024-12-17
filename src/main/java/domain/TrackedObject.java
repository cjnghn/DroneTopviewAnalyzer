package domain;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TrackedObject {
    String trackingId;      // 객체의 트래킹 ID
    String classId;         // 객체의 클래스 ID (차량, 사람 등)
    double confidence;      // 탐지 신뢰도
    BoundingBox boundingBox;
    int frameNumber;        // 프레임 번호
    double timestamp;       // 프레임의 타임스탬프
}