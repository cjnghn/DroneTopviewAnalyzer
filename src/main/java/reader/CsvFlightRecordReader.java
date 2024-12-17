package reader;

import com.opencsv.bean.CsvToBeanBuilder;
import domain.FlightRecord;
import reader.dto.FlightRecordDTO;

import java.io.FileReader;
import java.util.List;
import java.util.stream.Collectors;

public class CsvFlightRecordReader {
    public List<FlightRecord> read(String filePath) throws Exception {
        try (FileReader reader = new FileReader(filePath)) {
            List<FlightRecordDTO> dtoList = new CsvToBeanBuilder<FlightRecordDTO>(reader)
                    .withType(FlightRecordDTO.class)
                    .withIgnoreLeadingWhiteSpace(true)
                    .build()
                    .parse();

            return dtoList.stream()
                    .map(FlightRecordDTO::toDomain)
                    .collect(Collectors.toList());
        }
    }
}
