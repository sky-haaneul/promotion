package org.sky.haaneul.pointservicebatch.repository;

import org.sky.haaneul.pointservicebatch.domain.DailyPointReport;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DailyPointReportRepository extends JpaRepository<DailyPointReport, Long> {
}
