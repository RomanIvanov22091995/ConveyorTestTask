package org.example.conveyor;

import lombok.Getter;
import org.example.conveyor.exception.InvalidConveyorConfigException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Getter
public class ConveyorConfig {
    private final int queueALength;
    private final int queueBLength;
    private final List<IntersectionPoint> intersections;

    private ConveyorConfig(Builder builder) {
        this.queueALength = builder.queueALength;
        this.queueBLength = builder.queueBLength;
        this.intersections = Collections.unmodifiableList(new ArrayList<>(builder.intersections));
        validate();
    }

    private void validate() {
        if (queueALength <= 0 || queueBLength <= 0) {
            throw new InvalidConveyorConfigException("Длины очередей должны быть положительными");
        }

        for (IntersectionPoint intersection : intersections) {
            if (intersection.getPositionInA() >= queueALength) {
                throw new InvalidConveyorConfigException(
                    String.format("Позиция пересечения в очереди A (%d) выходит за границы очереди (длина: %d)",
                        intersection.getPositionInA(), queueALength)
                );
            }
            if (intersection.getPositionInB() >= queueBLength) {
                throw new InvalidConveyorConfigException(
                    String.format("Позиция пересечения в очереди B (%d) выходит за границы очереди (длина: %d)",
                        intersection.getPositionInB(), queueBLength)
                );
            }
        }
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private int queueALength;
        private int queueBLength;
        private final List<IntersectionPoint> intersections = new ArrayList<>();

        public Builder queueALength(int length) {
            this.queueALength = length;
            return this;
        }

        public Builder queueBLength(int length) {
            this.queueBLength = length;
            return this;
        }

        public Builder addIntersection(int positionInA, int positionInB) {
            this.intersections.add(new IntersectionPoint(positionInA, positionInB));
            return this;
        }

        public Builder addIntersections(List<IntersectionPoint> intersections) {
            this.intersections.addAll(intersections);
            return this;
        }

        public ConveyorConfig build() {
            return new ConveyorConfig(this);
        }
    }
}
