package domain;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class GeoreferencedObject {
    TrackedObject trackedObject; // 트래킹된 객체 정보
    GeoPoint location;  // 지리참조된 위치

    /**
     * 다른 지리참조 객체와의 거리를 계산
     */
    public double distanceTo(GeoreferencedObject other) {
        return this.location.distanceTo(other.location);
    }
}
