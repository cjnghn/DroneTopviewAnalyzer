package flight;

import com.opencsv.bean.CsvBindByName;
import com.opencsv.bean.CsvCustomBindByName;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class CSVFlightRecordData {
    private static final DateTimeFormatter DATE_TIME_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @CsvBindByName(column = "time(millisecond)")
    private Long elapsedTimeMillis;

    @CsvBindByName(column = "datetime(utc)")
    private String datetime;

    @CsvBindByName(column = "latitude")
    private Double latitude;

    @CsvBindByName(column = "longitude")
    private Double longitude;

    @CsvBindByName(column = "altitude(feet)")
    private Double altitudeFeet;

    @CsvBindByName(column = "compass_heading(degrees)")
    private Double compassHeading;

    @CsvBindByName(column = "isVideo")
    private Integer isVideo;

    public FlightRecord toFlightRecord() {
        validateFields();

        try {
            LocalDateTime dateTime = LocalDateTime.parse(datetime.trim(), DATE_TIME_FORMATTER);
            return new FlightRecord(
                    elapsedTimeMillis,
                    dateTime,
                    latitude,
                    longitude,
                    altitudeFeet,
                    compassHeading,
                    isVideo == 1
            );
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Invalid datetime format: " + datetime, e);
        }
    }

    private void validateFields() {
        if (elapsedTimeMillis == null) {
            throw new IllegalArgumentException("Field 'time(millisecond)' is missing or invalid.");
        }
        if (datetime == null || datetime.trim().isEmpty()) {
            throw new IllegalArgumentException("Field 'datetime(utc)' is missing or invalid.");
        }
        if (latitude == null) {
            throw new IllegalArgumentException("Field 'latitude' is missing or invalid.");
        }
        if (longitude == null) {
            throw new IllegalArgumentException("Field 'longitude' is missing or invalid.");
        }
        if (altitudeFeet == null) {
            throw new IllegalArgumentException("Field 'altitude(feet)' is missing or invalid.");
        }
        if (compassHeading == null) {
            throw new IllegalArgumentException("Field 'compass_heading(degrees)' is missing or invalid.");
        }
        if (isVideo == null) {
            throw new IllegalArgumentException("Field 'isVideo' is missing or invalid.");
        }
    }

    // Getters and setters with proper naming
    public Long getElapsedTimeMillis() { return elapsedTimeMillis; }
    public void setElapsedTimeMillis(Long value) { this.elapsedTimeMillis = value; }

    public String getDatetime() { return datetime; }
    public void setDatetime(String value) { this.datetime = value; }

    public Double getLatitude() { return latitude; }
    public void setLatitude(Double value) { this.latitude = value; }

    public Double getLongitude() { return longitude; }
    public void setLongitude(Double value) { this.longitude = value; }

    public Double getAltitudeFeet() { return altitudeFeet; }
    public void setAltitudeFeet(Double value) { this.altitudeFeet = value; }

    public Double getCompassHeading() { return compassHeading; }
    public void setCompassHeading(Double value) { this.compassHeading = value; }

    public Integer getIsVideo() { return isVideo; }
    public void setIsVideo(Integer value) { this.isVideo = value; }
}

