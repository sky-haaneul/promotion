package org.sky.haaneul.couponservice.service.v1;

import lombok.RequiredArgsConstructor;
import org.sky.haaneul.couponservice.domain.CouponPolicy;
import org.sky.haaneul.couponservice.dto.v1.CouponPolicyDto;
import org.sky.haaneul.couponservice.exception.CouponPolicyNotFoundException;
import org.sky.haaneul.couponservice.repository.CouponPolicyRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CouponPolicyService {
    private final CouponPolicyRepository couponPolicyRepository;

    @Transactional
    public CouponPolicy createCouponPolicy(CouponPolicyDto.CreateRequest request) {
        CouponPolicy couponPolicy = request.toEntity();
        return couponPolicyRepository.save(couponPolicy);
    }

    public CouponPolicy getCouponPolicy(Long id) {
        return couponPolicyRepository.findById(id)
                .orElseThrow(() -> new CouponPolicyNotFoundException("쿠폰 정책을 찾을 수 없습니다."));
    }

    @Transactional(readOnly = true)
    public List<CouponPolicy> getAllCouponPolicies() {
        return couponPolicyRepository.findAll();
    }

}
