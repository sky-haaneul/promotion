package org.sky.haaneul.couponservice.config;

import io.micrometer.core.instrument.MeterRegistry;
import org.sky.haaneul.couponservice.aop.CouponMetricsAspect;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

@Configuration
@EnableAspectJAutoProxy
public class MetricsConfig {

    @Bean
    public CouponMetricsAspect couponMetricsAspect(MeterRegistry registry) {
        return new CouponMetricsAspect(registry);
    }

}
