package org.sky.haaneul.couponservice.service.v1;

import lombok.RequiredArgsConstructor;
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

    @Transactional
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
