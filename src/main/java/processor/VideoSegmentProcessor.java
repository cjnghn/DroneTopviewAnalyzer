package processor;

import domain.FlightRecord;
import domain.VideoSegment;
import exception.ProcessingException;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
public class VideoSegmentProcessor {

    /**
     * 연속된 비행 로그에서 비디오 세그먼트들을 추출합니다.
     * isVideo 플래그가 true인 연속된 구간을 하나의 세그먼트로 처리합니다.
     */
    public List<VideoSegment> extractSegments(List<FlightRecord> flightRecords) {
        if (flightRecords == null || flightRecords.isEmpty()) {
            throw new ProcessingException("Flight logs cannot be null or empty");
        }

        List<VideoSegment> segments = new ArrayList<>();
        List<FlightRecord> currentSegmentRecords = new ArrayList<>();
        Double segmentStartTime = null;

        for (var record : flightRecords) {
            if (record.getIsVideo()) {
                if (segmentStartTime == null) {
                    segmentStartTime = record.getElapsedTime();
                }
                currentSegmentRecords.add(record);
            } else {
                if (!currentSegmentRecords.isEmpty()) {
                    segments.add(createSegment(currentSegmentRecords, segmentStartTime));
                    currentSegmentRecords = new ArrayList<>();
                    segmentStartTime = null;
                }
            }
        }

        // 마지막 세그먼트 처리
        if (!currentSegmentRecords.isEmpty()) {
            segments.add(createSegment(currentSegmentRecords, segmentStartTime));
        }

        return segments;
    }

    public VideoSegment createSegment(List<FlightRecord> records, double startTime) {
        if (records.isEmpty()) {
            throw new ProcessingException("빈 비행 기록으로는 비디오 세그먼트를 생성할 수 없다.");
        }

        return VideoSegment.builder()
            .startElapseTime(startTime)
            .endElapseTime(records.get(records.size() - 1).getElapsedTime())
            .flightRecords(new ArrayList<>(records))
            .build();
    }

    /**
     * 주어진 타임스탬프가 비디오 세그먼트 내에 포함되는지 확인합니다.
     */
    public boolean isTimestampInSegment(VideoSegment segment, double timestamp) {
        return timestamp >= segment.getStartElapseTime() &&
                timestamp <= segment.getEndElapseTime();
    }

    /**
     * 비디오 세그먼트의 지속 시간을 반환합니다.
     */
    public double getSegmentDuration(VideoSegment segment) {
        return segment.getEndElapseTime() - segment.getStartElapseTime();
    }
}
