package reader;

import com.fasterxml.jackson.databind.ObjectMapper;
import domain.TrackedObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class JsonTrackingReaderTest {

    private JsonTrackingReader reader;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        reader = new JsonTrackingReader();
    }

    @Test
    void testValidJsonParsing(@TempDir Path tempDir) throws Exception {
        // given
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
                "fps": 29.97,
                "total_frames": 10535
              },
              "tracking_results": [
                {
                  "i": 0,
                  "res": [
                    {
                      "tid": 1,
                      "bbox": [100, 200, 300, 400],
                      "conf": 0.968,
                      "cid": 1
                    }
                  ]
                }
              ]
            }
            """;
        Files.writeString(jsonFile, jsonContent);

        // when
        var results = reader.read(jsonFile.toString());

        // then
        assertEquals(1, results.getTrackedObjects().size());
        TrackedObject trackedObject = results.getTrackedObjects().get(0);
        assertEquals(1, trackedObject.getTrackingId());
        assertEquals(1, trackedObject.getClassId());
        assertEquals(0.968, trackedObject.getConfidence());
        assertEquals(0, trackedObject.getFrameNumber());

        // BoundingBox assertions
        assertEquals(100, trackedObject.getBoundingBox().getXMin());
        assertEquals(200, trackedObject.getBoundingBox().getYMin());
        assertEquals(300, trackedObject.getBoundingBox().getXMax());
        assertEquals(400, trackedObject.getBoundingBox().getYMax());
    }

    @Test
    void testInvalidJsonFormat(@TempDir Path tempDir) throws Exception {
        // given
        Path jsonFile = tempDir.resolve("invalid.json");
        String invalidJson = "{ invalid json }";
        Files.writeString(jsonFile, invalidJson);

        // when & then
        assertThrows(Exception.class, () -> reader.read(jsonFile.toString()));
    }

    @Test
    void testEmptyTrackingResults(@TempDir Path tempDir) throws Exception {
        // given
        Path jsonFile = tempDir.resolve("empty.json");
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
                "fps": 29.97,
                "total_frames": 10535
              },
              "tracking_results": []
            }
            """;
        Files.writeString(jsonFile, jsonContent);

        // when
        var results = reader.read(jsonFile.toString());

        // then
        assertTrue(results.getTrackedObjects().isEmpty());
    }
}