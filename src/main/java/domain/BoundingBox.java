package domain;

import lombok.RequiredArgsConstructor;
import lombok.Value;

@Value
@RequiredArgsConstructor
public class BoundingBox {
    double xMin;
    double yMin;
    double xMax;
    double yMax;

    public double getWidth() {
        return xMax - xMin;
    }

    public double getHeight() {
        return yMax - yMin;
    }

    public double getCenterX() {
        return (xMin + xMax) / 2;
    }

    public double getCenterY() {
        return (yMin + yMax) / 2;
    }
}
