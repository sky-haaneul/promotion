package org.sky.haaneul.couponservice.service.v1;

import lombok.RequiredArgsConstructor;
import org.sky.haaneul.couponservice.aop.CouponMetered;
import org.sky.haaneul.couponservice.config.UserIdInterceptor;
import org.sky.haaneul.couponservice.domain.Coupon;
import org.sky.haaneul.couponservice.domain.CouponPolicy;
import org.sky.haaneul.couponservice.dto.v1.CouponDto;
import org.sky.haaneul.couponservice.exception.CouponIssueException;
import org.sky.haaneul.couponservice.exception.CouponNotFoundException;
import org.sky.haaneul.couponservice.repository.CouponPolicyRepository;
import org.sky.haaneul.couponservice.repository.CouponRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CouponService {
    private final CouponRepository couponRepository;
    private final CouponPolicyRepository couponPolicyRepository;

    /**
     * 1. Race Condition 발생 가능성
     * findByIdWithLock으로 쿠폰 정책에 대해 락을 걸지만, countByCouponPolicyId와 실제 쿠폰 저장 사이에 갭이 존재
     * 여러 트랜잭션이 동시에 카운트를 조회하고 조건을 통과한 후 쿠폰을 저장할 수 있음
     * 결과적으로 totalQuantity보다 더 많은 쿠폰이 발급될 수 있음
     *
     * 2. 성능 이슈
     * 매 요청마다 발급된 쿠폰 수를 카운트하는 쿼리 실행
     * 쿠폰 수가 많아질수록 카운트 쿼리의 성능이 저하될 수 있음
     * PESSIMISTIC_LOCK으로 인한 병목 현상 발생 가능
     *
     * 3. Dead Lock 가능성
     * 여러 트랜잭션이 동시에 같은 쿠폰 정책에 대해 락을 획득하러 할 때
     * 트랜잭션 타임아웃이 발생할 수 있음
     *
     * 4. 정확한 수량 보장의 어려움
     * 분산 환경에서 여러 서버가 동시에 쿠폰을 발급할 경우
     * DB 레벨의 락만으로는 정확한 수량 제어가 어려움
     *
     *
     */
    @Transactional
    @CouponMetered(version = "v1")
    public Coupon issueCoupon(CouponDto.IssueRequest request) {
        CouponPolicy couponPolicy = couponPolicyRepository.findByIdWithLock(request.getCouponPolicyId())
                .orElseThrow(() -> new CouponIssueException("쿠폰 정책을 찾을 수 없습니다."));

        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(couponPolicy.getStartTime()) || now.isAfter(couponPolicy.getEndTime())) {
            throw new CouponIssueException("쿠폰 발급 기간이 아닙니다.");
        }

        long issuedCouponCount = couponRepository.countByCouponPolicyId(couponPolicy.getId());
        if (issuedCouponCount >= couponPolicy.getTotalQuantity()) {
            throw new CouponIssueException("쿠폰이 모두 발급되었습니다.");
        }

        // X-USER-ID 헤더에서 사용자 ID를 가져오는 로직이 필요합니다. (api-gateway에서 주입)

        Coupon coupon = Coupon.builder()
                .couponPolicy(couponPolicy)
                .userId(UserIdInterceptor.getCurrentUserId())
                .couponCode(generateCouponCode())
                .build();

        return couponRepository.save(coupon);
    }

    private String generateCouponCode() {
        // 쿠폰 코드를 생성하는 로직을 구현합니다.
        return UUID.randomUUID().toString().substring(0, 8);
    }

    @Transactional
    public Coupon useCoupon(Long couponId, Long orderId) {
        Long currentUserId = UserIdInterceptor.getCurrentUserId();

        Coupon coupon = couponRepository.findByIdAndUserId(couponId, currentUserId)
                .orElseThrow(() -> new CouponNotFoundException("쿠폰을 찾을 수 없거나 접근 권한이 없습니다."));

        coupon.use(orderId);
        return coupon;
    }

    @Transactional
    public Coupon cancelCoupon(Long couponId) {
        Long currentUserId = UserIdInterceptor.getCurrentUserId();

        Coupon coupon = couponRepository.findByIdAndUserId(couponId, currentUserId)
                .orElseThrow(() -> new CouponNotFoundException("쿠폰을 찾을 수 없거나 접근 권한이 없습니다."));

        coupon.cancel();
        return coupon;
    }


    @Transactional(readOnly = true)
    public Page<Coupon> getCoupon(CouponDto.ListRequest request) {
        Long currentUserId = UserIdInterceptor.getCurrentUserId();
        return couponRepository.findByUserIdAndStatusOrderByCreatedAtDesc(
                currentUserId,
                request.getStatus(),
                PageRequest.of(
                        request.getPage() != null ? request.getPage() : 0,
                        request.getSize() != null ? request.getSize() : 10
                )
        );
    }


}
