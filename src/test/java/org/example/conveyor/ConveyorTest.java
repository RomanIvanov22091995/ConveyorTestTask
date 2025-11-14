package org.example.conveyor;

import org.example.conveyor.exception.NonPrimeNumberException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Conveyor")
class ConveyorTest {

    @Nested
    @DisplayName("Базовые операции")
    class BasicOperations {

        @Test
        @DisplayName("Создание конвейера с корректной конфигурацией")
        void shouldCreateConveyorWithValidConfig() {
            ConveyorConfig config = ConveyorConfig.builder()
                    .queueALength(5)
                    .queueBLength(5)
                    .build();

            Conveyor conveyor = new Conveyor(config);

            assertEquals(5, conveyor.getQueueALength());
            assertEquals(5, conveyor.getQueueBLength());
        }

        @Test
        @DisplayName("Проталкивание простого числа в очередь A")
        void shouldPushPrimeToQueueA() {
            ConveyorConfig config = ConveyorConfig.builder()
                    .queueALength(3)
                    .queueBLength(3)
                    .build();
            Conveyor conveyor = new Conveyor(config);

            int result = conveyor.pushA(2);

            assertEquals(0, result);
            assertEquals(Arrays.asList(2, null, null), conveyor.getQueueAState());
        }

        @Test
        @DisplayName("Проталкивание простого числа в очередь B")
        void shouldPushPrimeToQueueB() {
            ConveyorConfig config = ConveyorConfig.builder()
                    .queueALength(3)
                    .queueBLength(3)
                    .build();
            Conveyor conveyor = new Conveyor(config);

            int result = conveyor.pushB(3);

            assertEquals(0, result);
            assertEquals(Arrays.asList(3, null, null), conveyor.getQueueBState());
        }

        @Test
        @DisplayName("Выталкивание числа при заполненной очереди")
        void shouldOutputValueWhenQueueFull() {
            ConveyorConfig config = ConveyorConfig.builder()
                    .queueALength(3)
                    .queueBLength(3)
                    .build();
            Conveyor conveyor = new Conveyor(config);

            conveyor.pushA(2);
            conveyor.pushA(3);
            conveyor.pushA(5);
            int result = conveyor.pushA(7);

            assertEquals(2, result);
            assertEquals(Arrays.asList(7, 5, 3), conveyor.getQueueAState());
        }
    }

    @Nested
    @DisplayName("Работа с пересечениями")
    class IntersectionOperations {

        @Test
        @DisplayName("Пересечение обновляется при проталкивании в очередь A")
        void shouldUpdateIntersectionWhenPushingToA() {
            ConveyorConfig config = ConveyorConfig.builder()
                    .queueALength(5)
                    .queueBLength(5)
                    .addIntersection(2, 2)
                    .build();
            Conveyor conveyor = new Conveyor(config);

            conveyor.pushA(2);
            conveyor.pushA(3);
            conveyor.pushA(5);

            List<Integer> stateA = conveyor.getQueueAState();
            List<Integer> stateB = conveyor.getQueueBState();

            assertEquals(2, stateA.get(2));
            assertEquals(2, stateB.get(2));
        }

        @Test
        @DisplayName("Пересечение обновляется при проталкивании в очередь B")
        void shouldUpdateIntersectionWhenPushingToB() {
            ConveyorConfig config = ConveyorConfig.builder()
                    .queueALength(5)
                    .queueBLength(5)
                    .addIntersection(2, 2)
                    .build();
            Conveyor conveyor = new Conveyor(config);

            conveyor.pushB(7);
            conveyor.pushB(11);
            conveyor.pushB(13);

            List<Integer> stateA = conveyor.getQueueAState();
            List<Integer> stateB = conveyor.getQueueBState();

            assertEquals(7, stateA.get(2));
            assertEquals(7, stateB.get(2));
        }

        @Test
        @DisplayName("Множественные пересечения работают корректно")
        void shouldHandleMultipleIntersections() {
            ConveyorConfig config = ConveyorConfig.builder()
                    .queueALength(6)
                    .queueBLength(6)
                    .addIntersection(2, 1)
                    .addIntersection(4, 3)
                    .build();
            Conveyor conveyor = new Conveyor(config);

            conveyor.pushA(2);
            conveyor.pushA(3);
            conveyor.pushA(5);

            List<Integer> stateA = conveyor.getQueueAState();
            List<Integer> stateB = conveyor.getQueueBState();

            assertEquals(2, stateA.get(2));
            assertEquals(2, stateB.get(1));
            assertNull(stateA.get(4));
            assertNull(stateB.get(3));
        }

