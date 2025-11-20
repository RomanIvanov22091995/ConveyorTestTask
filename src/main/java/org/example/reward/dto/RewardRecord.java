package org.example.reward.dto;

import java.time.LocalDateTime;

public record RewardRecord(
    Long employeeId,
    String employeeFullName,
    Long rewardId,
    String rewardName,
    LocalDateTime receivedDate
) {
}

