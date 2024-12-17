package calculator;

import domain.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class IntersectionCalculatorTest {
    private IntersectionCalculator calculator;

    @BeforeEach
    void setUp() {
        calculator = new IntersectionCalculator();
    }

    @Test
    void shouldFindIntersectionBetweenTwoCrossingTrajectories() {
        // given
        List<GeoreferencedObject> objects = List.of(
            // 첫 번째 궤적 (남->북)
            createGeoreferencedObject("1", "car", 0.0, 37.5, 127.0),
            createGeoreferencedObject("1", "car", 1.0, 37.6, 127.0),

            // 두 번째 궤적 (서->동)
            createGeoreferencedObject("2", "person", 0.0, 37.55, 126.95),
            createGeoreferencedObject("2", "person", 1.0, 37.55, 127.05)
        );

        // when
        List<TrajectoryIntersection> intersections = calculator.findIntersections(objects);

        // then
        assertEquals(1, intersections.size());
        TrajectoryIntersection intersection = intersections.get(0);
        assertEquals(37.55, intersection.getIntersectionPoint().getLatitude(), 0.001);
        assertEquals(127.0, intersection.getIntersectionPoint().getLongitude(), 0.001);
        assertEquals(0.5, intersection.getTimestamp(), 0.1);
    }

    @Test
    void shouldNotFindIntersectionBetweenParallelTrajectories() {
        // given
        List<GeoreferencedObject> objects = Arrays.asList(
            // 첫 번째 궤적 (남->북)
            createGeoreferencedObject("1", "car", 0.0, 37.5, 127.0),
            createGeoreferencedObject("1", "car", 1.0, 37.6, 127.0),

            // 두 번째 궤적 (남->북, 다른 경도)
            createGeoreferencedObject("2", "person", 0.0, 37.5, 127.1),
            createGeoreferencedObject("2", "person", 1.0, 37.6, 127.1)
        );

        // when
        List<TrajectoryIntersection> intersections = calculator.findIntersections(objects);

        // then
        assertTrue(intersections.isEmpty());
    }

    @Test
    void shouldHandleMultipleIntersections() {
        // given
        List<GeoreferencedObject> objects = Arrays.asList(
            // 첫 번째 궤적 (지그재그) /\
            createGeoreferencedObject("1", "car", 0.0, 37.4, 127.0),
            createGeoreferencedObject("1", "car", 1.0, 37.6, 128.0),
            createGeoreferencedObject("1", "car", 2.0, 37.4, 129.0),

            // 두 번째 궤적 (직선) ---
            createGeoreferencedObject("2", "person", 0.0, 37.5, 127.0),
            createGeoreferencedObject("2", "person", 2.0, 37.5, 129.0)
        );

        // when
        List<TrajectoryIntersection> intersections = calculator.findIntersections(objects);

        // then
        assertEquals(2, intersections.size());

        // 첫번째 교차 (시간 0.5)
        var firstIntersection = intersections.get(0);
        assertEquals(0.5d, firstIntersection.getTimestamp(), 1e-6);


        // 두번째 교차 (시간 1.5)
        var secondIntersection = intersections.get(1);
        assertEquals(secondIntersection.getTimestamp(), 1.5d,1e-6);
    }

    @Test
    void shouldHandleEmptyTrajectories() {
        // when
        List<TrajectoryIntersection> intersections = calculator.findIntersections(Arrays.asList());

        // then
        assertTrue(intersections.isEmpty());
    }

    @Test
    void shouldNotFindIntersectionInSameTrajectory() {
        // given
        List<GeoreferencedObject> objects = Arrays.asList(
            createGeoreferencedObject("1", "car", 0.0, 37.5, 127.0),
            createGeoreferencedObject("1", "car", 1.0, 37.6, 127.1),
            createGeoreferencedObject("1", "car", 2.0, 37.5, 127.0)
        );

        // when
        List<TrajectoryIntersection> intersections = calculator.findIntersections(objects);

        // then
        assertTrue(intersections.isEmpty());
    }

    private GeoreferencedObject createGeoreferencedObject(
            String trackingId,
            String classId,
            double timestamp,
            double latitude,
            double longitude) {

        return GeoreferencedObject.builder()
            .trackedObject(createTrackedObject(trackingId, classId, timestamp))
            .location(new GeoPoint(latitude, longitude, 100.0))  // 고도는 테스트용 고정값
            .build();
    }

    private TrackedObject createTrackedObject(String trackingId, String classId, double timestamp) {
        return TrackedObject.builder()
            .trackingId(trackingId)
            .classId(classId)
            .timestamp(timestamp)
            .confidence(0.9)  // 테스트용 고정값
            .boundingBox(new BoundingBox(0, 0, 100, 100))  // 테스트용 고정값
            .build();
    }
}