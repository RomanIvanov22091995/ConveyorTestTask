package org.example.reward.repository;

import org.example.reward.entity.Reward;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;


public interface RewardRepository extends R2dbcRepository<Reward, Long> {
}

