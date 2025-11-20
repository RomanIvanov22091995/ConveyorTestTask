package org.example.reward.dto;

public record RewardUploadResponse(
    Integer totalRecords,
    Integer savedRecords,
    Integer skippedRecords,
    String message
) {
}

