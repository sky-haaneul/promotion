package org.sky.haaneul.timesaleservice.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@Configuration
@EnableJpaAuditing
public class JpaConfig {

    /**
     * TimeSale에서 Product LAZY 조회시 오류 발생 해결 방법 2가지
     * 1. TimeSale에 아래와 같이 추가
     * public Product getProduct() {
 *         if (this.product instanceof HibernateProxy) {  // product객체를 HibernateLazyInitializer를 통해서 실제 Product를 객체로 만들어서 반환
 *             return (Product) ((HibernateProxy) this.product).getHibernateLazyInitializer().getImplementation();
 *         }
 *         return this.product;
     *     }
     * 와 같은 의미
     */

    // 2. 아래와 같이 ObjectMapper Bean 추가
//    @Bean
//    public ObjectMapper objectMapper() {
//        ObjectMapper objectMapper = new ObjectMapper();
//        objectMapper.registerModule(new Hibernate6Module()); // Hibernate6Module 사용
//        return objectMapper;
//    }
}
