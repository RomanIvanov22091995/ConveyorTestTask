package org.example.conveyor;

import lombok.Getter;
import org.example.conveyor.exception.InvalidIntersectionPointException;

@Getter
public class IntersectionPoint {
    private final int positionInA;
    private final int positionInB;

    public IntersectionPoint(int positionInA, int positionInB) {
        if (positionInA < 0 || positionInB < 0) {
            throw new InvalidIntersectionPointException("Позиции должны быть неотрицательными");
        }
        this.positionInA = positionInA;
        this.positionInB = positionInB;
    }
}
