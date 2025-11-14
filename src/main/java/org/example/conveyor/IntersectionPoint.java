package org.example.conveyor;

import lombok.Getter;
import org.example.conveyor.exception.InvalidIntersectionPointException;

@Getter
public class IntersectionPoint {
    private final Integer positionInA;
    private final Integer positionInB;

    public IntersectionPoint(Integer positionInA, Integer positionInB) {
        if (positionInA == null || positionInB == null || positionInA < 0 || positionInB < 0) {
            throw new InvalidIntersectionPointException("Позиции должны быть неотрицательными");
        }
        this.positionInA = positionInA;
        this.positionInB = positionInB;
    }
}
