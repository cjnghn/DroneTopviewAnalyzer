package processor;

import domain.FlightRecord;
import exception.ProcessingException;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
public class FlightRecordInterpolator {

    public List<FlightRecord> interpolateByFps(List<FlightRecord> flightRecords, double fps) {
        if (flightRecords == null || flightRecords.size() < 2) {
            throw new ProcessingException("At least 2 flight logs are required for interpolation");
        }

        double frameInterval = 1.0 / fps;   // 프레임 간 시간 간격 (초)
        List<FlightRecord> interpolatedLogs = new ArrayList<>();

        FlightRecord firstRecord = flightRecords.get(0);
        FlightRecord lastRecord = flightRecords.get(flightRecords.size() - 1);
        double startTime = firstRecord.getElapsedTime();
        double endTime = lastRecord.getElapsedTime();

        for (double currentTime = startTime; currentTime <= endTime; currentTime += frameInterval) {
            interpolatedLogs.add(interpolateAtTime(flightRecords, currentTime));
        }

        return interpolatedLogs;
    }

    public FlightRecord interpolateAtTime(List<FlightRecord> flightLogs, double targetTime) {
        // 범위를 벗어나는 경우 처리
        if (targetTime <= flightLogs.get(0).getElapsedTime()) {
            return flightLogs.get(0);
        }
        if (targetTime >= flightLogs.get(flightLogs.size() - 1).getElapsedTime()) {
            return flightLogs.get(flightLogs.size() - 1);
        }

        // 이전/다음 로그 찾기
        int index = findPreviousLogIndex(flightLogs, targetTime);

        FlightRecord prevLog = flightLogs.get(index);
        FlightRecord nextLog = flightLogs.get(index + 1);

        // 보간 비율 계산
        double ratio = calculateRatio(prevLog.getElapsedTime(), nextLog.getElapsedTime(), targetTime);

        // 선형 보간
        return FlightRecord.builder()
            .elapsedTime(targetTime)
            .datetime(interpolateDateTime(prevLog.getDatetime(), nextLog.getDatetime(), ratio))
            .latitude(lerp(prevLog.getLatitude(), nextLog.getLatitude(), ratio))
            .longitude(lerp(prevLog.getLongitude(), nextLog.getLongitude(), ratio))
            .ascent(lerp(prevLog.getAscent(), nextLog.getAscent(), ratio))
            .compassHeading(interpolateHeading(prevLog.getCompassHeading(), nextLog.getCompassHeading(), ratio))
            .isVideo(prevLog.getIsVideo())
            .build();
    }

    /**
     * 이전 로그의 인덱스를 찾습니다.
     */
    private int findPreviousLogIndex(List<FlightRecord> logs, double targetTime) {
        for (int i = 0; i < logs.size() - 1; i++) {
            if (logs.get(i).getElapsedTime() <= targetTime && logs.get(i + 1).getElapsedTime() > targetTime) {
                return i;
            }
        }
        return -1;
    }

    /**
     * 선형 보간 비율을 계산합니다.
     */
    private double calculateRatio(double start, double end, double target) {
        return (target - start) / (end - start);
    }

    /**
     * 두 값 사이를 선형 보간합니다.
     */
    private double lerp(double start, double end, double ratio) {
        return start + (end - start) * ratio;
    }

    /**
     * Heading 값을 보간합니다. (각도의 최단 경로를 고려)
     */
    private double interpolateHeading(double start, double end, double ratio) {
        double diff = end - start;

        // 각도 차이를 ±180도 이내로 조정
        if (diff > 180) diff -= 360;
        else if (diff < -180) diff += 360;

        double result = start + diff * ratio;

        // 결과를 0~360 범위로 정규화
        if (result < 0) result += 360;
        else if (result >= 360) result -= 360;

        return result;
    }

    /**
     * DateTime을 보간합니다.
     */
    private LocalDateTime interpolateDateTime(LocalDateTime start, LocalDateTime end, double ratio) {
        long millisBetween = ChronoUnit.MILLIS.between(start, end);
        long interpolatedMillis = (long)(millisBetween * ratio);
        return start.plus(interpolatedMillis, ChronoUnit.MILLIS);
    }
}
