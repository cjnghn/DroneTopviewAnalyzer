package reader;

import com.fasterxml.jackson.databind.ObjectMapper;
import domain.BoundingBox;
import domain.TrackedObject;
import lombok.Value;
import reader.dto.TrackingResultDTO;
import reader.dto.DetectionDTO;
import reader.dto.VideoMetadata;

import java.io.File;
import java.util.List;
import java.util.stream.Collectors;


public class JsonTrackingReader {
    private final ObjectMapper objectMapper;

    public JsonTrackingReader() {
        this.objectMapper = new ObjectMapper();
    }

    @Value
    public static class TrackingData {
        VideoMetadata metadata;
        List<TrackedObject> trackedObjects;
    }

    public TrackingData read(String filePath) throws Exception {
        TrackingResultDTO trackingResult = objectMapper
            .readValue(new File(filePath), TrackingResultDTO.class);

        // 비디오 메타데이터 추출
        VideoMetadata metadata = VideoMetadata.builder()
            .name(trackingResult.getVideo().getName())
            .width(trackingResult.getVideo().getWidth())
            .height(trackingResult.getVideo().getHeight())
            .fps(trackingResult.getVideo().getFps())
            .totalFrames(trackingResult.getVideo().getTotalFrames())
            .build();

        // 트래킹 객체들 추출
        List<TrackedObject> trackedObjects = trackingResult.getTrackingResults()
            .stream()
            .flatMap(frame -> frame.getDetections()
                .stream()
                .map(detection -> toTrackedObject(detection, frame.getFrameIndex())))
            .collect(Collectors.toList());

        return new TrackingData(metadata, trackedObjects);
    }

    private TrackedObject toTrackedObject(DetectionDTO detection, int frameIndex) {
        return TrackedObject.builder()
            .trackingId(detection.getTid())
            .classId(detection.getCid())
            .confidence(detection.getConf())
            .boundingBox(toBoundingBox(detection.getBbox()))
            .frameNumber(frameIndex)
            .build();
    }

    private BoundingBox toBoundingBox(double[] bbox) {
        return new BoundingBox(bbox[0], bbox[1], bbox[2], bbox[3]);
    }
}
