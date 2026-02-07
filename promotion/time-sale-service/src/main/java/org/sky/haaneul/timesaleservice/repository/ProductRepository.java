package org.sky.haaneul.timesaleservice.repository;

import org.sky.haaneul.timesaleservice.domain.Product;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product, Long> {
}
