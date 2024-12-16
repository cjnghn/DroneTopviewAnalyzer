package flight;

import java.io.IOException;
import java.util.List;

public interface FlightRecordReader {
    List<FlightRecord> readRecords() throws IOException;
}
