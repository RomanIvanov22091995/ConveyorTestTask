package org.example.reward.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;


@Data
@NoArgsConstructor
@AllArgsConstructor
@Table("rewards")
public class Reward {
    
    @Id
    private Long id;
    
    private Long employeeId;
    
    private Long rewardId;
    
    private String rewardName;
    
    private LocalDateTime receivedDate;
}

