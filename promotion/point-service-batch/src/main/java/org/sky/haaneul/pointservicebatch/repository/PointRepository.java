package org.sky.haaneul.pointservicebatch.repository;

import org.sky.haaneul.pointservicebatch.domain.Point;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PointRepository extends JpaRepository<Point, Long> {
}
