package org.example.reward.controller;

import lombok.extern.slf4j.Slf4j;
import org.example.reward.exception.InvalidFileFormatException;
import org.example.reward.exception.InvalidRecordException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;


@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {
    
    @ExceptionHandler(InvalidFileFormatException.class)
    public ResponseEntity<Map<String, String>> handleInvalidFileFormat(InvalidFileFormatException e) {
        log.error("Ошибка формата файла: {}", e.getMessage());
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(Map.of("error", "Неверный формат файла", "message", e.getMessage()));
    }
    
    @ExceptionHandler(InvalidRecordException.class)
    public ResponseEntity<Map<String, String>> handleInvalidRecord(InvalidRecordException e) {
        log.error("Ошибка в записи: {}", e.getMessage());
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(Map.of("error", "Неверный формат записи", "message", e.getMessage()));
    }
    
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleGenericException(Exception e) {
        log.error("Неожиданная ошибка", e);
        return ResponseEntity
            .status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(Map.of("error", "Внутренняя ошибка сервера", "message", e.getMessage()));
    }
}

