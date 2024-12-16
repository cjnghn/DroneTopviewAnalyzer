package tracking.model;

public record BoundingBox(
    double minX,
    double minY,
    double maxX,
    double maxY
) {
    public BoundingBox {
        if (minX > maxX) {
            throw new IllegalArgumentException("MinX must be less than or equal to MaxX");
        }
        if (minY > maxY) {
            throw new IllegalArgumentException("MinY must be less than or equal to MaxY");
        }
    }

    public double width() {
        return maxX - minX;
    }

    public double height() {
        return maxY - minY;
    }

    public double centerX() {
        return (minX + maxX) / 2.0;
    }

    public double centerY() {
        return (minY + maxY) / 2.0;
    }

    public double area() {
        return width() * height();
    }
}