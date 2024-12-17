import calculator.IntersectionCalculator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import domain.TrajectoryIntersection;
import processor.DroneVideoProcessor;
import processor.FlightRecordInterpolator;
import processor.VideoSegmentProcessor;
import reader.CsvFlightRecordReader;
import reader.JsonTrackingReader;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class Main {
    private final static String RESOURCES = "src/main/resources/";
    private final static String OUTPUT = RESOURCES + "output/";

    public static void main(String[] args) {
        if (args.length < 3) {
            System.out.println("Usage: java Main <flightLogPath> <trackingJsonPath1> <trackingJsonPath2> ...");
            System.out.println("Example: java Main flight.csv segment1.json segment2.json");
            System.exit(1);
        }

        try {
            // 1. 입력 파일 경로 설정
            String flightLogPath = RESOURCES + args[0];
            String[] trackingJsonPaths = new String[args.length - 1];
            for (int i = 0; i < args.length - 1; i++) {
                trackingJsonPaths[i] = RESOURCES + args[i + 1];
            }

            // 2. Processor 초기화
            DroneVideoProcessor processor = DroneVideoProcessor.builder()
                    .flightRecordReader(new CsvFlightRecordReader())
                    .trackingReader(new JsonTrackingReader())
                    .segmentProcessor(new VideoSegmentProcessor())
                    .interpolator(new FlightRecordInterpolator())
                    .intersectionCalculator(new IntersectionCalculator())
                    .fovDegrees(59.0)
                    .build();

            // 3. 비디오 처리 실행
            var result = processor.processVideo(flightLogPath, trackingJsonPaths);

            // 4. 결과 출력 및 저장
            printResults(result);
            saveResults(result);

        } catch (Exception e) {
            System.err.println("Error processing video: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }

    private static void printResults(List<DroneVideoProcessor.ProcessedSegment> segments) {
        System.out.println("\n=== Processing Results ===");

        // 세그먼트별 결과 출력
        System.out.println("\nProcessed Segments: " + segments.size());

        for (int i = 0; i < segments.size(); i++) {
            DroneVideoProcessor.ProcessedSegment processedSegment = segments.get(i);
            System.out.printf("\nSegment %d:%n", i + 1);
            System.out.printf("  - Time Range: %.1f ~ %.1f seconds%n",
                    processedSegment.getSegment().getStartElapseTime(),
                    processedSegment.getSegment().getEndElapseTime());
            System.out.printf("  - Video: %s (%dx%d)%n",
                    processedSegment.getMetadata().getName(),
                    processedSegment.getMetadata().getWidth(),
                    processedSegment.getMetadata().getHeight());
            System.out.printf("  - Tracked Objects: %d%n",
                    processedSegment.getGeoreferencedObjects().size());
            System.out.printf("  - Intersections: %d%n",
                    processedSegment.getIntersections().size());

            // 해당 세그먼트의 교차점 출력
            List<TrajectoryIntersection> intersections = processedSegment.getIntersections();
            if (!intersections.isEmpty()) {
                System.out.println("  - Intersection Details:");
                for (int j = 0; j < intersections.size(); j++) {
                    TrajectoryIntersection intersection = intersections.get(j);
                    System.out.printf("    %d) Time: %.1f seconds, Objects: %s intersects with %s%n",
                            j + 1,
                            intersection.getTimestamp(),
                            intersection.getObject1().getTrackedObject().getTrackingId(),
                            intersection.getObject2().getTrackedObject().getTrackingId());
                }
            }
        }

        System.out.println("\nSaving results to: " + OUTPUT);
    }

    private static void saveResults(List<DroneVideoProcessor.ProcessedSegment> segments) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        // 결과 저장할 디렉토리 생성
        Files.createDirectories(Paths.get(OUTPUT));

        // 각 세그먼트별 결과 저장
        for (int i = 0; i < segments.size(); i++) {
            DroneVideoProcessor.ProcessedSegment segment = segments.get(i);
            String segmentOutputPath = OUTPUT + "processed_segment_" + (i + 1) + ".json";

            objectMapper.writeValue(new File(segmentOutputPath), segment);
            System.out.println("Saved segment result to: " + segmentOutputPath);
        }

        // 전체 결과 저장
        String fullResultPath = OUTPUT + "full_result.json";
        objectMapper.writeValue(new File(fullResultPath), segments);
        System.out.println("Saved full result to: " + fullResultPath);
    }
}