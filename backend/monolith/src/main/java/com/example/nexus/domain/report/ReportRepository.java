package com.example.nexus.domain.report;

import com.example.nexus.domain.report.model.Report;
import com.example.nexus.domain.user.model.User;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReportRepository extends JpaRepository<Report, Long> {

    // 특정 유저 보고서 조회
    Page<Report> findByUserOrderByCreatedAtDesc(User user, Pageable pageable);
}
