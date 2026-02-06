package org.sky.haaneul.pointservice.repository;

import jakarta.persistence.LockModeType;
import org.sky.haaneul.pointservice.domain.PointBalance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;

import java.util.Optional;

public interface PointBalanceRepository extends JpaRepository<PointBalance, Long> {

    @Lock(LockModeType.OPTIMISTIC)
    Optional<PointBalance> findByUserId(Long userId);
}
