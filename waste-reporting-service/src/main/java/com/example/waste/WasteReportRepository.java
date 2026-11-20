package com.example.waste;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface WasteReportRepository extends JpaRepository<WasteReport, Long> {
    List<WasteReport> findByStatus(ReportStatus status);
}
