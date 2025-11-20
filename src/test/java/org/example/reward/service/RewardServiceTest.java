package org.example.reward.service;

import org.example.reward.dto.RewardRecord;
import org.example.reward.entity.Employee;
import org.example.reward.entity.Reward;
import org.example.reward.repository.EmployeeRepository;
import org.example.reward.repository.RewardRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RewardServiceTest {
    
    @Mock
    private EmployeeRepository employeeRepository;
    
    @Mock
    private RewardRepository rewardRepository;
    
    @InjectMocks
    private RewardService rewardService;
    
    private RewardRecord validRecord;
    private Reward savedReward;
    
    @BeforeEach
    void setUp() {
        validRecord = new RewardRecord(
            1L,
            "Иванов Иван Иванович",
            100L,
            "Лучший сотрудник",
            LocalDateTime.parse("2024-01-15T10:30:00")
        );
        
        savedReward = new Reward();
        savedReward.setId(1L);
        savedReward.setEmployeeId(1L);
        savedReward.setRewardId(100L);
        savedReward.setRewardName("Лучший сотрудник");
        savedReward.setReceivedDate(LocalDateTime.parse("2024-01-15T10:30:00"));
    }
    
    @Test
    void testProcessRewards_EmployeeExists_SavesReward() {
        when(employeeRepository.existsById(1L)).thenReturn(Mono.just(true));
        when(rewardRepository.save(any(Reward.class))).thenReturn(Mono.just(savedReward));
        
        Flux<RewardRecord> records = Flux.just(validRecord);
        
        StepVerifier.create(rewardService.processRewards(records))
            .assertNext(response -> {
                assertEquals(1, response.totalRecords());
                assertEquals(1, response.savedRecords());
                assertEquals(0, response.skippedRecords());
                assertTrue(response.message().contains("сохранено: 1"));
            })
            .verifyComplete();
        
        verify(employeeRepository, times(1)).existsById(1L);
        verify(rewardRepository, times(1)).save(any(Reward.class));
    }
    
    @Test
    void testProcessRewards_EmployeeNotExists_SkipsReward() {
        when(employeeRepository.existsById(1L)).thenReturn(Mono.just(false));
        
        Flux<RewardRecord> records = Flux.just(validRecord);
        
        StepVerifier.create(rewardService.processRewards(records))
            .assertNext(response -> {
                assertEquals(1, response.totalRecords());
                assertEquals(0, response.savedRecords());
                assertEquals(1, response.skippedRecords());
                assertTrue(response.message().contains("пропущено: 1"));
            })
            .verifyComplete();
        
        verify(employeeRepository, times(1)).existsById(1L);
        verify(rewardRepository, never()).save(any(Reward.class));
    }
    
    @Test
    void testProcessRewards_MultipleRecords_MixedResults() {
        RewardRecord record1 = new RewardRecord(1L, "Иванов", 100L, "Награда 1", LocalDateTime.now());
        RewardRecord record2 = new RewardRecord(2L, "Петров", 101L, "Награда 2", LocalDateTime.now());
        RewardRecord record3 = new RewardRecord(999L, "Несуществующий", 102L, "Награда 3", LocalDateTime.now());
        
        when(employeeRepository.existsById(1L)).thenReturn(Mono.just(true));
        when(employeeRepository.existsById(2L)).thenReturn(Mono.just(true));
        when(employeeRepository.existsById(999L)).thenReturn(Mono.just(false));
        
        when(rewardRepository.save(any(Reward.class)))
            .thenReturn(Mono.just(new Reward(1L, 1L, 100L, "Награда 1", LocalDateTime.now())))
            .thenReturn(Mono.just(new Reward(2L, 2L, 101L, "Награда 2", LocalDateTime.now())));
        
        Flux<RewardRecord> records = Flux.just(record1, record2, record3);
        
        StepVerifier.create(rewardService.processRewards(records))
            .assertNext(response -> {
                assertEquals(3, response.totalRecords());
                assertEquals(2, response.savedRecords());
                assertEquals(1, response.skippedRecords());
            })
            .verifyComplete();
        
        verify(employeeRepository, times(1)).existsById(1L);
        verify(employeeRepository, times(1)).existsById(2L);
        verify(employeeRepository, times(1)).existsById(999L);
        verify(rewardRepository, times(2)).save(any(Reward.class));
    }
    
    @Test
    void testProcessRewards_EmptyFlux_ReturnsZeroCounts() {
        Flux<RewardRecord> emptyRecords = Flux.empty();
        
        StepVerifier.create(rewardService.processRewards(emptyRecords))
            .assertNext(response -> {
                assertEquals(0, response.totalRecords());
                assertEquals(0, response.savedRecords());
                assertEquals(0, response.skippedRecords());
            })
            .verifyComplete();
        
        verify(employeeRepository, never()).existsById(anyLong());
        verify(rewardRepository, never()).save(any(Reward.class));
    }
}

