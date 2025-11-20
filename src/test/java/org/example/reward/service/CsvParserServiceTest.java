package org.example.reward.service;

import org.example.reward.dto.RewardRecord;
import org.example.reward.exception.InvalidFileFormatException;
import org.example.reward.exception.InvalidRecordException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import reactor.test.StepVerifier;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CsvParserServiceTest {
    
    private CsvParserService csvParserService;
    
    @BeforeEach
    void setUp() {
        csvParserService = new CsvParserService();
    }
    
    @Test
    void testParseValidCsv() {
        String csvContent = """
            employeeId,employeeFullName,rewardId,rewardName,receivedDate
            1,Иванов Иван Иванович,100,Лучший сотрудник,2024-01-15T10:30:00
            2,Петров Петр Петрович,101,За отличную работу,2024-02-20T14:45:00
            """;
        
        InputStream inputStream = new ByteArrayInputStream(csvContent.getBytes(StandardCharsets.UTF_8));
        
        StepVerifier.create(csvParserService.parseCsv(inputStream))
            .assertNext(record -> {
                assertEquals(1L, record.employeeId());
                assertEquals("Иванов Иван Иванович", record.employeeFullName());
                assertEquals(100L, record.rewardId());
                assertEquals("Лучший сотрудник", record.rewardName());
                assertEquals(LocalDateTime.parse("2024-01-15T10:30:00"), record.receivedDate());
            })
            .assertNext(record -> {
                assertEquals(2L, record.employeeId());
                assertEquals("Петров Петр Петрович", record.employeeFullName());
                assertEquals(101L, record.rewardId());
                assertEquals("За отличную работу", record.rewardName());
                assertEquals(LocalDateTime.parse("2024-02-20T14:45:00"), record.receivedDate());
            })
            .verifyComplete();
    }
    
    @Test
    void testParseCsvWithInvalidColumnCount() {
        String csvContent = """
            employeeId,employeeFullName,rewardId,rewardName,receivedDate
            1,Иванов Иван Иванович,100,Лучший сотрудник
            """;
        
        InputStream inputStream = new ByteArrayInputStream(csvContent.getBytes(StandardCharsets.UTF_8));
        
        StepVerifier.create(csvParserService.parseCsv(inputStream))
            .expectErrorMatches(throwable -> 
                throwable instanceof InvalidRecordException &&
                throwable.getMessage().contains("Неверное количество колонок")
            )
            .verify();
    }
    
    @Test
    void testParseCsvWithInvalidDate() {
        String csvContent = """
            employeeId,employeeFullName,rewardId,rewardName,receivedDate
            1,Иванов Иван Иванович,100,Лучший сотрудник,invalid-date
            """;
        
        InputStream inputStream = new ByteArrayInputStream(csvContent.getBytes(StandardCharsets.UTF_8));
        
        StepVerifier.create(csvParserService.parseCsv(inputStream))
            .expectErrorMatches(throwable -> 
                throwable instanceof InvalidRecordException &&
                throwable.getMessage().contains("Неверный формат даты")
            )
            .verify();
    }
    
    @Test
    void testParseCsvWithInvalidEmployeeId() {
        String csvContent = """
            employeeId,employeeFullName,rewardId,rewardName,receivedDate
            invalid,Иванов Иван Иванович,100,Лучший сотрудник,2024-01-15T10:30:00
            """;
        
        InputStream inputStream = new ByteArrayInputStream(csvContent.getBytes(StandardCharsets.UTF_8));
        
        StepVerifier.create(csvParserService.parseCsv(inputStream))
            .expectErrorMatches(throwable -> 
                throwable instanceof InvalidRecordException &&
                throwable.getMessage().contains("employeeId")
            )
            .verify();
    }
    
    @Test
    void testParseCsvWithEmptyFields() {
        String csvContent = """
            employeeId,employeeFullName,rewardId,rewardName,receivedDate
            ,Иванов Иван Иванович,100,Лучший сотрудник,2024-01-15T10:30:00
            """;
        
        InputStream inputStream = new ByteArrayInputStream(csvContent.getBytes(StandardCharsets.UTF_8));
        
        StepVerifier.create(csvParserService.parseCsv(inputStream))
            .expectErrorMatches(throwable -> 
                throwable instanceof InvalidRecordException &&
                throwable.getMessage().contains("employeeId")
            )
            .verify();
    }
    
    @Test
    void testParseCsvWithNegativeId() {
        String csvContent = """
            employeeId,employeeFullName,rewardId,rewardName,receivedDate
            -1,Иванов Иван Иванович,100,Лучший сотрудник,2024-01-15T10:30:00
            """;
        
        InputStream inputStream = new ByteArrayInputStream(csvContent.getBytes(StandardCharsets.UTF_8));
        
        StepVerifier.create(csvParserService.parseCsv(inputStream))
            .expectErrorMatches(throwable -> 
                throwable instanceof InvalidRecordException &&
                throwable.getMessage().contains("положительным числом")
            )
            .verify();
    }
}

