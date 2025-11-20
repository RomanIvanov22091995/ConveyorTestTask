package org.example.reward.controller;

import org.example.reward.dto.RewardRecord;
import org.example.reward.dto.RewardUploadResponse;
import org.example.reward.exception.InvalidFileFormatException;
import org.example.reward.exception.InvalidRecordException;
import org.example.reward.service.CsvParserService;
import org.example.reward.service.RewardService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DefaultDataBufferFactory;
import org.springframework.http.MediaType;
import org.springframework.http.codec.multipart.FilePart;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RewardControllerTest {
    
    @Mock
    private CsvParserService csvParserService;
    
    @Mock
    private RewardService rewardService;
    
    @Mock
    private FilePart filePart;
    
    @InjectMocks
    private RewardController rewardController;
    
    @BeforeEach
    void setUp() {
        when(filePart.filename()).thenReturn("rewards.csv");
    }
    
    @Test
    void testUploadRewards_ValidCsvFile_Success() {
        String csvContent = """
            employeeId,employeeFullName,rewardId,rewardName,receivedDate
            1,Иванов Иван Иванович,100,Лучший сотрудник,2024-01-15T10:30:00
            """;
        
        DataBuffer dataBuffer = new DefaultDataBufferFactory().wrap(
            csvContent.getBytes(StandardCharsets.UTF_8)
        );
        
        when(filePart.content()).thenReturn(Flux.just(dataBuffer));
        
        RewardRecord record = new RewardRecord(
            1L, "Иванов Иван Иванович", 100L, "Лучший сотрудник",
            LocalDateTime.parse("2024-01-15T10:30:00")
        );
        
        when(csvParserService.parseCsv(any())).thenReturn(Flux.just(record));
        
        RewardUploadResponse response = new RewardUploadResponse(1, 1, 0, "Успешно");
        when(rewardService.processRewards(any())).thenReturn(Mono.just(response));
        
        StepVerifier.create(rewardController.uploadRewards(filePart))
            .assertNext(resp -> {
                assertEquals(1, resp.totalRecords());
                assertEquals(1, resp.savedRecords());
            })
            .verifyComplete();
        
        verify(csvParserService, times(1)).parseCsv(any());
        verify(rewardService, times(1)).processRewards(any());
    }
    
    @Test
    void testUploadRewards_InvalidFileExtension_ReturnsError() {
        when(filePart.filename()).thenReturn("rewards.txt");
        
        StepVerifier.create(rewardController.uploadRewards(filePart))
            .expectErrorMatches(throwable -> 
                throwable instanceof InvalidFileFormatException &&
                throwable.getMessage().contains("CSV файлы")
            )
            .verify();
        
        verify(csvParserService, never()).parseCsv(any());
        verify(rewardService, never()).processRewards(any());
    }
    
    @Test
    void testUploadRewards_ParseError_ReturnsError() {
        DataBuffer dataBuffer = new DefaultDataBufferFactory().wrap(
            "invalid content".getBytes(StandardCharsets.UTF_8)
        );
        
        when(filePart.content()).thenReturn(Flux.just(dataBuffer));
        when(csvParserService.parseCsv(any()))
            .thenReturn(Flux.error(new InvalidFileFormatException("Ошибка парсинга")));
        
        StepVerifier.create(rewardController.uploadRewards(filePart))
            .expectErrorMatches(throwable -> 
                throwable instanceof InvalidFileFormatException ||
                (throwable.getCause() instanceof InvalidFileFormatException)
            )
            .verify();
        
        verify(csvParserService, atLeastOnce()).parseCsv(any());
        verify(rewardService, never()).processRewards(any());
    }
}

