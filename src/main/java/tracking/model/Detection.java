package tracking.model;

public record Detection(
    int trackId,
    BoundingBox bbox,
    double confidence,
    int classId
) {
    public Detection {
        if (confidence < 0 || confidence > 1) {
            throw new IllegalArgumentException("Confidence must be between 0 and 1");
        }
    }
}
