package org.sky.haaneul.couponservice.controller.v1;

import lombok.RequiredArgsConstructor;
import org.sky.haaneul.couponservice.dto.v1.CouponPolicyDto;
import org.sky.haaneul.couponservice.service.v1.CouponPolicyService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/coupon-policies")
@RequiredArgsConstructor
public class CouponPolicyController {
    private final CouponPolicyService couponPolicyService;

    @PostMapping
    public ResponseEntity<CouponPolicyDto.Response> createCouponPollicy(
            @RequestBody CouponPolicyDto.CreateRequest request) {
        return ResponseEntity.ok()
                .body(CouponPolicyDto.Response.from(
                        couponPolicyService.createCouponPolicy(request)
                ));
    }

    @GetMapping("/{id}")
    public ResponseEntity<CouponPolicyDto.Response> getCouponPolicy(@PathVariable Long id) {
        return ResponseEntity.ok(CouponPolicyDto.Response.from(couponPolicyService.getCouponPolicy(id)));
    }

    @GetMapping
    public ResponseEntity<List<CouponPolicyDto.Response>> getAllCouponPolicies() {
        return ResponseEntity.ok(
                couponPolicyService.getAllCouponPolicies().stream()
                        .map(CouponPolicyDto.Response::from)
                        .toList()
        );
    }

}
