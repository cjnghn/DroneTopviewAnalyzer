package flight;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CSVFlightRecordReaderTest {
    @Test
    void shouldReadValidCSVFile(@TempDir Path tempDir) throws IOException {
        // Given
        Path csvFile = tempDir.resolve("test.csv");
        String csvContent = """
            time(millisecond),datetime(utc),latitude,longitude,compass_heading(degrees),isVideo
            1000,2024-01-01 10:00:00,37.5,127.1,45.5,1
            2000,2024-01-01 10:00:01,37.6,127.2,46.0,1
            3000,2024-01-01 10:00:02,37.7,127.3,46.5,0
            """;
        Files.writeString(csvFile, csvContent);

        // When
        FlightRecordReader reader = new CSVFlightRecordReader(csvFile.toString());
        List<FlightRecord> records = reader.readRecords();

        // Then
        assertEquals(3, records.size());
        FlightRecord firstRecord = records.get(0);

        assertEquals(1000L, firstRecord.elapsedTimeMillis());
        assertEquals(
                LocalDateTime.parse("2024-01-01 10:00:00",
                        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")),
                firstRecord.dateTime()
        );
        assertEquals(37.5, firstRecord.latitude(), 0.001);
        assertTrue(firstRecord.isVideo());
    }

    @Test
    void shouldThrowExceptionForMalformedDateTime(@TempDir Path tempDir) throws IOException {
        // Given
        Path csvFile = tempDir.resolve("test.csv");
        String csvContent = """
        time(millisecond),datetime(utc),latitude,longitude,compass_heading(degrees),isVideo
        1000,2024-01-01X10:00:00,37.5,127.1,45.5,1
        """;
        Files.writeString(csvFile, csvContent);

        // When & Then
        FlightRecordReader reader = new CSVFlightRecordReader(csvFile.toString());
        IOException thrown = assertThrows(IOException.class, reader::readRecords);

        assertTrue(thrown.getMessage().contains("Invalid datetime format"));
    }

}