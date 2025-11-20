package org.example.reward.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.reward.dto.RewardRecord;
import org.example.reward.dto.RewardUploadResponse;
import org.example.reward.entity.Reward;
import org.example.reward.repository.EmployeeRepository;
import org.example.reward.repository.RewardRepository;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.concurrent.atomic.AtomicInteger;


@Slf4j
@Service
@RequiredArgsConstructor
public class RewardService {
    
    private final EmployeeRepository employeeRepository;
    private final RewardRepository rewardRepository;
    
    /**
     * Обрабатывает записи о наградах и сохраняет только те, для которых сотрудник существует в БД.
     * 
     * @param records поток записей о наградах
     * @return Mono<RewardUploadResponse> результат обработки
     */
    public Mono<RewardUploadResponse> processRewards(Flux<RewardRecord> records) {
        AtomicInteger totalRecords = new AtomicInteger(0);
        AtomicInteger savedRecords = new AtomicInteger(0);
        AtomicInteger skippedRecords = new AtomicInteger(0);
        
        return records
            .doOnNext(record -> totalRecords.incrementAndGet())
            .flatMap(record -> 
                employeeRepository.existsById(record.employeeId())
                    .flatMap(exists -> {
                        if (exists) {
                            return saveReward(record)
                                .doOnSuccess(r -> savedRecords.incrementAndGet())
                                .thenReturn(true);
                        } else {
                            log.warn("Сотрудник с ID {} не найден в БД. Награда пропущена.", record.employeeId());
                            skippedRecords.incrementAndGet();
                            return Mono.just(false);
                        }
                    })
            )
            .then(Mono.fromCallable(() -> {
                Integer total = totalRecords.get();
                Integer saved = savedRecords.get();
                Integer skipped = skippedRecords.get();
                
                String message = String.format(
                    "Обработано записей: %d, сохранено: %d, пропущено: %d",
                    total, saved, skipped
                );
                
                return new RewardUploadResponse(total, saved, skipped, message);
            }));
    }
    

    private Mono<Reward> saveReward(RewardRecord record) {
        Reward reward = new Reward();
        reward.setEmployeeId(record.employeeId());
        reward.setRewardId(record.rewardId());
        reward.setRewardName(record.rewardName());
        reward.setReceivedDate(record.receivedDate());
        
        return rewardRepository.save(reward)
            .doOnSuccess(r -> log.debug("Награда сохранена: employeeId={}, rewardId={}", 
                r.getEmployeeId(), r.getRewardId()));
    }
}

