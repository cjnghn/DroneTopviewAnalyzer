package videosegment;

import flight.FlightRecord;
import georeference.*;
import tracking.model.Frame;
import tracking.model.Detection;
import tracking.model.VideoMetadata;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

public class GeoreferencedVideoSegment {
    private final VideoSegment segment;
    private final GeoreferencingCalculator geoCalculator;
    private final List<Frame> frames;
    private final Map<Integer, FlightRecord> frameToFlightRecord;

    public GeoreferencedVideoSegment(
            VideoSegment segment,
            List<Frame> frames,
            List<FlightRecord> interpolatedRecords,
            VideoMetadata videoMetadata,
            double fovDegrees) {
        this.segment = segment;
        this.frames = frames;
        this.geoCalculator = new GeoreferencingCalculator
            .Builder(
                videoMetadata.width(),
                videoMetadata.height(),
                fovDegrees
            )
            .build();

        // 프레임 인덱스와 FlightRecord 매핑
        this.frameToFlightRecord = new HashMap<>();
        for (int i = 0; i < interpolatedRecords.size(); i++) {
            frameToFlightRecord.put(i, interpolatedRecords.get(i));
        }
    }

    public List<GeoreferencedDetection> getGeoReferencedDetections(int frameIndex) {
        Frame frame = frames.stream()
                .filter(f -> f.index() == frameIndex)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Frame not found: " + frameIndex));

        FlightRecord record = frameToFlightRecord.get(frameIndex);
        if (record == null) {
            throw new IllegalStateException("No flight record for frame: " + frameIndex);
        }

        List<GeoreferencedDetection> geoDetections = new ArrayList<>();
        for (Detection detection : frame.detections()) {
            GeoPoint center = geoCalculator.pixelToGps(
                detection.bbox().centerX(),
                detection.bbox().centerY(),
                new GeoPoint(record.latitude(), record.longitude()),
                record.getAltitudeMeters(),
                record.compassHeading()
            );

            List<GeoPoint> corners = getDetectionCorners(detection, record);

            geoDetections.add(new GeoreferencedDetection(
                detection.trackId(),
                detection.confidence(),
                detection.classId(),
                center,
                corners,
                detection.bbox()
            ));
        }

        return geoDetections;
    }

    private List<GeoPoint> getDetectionCorners(Detection detection, FlightRecord record) {
        // Detection의 각 모서리 픽셀 좌표를 GPS 좌표로 변환
        var bbox = detection.bbox();
        List<GeoPoint> corners = new ArrayList<>();

        // 박스의 네 모서리를 GPS 좌표로 변환
        double[][] cornerPixels = {
            {bbox.minX(), bbox.minY()},  // 좌상단
            {bbox.maxX(), bbox.minY()},  // 우상단
            {bbox.maxX(), bbox.maxY()},  // 우하단
            {bbox.minX(), bbox.maxY()}   // 좌하단
        };

        for (double[] pixel : cornerPixels) {
            GeoPoint corner = geoCalculator.pixelToGps(
                pixel[0], pixel[1],
                new GeoPoint(record.latitude(), record.longitude()),
                record.getAltitudeMeters(),
                record.compassHeading()
            );
            corners.add(corner);
        }

        return corners;
    }

    public VideoSegment getSegment() {
        return segment;
    }

    public List<Frame> getFrames() {
        return frames;
    }
}