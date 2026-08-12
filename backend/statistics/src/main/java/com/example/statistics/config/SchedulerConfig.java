package com.example.statistics.config;

import net.javacrumbs.shedlock.provider.jdbctemplate.JdbcTemplateLockProvider;
import net.javacrumbs.shedlock.spring.annotation.EnableSchedulerLock;
import net.javacrumbs.shedlock.core.LockProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.EnableScheduling;

import javax.sql.DataSource;

/**
 * 스케줄러 + ShedLock 설정.
 *
 * <ul>
 *   <li>{@link EnableScheduling} — Spring 의 {@code @Scheduled} 활성화</li>
 *   <li>{@link EnableSchedulerLock} — ShedLock 활성화 (분산 락)</li>
 *   <li>{@link LockProvider} — 락 저장소 = JDBC (DB 의 shedlock 테이블 사용)</li>
 * </ul>
 *
 * <p>shedlock 테이블은 {@code resources/schema.sql} 에서 IF NOT EXISTS 로 생성.
 * (ShedLock 라이브러리가 자동 마이그레이션을 제공하지 않으므로 수동 DDL 필요)
 *
 * <p>{@code usingDbTime()} — 각 pod 의 OS 시각 차이(clock skew) 무관하게
 * DB 서버 시각을 기준으로 락 유효 시간 계산.
 */
@Configuration
@EnableScheduling
@EnableSchedulerLock(defaultLockAtMostFor = "10m")
public class SchedulerConfig {

    @Bean
    public LockProvider lockProvider(DataSource dataSource) {
        return new JdbcTemplateLockProvider(
                JdbcTemplateLockProvider.Configuration.builder()
                        .withJdbcTemplate(new JdbcTemplate(dataSource))
                        .usingDbTime()
                        .build()
        );
    }
}
