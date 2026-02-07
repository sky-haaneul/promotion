package org.sky.haaneul.pointservice.service.v1;

import lombok.RequiredArgsConstructor;
import org.sky.haaneul.pointservice.aop.PointMetered;
import org.sky.haaneul.pointservice.domain.Point;
import org.sky.haaneul.pointservice.domain.PointBalance;
import org.sky.haaneul.pointservice.domain.PointType;
import org.sky.haaneul.pointservice.repository.PointBalanceRepository;
import org.sky.haaneul.pointservice.repository.PointRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PointService {
    private final PointBalanceRepository pointBalanceRepository;
    private final PointRepository pointRepository;

    // 포인트 획득
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    @PointMetered(version = "v1")
    public Point earnPoints(Long userId, Long amount, String description) {
        PointBalance pointBalance = pointBalanceRepository.findByUserId(userId)
                .orElseGet(() -> PointBalance.builder()
                        .userId(userId)
                        .balance(0L)
                        .build());

        pointBalance.addBalance(amount);
        pointBalance = pointBalanceRepository.save(pointBalance);

        // 포인트 내역 추가
        Point point = Point.builder()
                .userId(userId)
                .amount(amount)
                .type(PointType.EARNED)
                .description(description)
                .balanceSnapshot(pointBalance.getBalance())
                .pointBalance(pointBalance)
                .build();

        return pointRepository.save(point);
    }

    // 포인트 사용
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    @PointMetered(version = "v1")
    public Point usePoints(Long userId, Long amount, String description) {
        PointBalance pointBalance = pointBalanceRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        pointBalance.subtractBalance(amount);
        pointBalance = pointBalanceRepository.save(pointBalance);

        Point point = Point.builder()
                .userId(userId)
                .amount(amount)
                .type(PointType.USED)
                .description(description)
                .balanceSnapshot(pointBalance.getBalance())
                .pointBalance(pointBalance)
                .build();

        return pointRepository.save(point);
    }

    // 포인트 취소
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    @PointMetered(version = "v1")
    public Point cancelPoints(Long pointId, String description) {
        Point originalPoint = pointRepository.findById(pointId)
                .orElseThrow(() -> new IllegalArgumentException("Point not found"));

        // 2번 취소 방지
        if (originalPoint.getType() == PointType.CANCELED) {
            throw new IllegalArgumentException("Already canceled point");
        }

        PointBalance pointBalance = pointBalanceRepository.findByUserId(originalPoint.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        Long currentBalance = pointBalance.getBalance();
        Long newBalance;

        if (originalPoint.getType() == PointType.EARNED) {
            if (currentBalance < originalPoint.getAmount()) {
                throw new IllegalArgumentException("Cannot cancel earned points: insufficient balance");
            }
            newBalance = currentBalance - originalPoint.getAmount();
        } else if (originalPoint.getType() == PointType.USED) { // 이미 사용된 경우
            newBalance = currentBalance + originalPoint.getAmount();
        } else {
            throw new IllegalArgumentException("Invalid point type for cancellation");
        }

        pointBalance.setBalance(newBalance);
        pointBalance = pointBalanceRepository.save(pointBalance);

        Point cancelPoint = Point.builder()
                .userId(originalPoint.getUserId())
                .amount(originalPoint.getAmount())
                .type(PointType.CANCELED)
                .description(description)
                .balanceSnapshot(pointBalance.getBalance())
                .pointBalance(pointBalance)
                .build();

        return pointRepository.save(cancelPoint);
    }

    // 포인트 정보 조회
    @Transactional(readOnly = true)
    public Long getBalance(Long userId) {
        return pointBalanceRepository.findByUserId(userId)
                .map(PointBalance::getBalance)
                .orElse(0L);

    }

    // 포인트 이력 조회
    public Page<Point> getPointHistory(Long userId, Pageable pageable) {
        return pointRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);
    }

}
