package org.sky.haaneul.couponservice.controller.v3;

import lombok.RequiredArgsConstructor;
import org.sky.haaneul.couponservice.dto.v3.CouponDto;
import org.sky.haaneul.couponservice.service.v3.CouponService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController("couponControllerV3")
@RequestMapping("/api/v3/coupons")
@RequiredArgsConstructor
public class CouponController {
    private final CouponService couponService;

    @PostMapping("/issue")
    public ResponseEntity<CouponDto.CouponResponse> issueCoupon(@RequestBody CouponDto.IssueRequest request) {
        couponService.requestCouponIssue(request);
        return ResponseEntity.accepted().build();
    }

    @PostMapping("/{couponId}/use")
    public ResponseEntity<CouponDto.CouponResponse> useCoupon(@PathVariable Long couponId, @RequestParam Long orderId) {
        CouponDto.CouponResponse response = CouponDto.CouponResponse.from(couponService.useCoupon(couponId, orderId));
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{couponId}/cancel")
    public ResponseEntity<CouponDto.CouponResponse> cancelCoupon(@PathVariable Long couponId) {
        CouponDto.CouponResponse response = CouponDto.CouponResponse.from(couponService.cancelCoupon(couponId));
        return ResponseEntity.ok().build();
    }
}
