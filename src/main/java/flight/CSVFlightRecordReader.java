package flight;

import com.opencsv.bean.CsvToBeanBuilder;
import com.opencsv.exceptions.CsvException;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;

public class CSVFlightRecordReader implements FlightRecordReader {
    private final String filePath;

    public CSVFlightRecordReader(String filePath) {
        if (filePath == null || filePath.trim().isEmpty()) {
            throw new IllegalArgumentException("File path cannot be null or empty");
        }
        this.filePath = filePath;
    }

    @Override
    public List<FlightRecord> readRecords() throws IOException {
        try (FileReader reader = new FileReader(filePath)) {
            List<CSVFlightRecordData> records = new CsvToBeanBuilder<CSVFlightRecordData>(reader)
                    .withType(CSVFlightRecordData.class)
                    .withSkipLines(0) // 헤더를 건너뛰지 않습니다.
                    .withThrowExceptions(true) // 모든 예외를 발생시키도록 변경
                    .build()
                    .parse();

            return records.stream()
                    .map(CSVFlightRecordData::toFlightRecord)
                    .toList();
        } catch (Exception e) {
            throw new IOException("Error reading CSV file: " + e.getMessage(), e);
        }
    }

}
