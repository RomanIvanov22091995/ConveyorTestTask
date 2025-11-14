package org.example.conveyor;

import org.example.conveyor.exception.InvalidConveyorConfigException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("ConveyorConfig")
class ConveyorConfigTest {

    @Test
    @DisplayName("Создание корректной конфигурации")
    void shouldCreateValidConfig() {
        ConveyorConfig config = ConveyorConfig.builder()
                .queueALength(5)
                .queueBLength(7)
                .addIntersection(2, 3)
                .build();

        assertEquals(5, config.getQueueALength());
        assertEquals(7, config.getQueueBLength());
        assertEquals(1, config.getIntersections().size());
    }

    @Test
    @DisplayName("Отклонение отрицательной длины очереди A")
    void shouldRejectNegativeQueueALength() {
            assertThrows(InvalidConveyorConfigException.class, () -> 
            ConveyorConfig.builder()
                    .queueALength(-1)
                    .queueBLength(5)
                    .build()
        );
    }

    @Test
    @DisplayName("Отклонение нулевой длины очереди B")
    void shouldRejectZeroQueueBLength() {
            assertThrows(InvalidConveyorConfigException.class, () -> 
            ConveyorConfig.builder()
                    .queueALength(5)
                    .queueBLength(0)
                    .build()
        );
    }

    @Test
    @DisplayName("Отклонение пересечения за границами очереди A")
    void shouldRejectIntersectionOutOfBoundsInA() {
            assertThrows(InvalidConveyorConfigException.class, () -> 
            ConveyorConfig.builder()
                    .queueALength(3)
                    .queueBLength(5)
                    .addIntersection(5, 2)
                    .build()
        );
    }

    @Test
    @DisplayName("Отклонение пересечения за границами очереди B")
    void shouldRejectIntersectionOutOfBoundsInB() {
            assertThrows(InvalidConveyorConfigException.class, () -> 
            ConveyorConfig.builder()
                    .queueALength(5)
                    .queueBLength(3)
                    .addIntersection(2, 5)
                    .build()
        );
    }

    @Test
    @DisplayName("Создание конфигурации без пересечений")
    void shouldCreateConfigWithoutIntersections() {
        ConveyorConfig config = ConveyorConfig.builder()
                .queueALength(5)
                .queueBLength(5)
                .build();

        assertTrue(config.getIntersections().isEmpty());
    }

    @Test
    @DisplayName("Создание конфигурации с множественными пересечениями")
    void shouldCreateConfigWithMultipleIntersections() {
        ConveyorConfig config = ConveyorConfig.builder()
                .queueALength(10)
                .queueBLength(10)
                .addIntersection(2, 3)
                .addIntersection(5, 7)
                .addIntersection(8, 1)
                .build();

        assertEquals(3, config.getIntersections().size());
    }
}

