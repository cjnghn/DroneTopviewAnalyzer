package domain;


import lombok.Builder;
import lombok.Value;

@Value  // 불변 클래스를 쉽게 생성. (모든 필드를 final private 으로)
@Builder
public class TrajectoryIntersection {
    GeoPoint intersectionPoint;      // 교차점의 위치 (위도, 경도, 고도)
    double timestamp;                // 교차가 발생한 시간 (elapsed time)
    GeoreferencedObject object1;     // 첫 번째 궤적의 객체
    GeoreferencedObject object2;     // 두 번째 궤적의 객체

    /**
     * 교차하는 두 객체의 ID를 반환
     */
    public String[] getIntersectingIds() {
        return new String[]{
            object1.getTrackedObject().getTrackingId(),
            object2.getTrackedObject().getTrackingId()
        };
    }

    /**
     * 교차하는 두 객체의 클래스를 반환
     */
    public String[] getIntersectingClasses() {
        return new String[]{
            object1.getTrackedObject().getClassId(),
            object2.getTrackedObject().getClassId()
        };
    }
}
