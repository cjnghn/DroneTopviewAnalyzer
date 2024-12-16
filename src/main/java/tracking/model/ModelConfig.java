package tracking.model;

public record ModelConfig(
    String name,
    double confidenceThreshold,
    boolean nms
) {}
