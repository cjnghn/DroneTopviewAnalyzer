package reader;

import domain.FlightRecord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.StringReader;
import java.time.LocalDateTime;
import java.util.List;

import com.opencsv.bean.CsvToBeanBuilder;
import reader.dto.FlightRecordDTO;

import static org.junit.jupiter.api.Assertions.*;

public class CsvFlightRecordReaderTest {

    private String mockCsvData;

    @BeforeEach
    void setup() {
        mockCsvData = "time(millisecond),datetime(utc),latitude,longitude,ascent(feet),compass_heading(degrees),isVideo\n"
                + "1622548800000,2024-11-19 05:27:23,37.7749,-122.4194,328,45,1\n"
                + "1622548810000,2024-11-19 06:00:00,37.7750,-122.4195,656,90,0";
    }

    @Test
    void testCsvParsingAndMapping() {
        StringReader stringReader = new StringReader(mockCsvData);

        List<FlightRecordDTO> dtoList = new CsvToBeanBuilder<FlightRecordDTO>(stringReader)
                .withType(FlightRecordDTO.class)
                .withIgnoreLeadingWhiteSpace(true)
                .build()
                .parse();

        assertEquals(2, dtoList.size());

        // 첫 번째 DTO 검증
        FlightRecordDTO dto1 = dtoList.get(0);
        assertEquals(LocalDateTime.of(2024, 11, 19, 5, 27, 23), dto1.getDatetime());
        assertEquals(99.9744, dto1.getAscentInMeters(), 0.0001);

        // 두 번째 DTO 검증
        FlightRecordDTO dto2 = dtoList.get(1);
        assertEquals(LocalDateTime.of(2024, 11, 19, 6, 0, 0), dto2.getDatetime());
    }

    @Test
    void testDtoToDomainMapping() {
        StringReader stringReader = new StringReader(mockCsvData);

        List<FlightRecordDTO> dtoList = new CsvToBeanBuilder<FlightRecordDTO>(stringReader)
                .withType(FlightRecordDTO.class)
                .withIgnoreLeadingWhiteSpace(true)
                .build()
                .parse();

        List<FlightRecord> records = dtoList.stream()
                .map(FlightRecordDTO::toDomain)
                .toList();

        assertEquals(2, records.size());

        // 첫 번째 도메인 모델 검증
        FlightRecord record1 = records.get(0);
        assertEquals(LocalDateTime.of(2024, 11, 19, 5, 27, 23), record1.getDatetime());
        assertEquals(99.9744, record1.getAscent(), 0.0001);

        // 두 번째 도메인 모델 검증
        FlightRecord record2 = records.get(1);
        assertEquals(LocalDateTime.of(2024, 11, 19, 6, 0, 0), record2.getDatetime());
    }
}