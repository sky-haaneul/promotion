package org.sky.haaneul.couponservice.repository;

import jakarta.persistence.LockModeType;
import org.sky.haaneul.couponservice.domain.CouponPolicy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface CouponPolicyRepository extends JpaRepository<CouponPolicy, Long> {

    // PESSIMISTIC_WRITE -> 해당 데이터에 접근하는 트랜잭션에 대한 쓰기 잠금
    // 잠금이 설정되어 있는 동안에 다른 트랜잭션이 해당 데이터를 수정하거나 삭제하려고 하면 대기상태(blocking 상태) 됨
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT cp FROM CouponPolicy cp WHERE cp.id = :id")
    Optional<CouponPolicy> findByIdWithLock(Long id);
}
