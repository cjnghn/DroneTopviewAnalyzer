package tracking.parser;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import tracking.TrackingResults;
import tracking.model.Frame;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class TrackingResultsParserTest {

    @Test
    void shouldParseValidTrackingResults(@TempDir Path tempDir) throws Exception {
        // Given
        Path jsonFile = tempDir.resolve("test.json");
        String jsonContent = """
            {
              "model": {
                "name": "yolov11s_v4_2560_b8_e60",
                "confidence_threshold": 0.3,
                "nms": true
              },
              "tracker": {
                "name": "bytetrack"
              },
              "video": {
                "name": "DJI_0268",
                "width": 2688,
                "height": 1512,
                "fps": 29.97002997002997,
                "total_frames": 10535
              },
              "tracking_results": [
                {
                  "i": 0,
                  "res": [
                    {
                      "tid": 1,
                      "bbox": [100.0, 200.0, 150.0, 250.0],
                      "conf": 0.968,
                      "cid": 1
                    }
                  ]
                }
              ]
            }
        """;
        Files.writeString(jsonFile, jsonContent);

        // When
        TrackingResultsParser parser = new TrackingResultsParser();
        TrackingResults results = parser.parse(jsonFile);

        // Then
        assertEquals("yolov11s_v4_2560_b8_e60", results.model().name());
        assertEquals("bytetrack", results.tracker().name());

        Frame frame = results.frames().get(0);
        var detection = frame.detections().get(0);
        var bbox = detection.bbox();

        assertEquals(100.0, bbox.minX());
        assertEquals(200.0, bbox.minY());
        assertEquals(150.0, bbox.maxX());
        assertEquals(250.0, bbox.maxY());
        assertEquals(50.0, bbox.width());
        assertEquals(50.0, bbox.height());
        assertEquals(125.0, bbox.centerX());
        assertEquals(225.0, bbox.centerY());
    }
}