        @Test
        @DisplayName("Сложный сценарий с чередованием очередей")
        void shouldHandleComplexScenarioWithAlternatingQueues() {
            ConveyorConfig config = ConveyorConfig.builder()
                    .queueALength(5)
                    .queueBLength(5)
                    .addIntersection(2, 3)
                    .build();
            Conveyor conveyor = new Conveyor(config);

            conveyor.pushA(2);
            conveyor.pushB(3);
            conveyor.pushA(5);
            conveyor.pushB(7);

            List<Integer> stateA = conveyor.getQueueAState();
            List<Integer> stateB = conveyor.getQueueBState();

            assertEquals(Arrays.asList(5, 2, null, null, null), stateA);
            assertEquals(Arrays.asList(7, 3, null, null, null), stateB);
        }
    }

    @Nested
    @DisplayName("Валидация")
    class Validation {

        @Test
        @DisplayName("Отклонение непростого числа в очереди A")
        void shouldRejectNonPrimeInQueueA() {
            ConveyorConfig config = ConveyorConfig.builder()
                    .queueALength(3)
                    .queueBLength(3)
                    .build();
            Conveyor conveyor = new Conveyor(config);

            assertThrows(NonPrimeNumberException.class, () -> conveyor.pushA(4));
            assertThrows(NonPrimeNumberException.class, () -> conveyor.pushA(1));
            assertThrows(NonPrimeNumberException.class, () -> conveyor.pushA(0));
        }

        @Test
        @DisplayName("Отклонение непростого числа в очереди B")
        void shouldRejectNonPrimeInQueueB() {
            ConveyorConfig config = ConveyorConfig.builder()
                    .queueALength(3)
                    .queueBLength(3)
                    .build();
            Conveyor conveyor = new Conveyor(config);

            assertThrows(NonPrimeNumberException.class, () -> conveyor.pushB(9));
            assertThrows(NonPrimeNumberException.class, () -> conveyor.pushB(-5));
        }

        @Test
        @DisplayName("Принятие простых чисел")
        void shouldAcceptPrimeNumbers() {
            ConveyorConfig config = ConveyorConfig.builder()
                    .queueALength(10)
                    .queueBLength(10)
                    .build();
            Conveyor conveyor = new Conveyor(config);

            assertDoesNotThrow(() -> {
                conveyor.pushA(2);
                conveyor.pushA(3);
                conveyor.pushA(5);
                conveyor.pushA(7);
                conveyor.pushA(11);
                conveyor.pushA(13);
                conveyor.pushA(17);
                conveyor.pushA(19);
                conveyor.pushA(23);
            });
        }
    }

    @Nested
    @DisplayName("Граничные случаи")
    class EdgeCases {

        @Test
        @DisplayName("Работа с минимальной очередью")
        void shouldHandleMinimalQueue() {
            ConveyorConfig config = ConveyorConfig.builder()
                    .queueALength(1)
                    .queueBLength(1)
                    .build();
            Conveyor conveyor = new Conveyor(config);

            int result1 = conveyor.pushA(2);
            int result2 = conveyor.pushA(3);

            assertEquals(0, result1);
            assertEquals(2, result2);
        }

        @Test
        @DisplayName("Пересечение в начале очередей")
        void shouldHandleIntersectionAtStart() {
            ConveyorConfig config = ConveyorConfig.builder()
                    .queueALength(3)
                    .queueBLength(3)
                    .addIntersection(0, 0)
                    .build();
            Conveyor conveyor = new Conveyor(config);

            conveyor.pushA(2);
            
            assertEquals(2, conveyor.getQueueAState().get(0));
            assertEquals(2, conveyor.getQueueBState().get(0));
        }

        @Test
        @DisplayName("Пересечение в конце очередей")
        void shouldHandleIntersectionAtEnd() {
            ConveyorConfig config = ConveyorConfig.builder()
                    .queueALength(4)
                    .queueBLength(4)
                    .addIntersection(3, 3)
                    .build();
            Conveyor conveyor = new Conveyor(config);

            conveyor.pushA(2);
            conveyor.pushA(3);
            conveyor.pushA(5);
            conveyor.pushA(7);

            assertEquals(2, conveyor.getQueueAState().get(3));
            assertEquals(2, conveyor.getQueueBState().get(3));
        }

        @Test
        @DisplayName("Полное заполнение обеих очередей")
        void shouldHandleFullQueues() {
            ConveyorConfig config = ConveyorConfig.builder()
                    .queueALength(3)
                    .queueBLength(3)
                    .addIntersection(1, 1)
                    .build();
            Conveyor conveyor = new Conveyor(config);

            conveyor.pushA(2);
            conveyor.pushA(3);
            conveyor.pushA(5);
            
            conveyor.pushB(7);
            conveyor.pushB(11);
            conveyor.pushB(13);

            assertNotNull(conveyor.getQueueAState().get(0));
            assertNotNull(conveyor.getQueueBState().get(0));
        }
    }
}

