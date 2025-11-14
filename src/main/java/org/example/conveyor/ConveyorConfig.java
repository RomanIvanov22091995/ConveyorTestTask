package org.example.conveyor;

import lombok.Getter;
import org.example.conveyor.exception.InvalidConveyorConfigException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Getter
public class ConveyorConfig {
    private final Integer queueALength;
    private final Integer queueBLength;
    private final List<IntersectionPoint> intersections;

    private ConveyorConfig(Builder builder) {
        this.queueALength = builder.queueALength;
        this.queueBLength = builder.queueBLength;
        this.intersections = Collections.unmodifiableList(new ArrayList<>(builder.intersections));
        validate();
    }

    private void validate() {
        validateQueueLengths();
        validateIntersections();
    }

    private void validateQueueLengths() {
        if (!isValidLength(queueALength) || !isValidLength(queueBLength)) {
            throw new InvalidConveyorConfigException("Длины очередей должны быть положительными");
        }
    }

    private boolean isValidLength(Integer length) {
        return length != null && length > 0;
    }

    private void validateIntersections() {
        intersections.forEach(this::validateIntersection);
    }

    private void validateIntersection(IntersectionPoint intersection) {
        validateIntersectionPosition(intersection.getPositionInA(), queueALength, "A");
        validateIntersectionPosition(intersection.getPositionInB(), queueBLength, "B");
    }

    private void validateIntersectionPosition(Integer position, Integer queueLength, String queueName) {
        if (position >= queueLength) {
            throw new InvalidConveyorConfigException(
                String.format("Позиция пересечения в очереди %s (%d) выходит за границы очереди (длина: %d)",
                    queueName, position, queueLength)
            );
        }
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Integer queueALength;
        private Integer queueBLength;
        private final List<IntersectionPoint> intersections = new ArrayList<>();

        public Builder queueALength(Integer length) {
            this.queueALength = length;
            return this;
        }

        public Builder queueBLength(Integer length) {
            this.queueBLength = length;
            return this;
        }

        public Builder addIntersection(Integer positionInA, Integer positionInB) {
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
