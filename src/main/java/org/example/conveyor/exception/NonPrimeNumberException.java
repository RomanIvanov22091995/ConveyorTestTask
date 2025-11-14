package org.example.conveyor.exception;

public class NonPrimeNumberException extends RuntimeException {
    public NonPrimeNumberException(int value) {
        super(String.format("Значение должно быть простым числом: %d", value));
    }
}

