package tracking.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;


class BoundingBoxTest {

    @Test
    void shouldCreateValidBoundingBox() {
        BoundingBox bbox = new BoundingBox(10.0, 20.0, 30.0, 40.0);

        assertEquals(10.0, bbox.minX());
        assertEquals(20.0, bbox.minY());
        assertEquals(30.0, bbox.maxX());
        assertEquals(40.0, bbox.maxY());
    }

    @Test
    void shouldCalculateWidthAndHeight() {
        BoundingBox bbox = new BoundingBox(10.0, 20.0, 30.0, 40.0);

        assertEquals(20.0, bbox.width());
        assertEquals(20.0, bbox.height());
    }

    @Test
    void shouldCalculateCenter() {
        BoundingBox bbox = new BoundingBox(10.0, 20.0, 30.0, 40.0);

        assertEquals(20.0, bbox.centerX());
        assertEquals(30.0, bbox.centerY());
    }

    @Test
    void shouldCalculateArea() {
        BoundingBox bbox = new BoundingBox(10.0, 20.0, 30.0, 40.0);

        assertEquals(400.0, bbox.area());
    }

    @Test
    void shouldThrowExceptionForInvalidXCoordinates() {
        assertThrows(IllegalArgumentException.class, () ->
            new BoundingBox(30.0, 20.0, 10.0, 40.0)
        );
    }

    @Test
    void shouldThrowExceptionForInvalidYCoordinates() {
        assertThrows(IllegalArgumentException.class, () ->
            new BoundingBox(10.0, 40.0, 30.0, 20.0)
        );
    }

    @Test
    void shouldAllowEqualCoordinates() {
        BoundingBox bbox = new BoundingBox(10.0, 20.0, 10.0, 20.0);

        assertEquals(0.0, bbox.width());
        assertEquals(0.0, bbox.height());
        assertEquals(0.0, bbox.area());
    }
}