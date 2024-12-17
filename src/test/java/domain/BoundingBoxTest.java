package domain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class BoundingBoxTest {

    @Test
    void createBoundingBox() {
        // given
        double xMin = 100.0;
        double yMin = 200.0;
        double xMax = 300.0;
        double yMax = 400.0;

        // when
        BoundingBox bbox = new BoundingBox(xMin, yMin, xMax, yMax);

        // then
        assertEquals(xMin, bbox.getXMin());
        assertEquals(yMin, bbox.getYMin());
        assertEquals(xMax, bbox.getXMax());
        assertEquals(yMax, bbox.getYMax());
    }

    @Test
    void calculateWidth() {
        // given
        BoundingBox bbox = new BoundingBox(100, 200, 300, 400);

        // when
        double width = bbox.getWidth();

        // then
        assertEquals(200.0, width);
    }

    @Test
    void calculateHeight() {
        // given
        BoundingBox bbox = new BoundingBox(100, 200, 300, 400);

        // when
        double height = bbox.getHeight();

        // then
        assertEquals(200.0, height);
    }

    @Test
    void calculateCenter() {
        // given
        BoundingBox bbox = new BoundingBox(100, 200, 300, 400);

        // when
        double centerX = bbox.getCenterX();
        double centerY = bbox.getCenterY();

        // then
        assertEquals(200.0, centerX);
        assertEquals(300.0, centerY);
    }
}
