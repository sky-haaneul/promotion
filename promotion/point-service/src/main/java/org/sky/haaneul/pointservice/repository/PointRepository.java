package org.sky.haaneul.pointservice.repository;

import io.lettuce.core.dynamic.annotation.Param;
import org.sky.haaneul.pointservice.domain.Point;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface PointRepository extends JpaRepository<Point, Long> {

    @Query("SELECT p FROM Point p " +
            "LEFT JOIN FETCH p.pointBalance " +
            "WHERE p.userId = :userId " +
            "ORDER BY p.createdAt DESC")
    Page<Point> findByUserIdOrderByCreatedAtDesc(@Param("userId") Long userId, Pageable pageable);

}
