package org.sky.haaneul.couponservice.service.v2;

import lombok.RequiredArgsConstructor;
import org.sky.haaneul.couponservice.domain.Coupon;
import org.sky.haaneul.couponservice.dto.v1.CouponDto;
import org.sky.haaneul.couponservice.exception.CouponNotFoundException;
import org.sky.haaneul.couponservice.repository.CouponRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// CouponService에서 Transactional을 걸어서 redis와 state까지 변경할 수 있는 서비스
@Service("couponServiceV2")
@RequiredArgsConstructor
public class CouponService {
    private final CouponRepository couponRepository;
    private final CouponRedisService couponRedisService;
    private final CouponStateService couponStateService;

    // 쿠폰 발급
    @Transactional
    public CouponDto.Response issueCoupon(CouponDto.IssueRequest request) {
        Coupon coupon = couponRedisService.issueCoupon(request);
        couponStateService.updateCouponState(couponRepository.findById(coupon.getId())
                .orElseThrow(() -> new RuntimeException("쿠폰 상태 업데이트 중 오류가 발생했습니다.")));

        return CouponDto.Response.from(coupon);
    }

    // 쿠폰 사용 -> 쿠폰 사용량이 많을 경우 레디스로 처리하는 부분도 고려
    @Transactional
    public CouponDto.Response useCoupon(Long couponId, Long orderId) {
        Coupon coupon = couponRepository.findByIdWithLock(couponId)
                .orElseThrow(() -> new CouponNotFoundException("쿠폰을 찾을 수 없습니다."));

        coupon.use(orderId);
        couponStateService.updateCouponState(coupon);

        return CouponDto.Response.from(coupon);
    }

    // 쿠폰 취소
    @Transactional
    public CouponDto.Response cancelCoupon(Long couponId) {
        Coupon coupon = couponRepository.findByIdWithLock(couponId)
                .orElseThrow(() -> new CouponNotFoundException("쿠폰을 찾을 수 없습니다."));

        coupon.cancel();
        couponStateService.updateCouponState(coupon);

        return CouponDto.Response.from(coupon);
    }

    public CouponDto.Response getCoupon(Long couponId) {
        CouponDto.Response cachedCoupon = couponStateService.getCouponState(couponId);
        if (cachedCoupon != null) {
            return cachedCoupon;
        }

        Coupon coupon = couponRepository.findById(couponId)
                .orElseThrow(() -> new CouponNotFoundException("쿠폰을 찾을 수 없습니다."));

        CouponDto.Response response = CouponDto.Response.from(coupon);
        couponStateService.updateCouponState(coupon); // 상태 캐싱

        return response;
    }
}
