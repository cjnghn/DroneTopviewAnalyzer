// DroneVideoProcessor.java
package processor;

import calculator.GeoreferencingCalculator;
import calculator.IntersectionCalculator;
import domain.*;
import exception.ProcessingException;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import reader.CsvFlightRecordReader;
import reader.JsonTrackingReader;
import reader.dto.VideoMetadata;

import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
@Builder
public class DroneVideoProcessor {
    private final CsvFlightRecordReader flightRecordReader;
    private final JsonTrackingReader trackingReader;
    private final VideoSegmentProcessor segmentProcessor;
    private final FlightRecordInterpolator interpolator;
    private final GeoreferencingCalculator georeferencingCalculator;
    private final IntersectionCalculator intersectionCalculator;
    private final double fovDegrees;

    public List<ProcessedSegment> processVideo(String flightLogPath, String[] trackingJsonPaths) {
        System.out.println("=== Start Video Processing ===");

        // 1. 비행 로그 읽기
        System.out.println("Step 1: Reading flight log from: " + flightLogPath);
        List<FlightRecord> flightRecords = readFlightLog(flightLogPath);
        System.out.println("Step 1 Result: Total flight records read = " + flightRecords.size());

        // 2. 비디오 세그먼트 추출
        System.out.println("Step 2: Extracting video segments...");
        List<VideoSegment> segments = segmentProcessor.extractSegments(flightRecords);
        System.out.println("Step 2 Result: Total segments extracted = " + segments.size());

        // 세그먼트 수와 트래킹 결과 파일 수가 일치하는지 확인
        if (segments.size() != trackingJsonPaths.length) {
            throw new ProcessingException(
                    String.format("Number of segments (%d) and tracking files (%d) do not match",
                            segments.size(), trackingJsonPaths.length)
            );
        }

        // 3. 각 세그먼트별 처리 (교차점 계산 포함)
        System.out.println("Step 3: Processing each segment...");
        List<ProcessedSegment> processedSegments = processSegments(segments, trackingJsonPaths);
        System.out.println("Step 3 Result: Total processed segments = " + processedSegments.size());

        System.out.println("=== Video Processing Completed ===");
        return processedSegments;
    }

    private List<FlightRecord> readFlightLog(String path) {
        try {
            List<FlightRecord> records = flightRecordReader.read(path);
            System.out.println("Read " + records.size() + " flight records from log.");
            return records;
        } catch (Exception e) {
            throw new ProcessingException("Failed to read flight log: " + e.getMessage(), e);
        }
    }

    private List<ProcessedSegment> processSegments(List<VideoSegment> segments, String[] trackingJsonPaths) {
        List<ProcessedSegment> processedSegments = new ArrayList<>();

        for (int i = 0; i < segments.size(); i++) {
            VideoSegment segment = segments.get(i);
            String trackingPath = trackingJsonPaths[i];
            System.out.println("Processing segment " + (i + 1) + "/" + segments.size() + ", Tracking file: " + trackingPath);

            try {
                // 1. 트래킹 결과 읽기
                JsonTrackingReader.TrackingData trackingData = trackingReader.read(trackingPath);
                System.out.println("Read tracking data: Total objects tracked = " + trackingData.getTrackedObjects().size());

                // 2. 프레임 레이트에 맞춰 비행 로그 보간
                List<FlightRecord> interpolatedRecords = interpolator.interpolateByFps(
                        segment.getFlightRecords(),
                        trackingData.getMetadata().getFps()
                );
                System.out.println("Interpolated " + interpolatedRecords.size() + " flight records for FPS: " + trackingData.getMetadata().getFps());

                // GeoreferencingCalculator 초기화 (비디오 메타데이터 기반)
                GeoreferencingCalculator calculator = GeoreferencingCalculator.builder()
                        .videoWidth(trackingData.getMetadata().getWidth())
                        .videoHeight(trackingData.getMetadata().getHeight())
                        .fovDegrees(fovDegrees)
                        .build();

                // 3. 각 객체의 지리참조 정보 계산
                List<GeoreferencedObject> geoObjects = new ArrayList<>();
                for (TrackedObject obj : trackingData.getTrackedObjects()) {
                    FlightRecord record = interpolatedRecords.get(obj.getFrameNumber());
                    geoObjects.add(calculator.georeference(obj, record));
                }
                System.out.println("Georeferenced " + geoObjects.size() + " objects for this segment.");

                // 4. 해당 세그먼트의 교차점 계산
                System.out.println("Finding intersections for segment " + (i + 1));
                List<TrajectoryIntersection> segmentIntersections =
                        intersectionCalculator.findIntersections(geoObjects);
                System.out.println("Found " + segmentIntersections.size() + " intersections in segment " + (i + 1));

                processedSegments.add(new ProcessedSegment(
                        segment,
                        geoObjects,
                        trackingData.getMetadata(),
                        segmentIntersections
                ));

            } catch (Exception e) {
                throw new ProcessingException(
                        String.format("Failed to process segment %d: %s", i, e.getMessage()), e
                );
            }
        }

        return processedSegments;
    }

    @Value
    public static class ProcessedSegment {
        VideoSegment segment;
        List<GeoreferencedObject> georeferencedObjects;
        VideoMetadata metadata;
        List<TrajectoryIntersection> intersections;
    }

}