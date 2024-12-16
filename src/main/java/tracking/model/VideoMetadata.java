package tracking.model;

public record VideoMetadata(
    String name,
    int width,
    int height,
    double fps,
    int totalFrames
) {
    public VideoMetadata {
        if (width <= 0 || height <= 0) {
            throw new IllegalArgumentException("Video dimensions must be positive");
        }
        if (fps <= 0) {
            throw new IllegalArgumentException("FPS must be positive");
        }
        if (totalFrames <= 0) {
            throw new IllegalArgumentException("Total frames must be positive");
        }
    }

    public double getFrameDuration() {
        return 1.0 / fps;
    }
}
