package interpolation;

import flight.FlightRecord;
import tracking.model.VideoMetadata;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

public class FlightRecordInterpolator {
    private final VideoMetadata videoMetadata;

    public FlightRecordInterpolator(VideoMetadata videoMetadata) {
        this.videoMetadata = videoMetadata;
    }

    public List<FlightRecord> interpolateToFrames(List<FlightRecord> records) {
        if (records == null || records.size() < 2) {
            throw new IllegalArgumentException("Need at least 2 records for interpolation");
        }

        List<FlightRecord> interpolatedRecords = new ArrayList<>();
        double frameInterval = 1.0 / videoMetadata.fps(); // 초 단위의 프레임 간격
        LocalDateTime firstTime = records.get(0).dateTime();
        LocalDateTime lastTime = records.get(records.size() - 1).dateTime();

        // 총 프레임 수 계산
        long totalMicros = ChronoUnit.MICROS.between(firstTime, lastTime);
        long microsPerFrame = (long)(frameInterval * 1_000_000);
        int frameCount = (int)(totalMicros / microsPerFrame);

        // 각 프레임에 대해 보간
        for (int frameIndex = 0; frameIndex < frameCount; frameIndex++) {
            LocalDateTime frameTime = firstTime.plus(
                    frameIndex * microsPerFrame,
                    ChronoUnit.MICROS
            );

            FlightRecord interpolatedRecord = interpolateAtTime(records, frameTime, frameIndex);
            interpolatedRecords.add(interpolatedRecord);
        }

        return interpolatedRecords;
    }

    private FlightRecord interpolateAtTime(List<FlightRecord> records, LocalDateTime targetTime, int frameIndex) {
        int index = findNearestIndex(records, targetTime);

        if (index <= 0) {
            return records.get(0);
        }
        if (index >= records.size()) {
            return records.get(records.size() - 1);
        }

        FlightRecord before = records.get(index - 1);
        FlightRecord after = records.get(index);

        double ratio = calculateTimeRatio(before.dateTime(), after.dateTime(), targetTime);

        return new FlightRecord(
                frameIndex * (1000 / (long)videoMetadata.fps()),
                targetTime,
                interpolateValue(before.latitude(), after.latitude(), ratio),
                interpolateValue(before.longitude(), after.longitude(), ratio),
                interpolateValue(before.altitudeFeet(), after.altitudeFeet(), ratio),
                interpolateValue(before.compassHeading(), after.compassHeading(), ratio),
                before.isVideo()
        );
    }

    private int findNearestIndex(List<FlightRecord> records, LocalDateTime targetTime) {
        int low = 0;
        int high = records.size() - 1;

        while (low <= high) {
            int mid = (low + high) >>> 1;
            LocalDateTime midTime = records.get(mid).dateTime();

            if (midTime.equals(targetTime)) {
                return mid;
            } else if (midTime.isBefore(targetTime)) {
                low = mid + 1;
            } else {
                high = mid - 1;
            }
        }

        return low;
    }

    private double calculateTimeRatio(LocalDateTime before, LocalDateTime after, LocalDateTime target) {
        double totalDiff = ChronoUnit.MICROS.between(before, after);
        double targetDiff = ChronoUnit.MICROS.between(before, target);
        return targetDiff / totalDiff;
    }

    private double interpolateValue(double before, double after, double ratio) {
        return before + (after - before) * ratio;
    }
}