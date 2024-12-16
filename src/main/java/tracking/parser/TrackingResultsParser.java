package tracking.parser;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;
import tracking.*;
import tracking.model.*;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class TrackingResultsParser {
    private final ObjectMapper mapper = new ObjectMapper();

    public TrackingResults parse(Path jsonPath) throws IOException {
        JsonNode root = mapper.readTree(jsonPath.toFile());

        ModelConfig model = parseModelConfig(root.get("model"));
        TrackerConfig tracker = parseTrackerConfig(root.get("tracker"));
        VideoMetadata video = parseVideoMetadata(root.get("video"));
        List<Frame> frames = parseTrackingResults(root.get("tracking_results"));

        return new TrackingResults(model, tracker, video, frames);
    }

    private ModelConfig parseModelConfig(JsonNode node) {
        return new ModelConfig(
            node.get("name").asText(),
            node.get("confidence_threshold").asDouble(),
            node.get("nms").asBoolean()
        );
    }

    private TrackerConfig parseTrackerConfig(JsonNode node) {
        return new TrackerConfig(
            node.get("name").asText()
        );
    }

    private VideoMetadata parseVideoMetadata(JsonNode node) {
        return new VideoMetadata(
            node.get("name").asText(),
            node.get("width").asInt(),
            node.get("height").asInt(),
            node.get("fps").asDouble(),
            node.get("total_frames").asInt()
        );
    }

    private List<Frame> parseTrackingResults(JsonNode resultsArray) {
        List<Frame> frames = new ArrayList<>();

        for (JsonNode frameNode : resultsArray) {
            int frameIndex = frameNode.get("i").asInt();
            List<Detection> detections = parseDetections(frameNode.get("res"));
            frames.add(new Frame(frameIndex, detections));
        }

        return frames;
    }

    private List<Detection> parseDetections(JsonNode detectionsArray) {
        List<Detection> detections = new ArrayList<>();

        for (JsonNode detNode : detectionsArray) {
            JsonNode bboxNode = detNode.get("bbox");
            double minX = bboxNode.get(0).asDouble();
            double minY = bboxNode.get(1).asDouble();
            double maxX = bboxNode.get(2).asDouble();
            double maxY = bboxNode.get(3).asDouble();

            BoundingBox bbox = new BoundingBox(minX, minY, maxX, maxY);

            Detection detection = new Detection(
                detNode.get("tid").asInt(),
                bbox,
                detNode.get("conf").asDouble(),
                detNode.get("cid").asInt()
            );

            detections.add(detection);
        }

        return detections;
    }
}
