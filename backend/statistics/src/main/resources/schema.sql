-- ============================================================
-- ShedLock 분산 락 테이블
-- ShedLock 라이브러리가 자동 마이그레이션 제공하지 않으므로 수동 DDL.
-- application 시작 시 spring.sql.init.mode=always 로 항상 실행됨.
-- IF NOT EXISTS 라 중복 실행해도 안전.
-- ============================================================

CREATE TABLE IF NOT EXISTS shedlock (
    name VARCHAR(64) NOT NULL,
    lock_until TIMESTAMP(3) NOT NULL,
    locked_at TIMESTAMP(3) NOT NULL,
    locked_by VARCHAR(255) NOT NULL,
    PRIMARY KEY (name)
);
