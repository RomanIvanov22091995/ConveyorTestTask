package org.example;

import org.example.conveyor.Conveyor;
import org.example.conveyor.ConveyorConfig;

public class Main {
    public static void main(String[] args) {
        System.out.println("=== Демонстрация работы конвейера с пересекающимися очередями ===\n");

        ConveyorConfig config = ConveyorConfig.builder()
                .queueALength(7)
                .queueBLength(7)
                .addIntersection(2, 3)
                .addIntersection(5, 1)
                .build();

        Conveyor conveyor = new Conveyor(config);

        System.out.println("Конфигурация:");
        System.out.println("  Длина очереди A: " + config.getQueueALength());
        System.out.println("  Длина очереди B: " + config.getQueueBLength());
        System.out.println("  Точки пересечения:");
        config.getIntersections().forEach(intersection -> 
            System.out.println("    - Позиция в A: " + intersection.getPositionInA() + 
                             ", Позиция в B: " + intersection.getPositionInB())
        );

        System.out.println("\n--- Сценарий 1: Проталкивание в очередь A ---");
        int[] primesForA = {2, 3, 5, 7, 11};
        for (int prime : primesForA) {
            int output = conveyor.pushA(prime);
            System.out.printf("PushA(%d) -> Выход: %d%n", prime, output);
            System.out.println("  Состояние A: " + conveyor.getQueueAState());
            System.out.println("  Состояние B: " + conveyor.getQueueBState());
            System.out.println();
        }

        System.out.println("--- Сценарий 2: Проталкивание в очередь B ---");
        int[] primesForB = {13, 17, 19};
        for (int prime : primesForB) {
            int output = conveyor.pushB(prime);
            System.out.printf("PushB(%d) -> Выход: %d%n", prime, output);
            System.out.println("  Состояние A: " + conveyor.getQueueAState());
            System.out.println("  Состояние B: " + conveyor.getQueueBState());
            System.out.println();
        }

        System.out.println("--- Сценарий 3: Переполнение очереди ---");
        ConveyorConfig smallConfig = ConveyorConfig.builder()
                .queueALength(3)
                .queueBLength(3)
                .addIntersection(1, 1)
                .build();
        
        Conveyor smallConveyor = new Conveyor(smallConfig);
        
        System.out.println("Заполняем очередь A длиной 3:");
        int[] morePrimes = {2, 3, 5, 7};
        for (int prime : morePrimes) {
            int output = smallConveyor.pushA(prime);
            System.out.printf("PushA(%d) -> Выход: %d, Состояние A: %s%n", 
                prime, output, smallConveyor.getQueueAState());
        }

        System.out.println("\n--- Сценарий 4: Демонстрация общего узла ---");
        System.out.println("Проталкиваем в A, затем в B через общий узел:");
        ConveyorConfig sharedConfig = ConveyorConfig.builder()
                .queueALength(5)
                .queueBLength(5)
                .addIntersection(2, 2)
                .build();
        
        Conveyor sharedConveyor = new Conveyor(sharedConfig);
        
        sharedConveyor.pushA(2);
        sharedConveyor.pushA(3);
        sharedConveyor.pushA(5);
        System.out.println("После 3 push в A:");
        System.out.println("  A: " + sharedConveyor.getQueueAState());
        System.out.println("  B: " + sharedConveyor.getQueueBState());
        
        sharedConveyor.pushB(7);
        sharedConveyor.pushB(11);
        sharedConveyor.pushB(13);
        System.out.println("\nПосле 3 push в B:");
        System.out.println("  A: " + sharedConveyor.getQueueAState());
        System.out.println("  B: " + sharedConveyor.getQueueBState());

        System.out.println("\n=== Демонстрация завершена ===");
    }
}
