package com.example.nexus.domain.report.model;

import com.example.nexus.domain.user.model.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "report")
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class Report {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "report_idx")
    private Long idx;

    @Column(name = "report_title", nullable = false)
    private String reportTitle;

    @Column(name = "report_file_path", nullable = false)
    private String reportFilePath;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_idx")
    private User user;



    @Builder
    public Report(String title, String filePath, User user) {
        this.reportTitle = title;
        this.reportFilePath = filePath;
        this.user = user;
        this.createdAt = LocalDateTime.now(); // 생성 시간은 객체가 만들어질 때 자동으로 세팅!
    }
}
