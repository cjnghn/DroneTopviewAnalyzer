package integration;

import flight.*;
import videosegment.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class VideoSegmentExtractionIntegrationTest {

    @Test
    void shouldExtractSegmentsFromCSVFile(@TempDir Path tempDir) throws Exception {
        // Given
        Path csvFile = tempDir.resolve("test.csv");
        String csvContent = """
            time(millisecond),datetime(utc),latitude,longitude,compass_heading(degrees),isVideo
            1000,2024-01-01 10:00:00,37.5,127.1,45.0,1
            2000,2024-01-01 10:00:01,37.6,127.2,46.0,1
            3000,2024-01-01 10:00:02,37.7,127.3,47.0,0
            4000,2024-01-01 10:00:03,37.8,127.4,48.0,1
            5000,2024-01-01 10:00:04,37.9,127.5,49.0,1
            """;
        Files.writeString(csvFile, csvContent);

        // When
        FlightRecordReader reader = new CSVFlightRecordReader(csvFile.toString());
        List<FlightRecord> records = reader.readRecords();

        VideoSegmentExtractor extractor = new VideoSegmentExtractor();
        List<VideoSegment> segments = extractor.extractSegments(records);

        // Then
        assertEquals(2, segments.size());
        VideoSegment firstSegment = segments.get(0);
        assertEquals(2, firstSegment.getDataPointCount());

        // Check compass heading values
        List<FlightRecord> firstSegmentRecords = firstSegment.getFlightRecords();
        assertEquals(45.0, firstSegmentRecords.get(0).compassHeading(), 0.001);
        assertEquals(46.0, firstSegmentRecords.get(1).compassHeading(), 0.001);
    }
}