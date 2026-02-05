package org.sky.haaneul.couponservice.controller.v1;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PageController {

    @GetMapping("/ui/coupons")
    public String couponsPage() {
        return "coupon";
    }

    @GetMapping("/ui/policies")
    public String policyPage() {
        return "policy";
    }
}
