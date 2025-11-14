package org.example.conveyor;

import org.example.conveyor.exception.InvalidIntersectionPointException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("IntersectionPoint")
class IntersectionPointTest {

    @Test
    @DisplayName("Создание корректной точки пересечения")
    void shouldCreateValidIntersectionPoint() {
        IntersectionPoint point = new IntersectionPoint(3, 5);

        assertEquals(3, point.getPositionInA());
        assertEquals(5, point.getPositionInB());
    }

    @Test
    @DisplayName("Отклонение отрицательной позиции в очереди A")
    void shouldRejectNegativePositionInA() {
        assertThrows(InvalidIntersectionPointException.class, 
            () -> new IntersectionPoint(-1, 5));
    }

    @Test
    @DisplayName("Отклонение отрицательной позиции в очереди B")
    void shouldRejectNegativePositionInB() {
        assertThrows(InvalidIntersectionPointException.class, 
            () -> new IntersectionPoint(3, -1));
    }

    @Test
    @DisplayName("Принятие нулевых позиций")
    void shouldAcceptZeroPositions() {
        assertDoesNotThrow(() -> new IntersectionPoint(0, 0));
    }
}

