package tracking.model;

import java.util.List;

public record Frame(
    int index,
    List<Detection> detections
) {
    public Frame {
        detections = List.copyOf(detections); // 불변 리스트로 만듦
    }
}