package org.sky.haaneul.timesaleservice.repository;

import org.sky.haaneul.timesaleservice.domain.TimeSaleOrder;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TimeSaleOrderRepository extends JpaRepository<TimeSaleOrder, Long> {
}
