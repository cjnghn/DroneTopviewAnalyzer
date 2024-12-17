package reader.dto;

import com.opencsv.bean.CsvBindByName;
import com.opencsv.bean.CsvCustomBindByName;
import domain.FlightRecord;
import lombok.Getter;
import lombok.NoArgsConstructor;
import reader.converter.BooleanConverter;
import reader.converter.FeetToMeterConverter;
import reader.converter.LocalDateTimeConverter;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
public class FlightRecordDTO {
    @CsvBindByName(column = "time(millisecond)")
    private Double time;

    @CsvCustomBindByName(column = "datetime(utc)", converter = LocalDateTimeConverter.class)
    private LocalDateTime datetime;

    @CsvBindByName(column = "latitude")
    private Double latitude;

    @CsvBindByName(column = "longitude")
    private Double longitude;

    @CsvCustomBindByName(column = "ascent(feet)", converter = FeetToMeterConverter.class)
    private Double ascentInMeters;

    @CsvCustomBindByName(column = "isVideo", converter = BooleanConverter.class)
    private Boolean isVideo;

    @CsvBindByName(column = "compass_heading(degrees)")
    private Double compassHeading;

    // DTO를 도메인 모델로 변환하는 메서드
    public FlightRecord toDomain() {
        return new FlightRecord(
            time, datetime,
            latitude, longitude, ascentInMeters,
            compassHeading,
            isVideo
        );
    }
}