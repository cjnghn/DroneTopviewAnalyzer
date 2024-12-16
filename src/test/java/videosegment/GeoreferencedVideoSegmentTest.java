package videosegment;

import flight.FlightRecord;
import org.junit.jupiter.api.Test;
import tracking.model.BoundingBox;
import tracking.model.Detection;
import tracking.model.Frame;
import tracking.model.VideoMetadata;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class GeoreferencedVideoSegmentTest {

    @Test
    void shouldGeoReferenceDetections() {
        // Given
        VideoMetadata videoMetadata = new VideoMetadata("test", 2688, 1512, 30.0, 300);
        LocalDateTime startTime = LocalDateTime.now();

        var records = List.of(
            new FlightRecord(0, startTime, 37.5, 127.1, 100.0, 90.0, true),
            new FlightRecord(33, startTime.plus(Duration.ofMillis(33)), 37.5001, 127.1001, 100.0, 90.0, true)
        );

        Detection detection = new Detection(
            1,
            new BoundingBox(100, 100, 200, 200),
            0.95,
            1
        );

        Frame frame = new Frame(0, List.of(detection));

        VideoSegment segment = new VideoSegment(startTime, startTime.plus(Duration.ofMillis(33)), records);

        // When
        var geoSegment = new GeoreferencedVideoSegment(
            segment,
            List.of(frame),
            records,
            videoMetadata,
            83.0
        );

        var geoDetections = geoSegment.getGeoReferencedDetections(0);

        // Then
        assertEquals(1, geoDetections.size());
        var geoDetection = geoDetections.get(0);

        // Detection의 속성이 보존되었는지 확인
        assertEquals(1, geoDetection.trackId());
        assertEquals(0.95, geoDetection.confidence(), 0.001);
        assertEquals(1, geoDetection.classId());

        // 모서리 좌표가 모두 계산되었는지 확인
        assertEquals(4, geoDetection.corners().size());

        // 중심 좌표가 비행 기록의 GPS 좌표 범위 내에 있는지 확인
        System.out.println(geoDetection.center());
        assertTrue(geoDetection.center().latitude() >= 37.5);
        assertTrue(geoDetection.center().latitude() <= 37.5001);
        assertTrue(geoDetection.center().longitude() >= 127.1);
        assertTrue(geoDetection.center().longitude() <= 127.1001);
    }
}