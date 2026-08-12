-- ============================================================
-- 장기 통계 (#881) 로컬 검증용 더미 데이터
-- 기간: 1년치 (2025-05-23 ~ 2026-05-22)
--
-- ⚠️ WARNING: 운영 DB 에는 절대 실행 금지!
--    로컬/테스트 환경에서만 사용.
--
-- 실행 방법:
--   mariadb -h 127.0.0.1 -P 3307 -uroot -pqwer1234 nexus \
--     < backend/statistics/src/main/resources/sql/seed-long-term-dummy.sql
--
--   또는 mariadb 셸 안에서 source 명령:
--   source backend/statistics/src/main/resources/sql/seed-long-term-dummy.sql;
-- ============================================================


-- ─── (1) daily_total_sales: 365 row, 일일 1억~2억 랜덤 ───────
INSERT IGNORE INTO daily_total_sales (aggregate_date, total_amount, created_at)
SELECT
    DATE_SUB('2026-05-22', INTERVAL n DAY),
    FLOOR(100000000 + RAND() * 100000000),
    NOW()
FROM (
    SELECT a.N + b.N * 10 + c.N * 100 AS n
    FROM
        (SELECT 0 AS N UNION SELECT 1 UNION SELECT 2 UNION SELECT 3 UNION SELECT 4
         UNION SELECT 5 UNION SELECT 6 UNION SELECT 7 UNION SELECT 8 UNION SELECT 9) a
        CROSS JOIN (SELECT 0 AS N UNION SELECT 1 UNION SELECT 2 UNION SELECT 3 UNION SELECT 4
         UNION SELECT 5 UNION SELECT 6 UNION SELECT 7 UNION SELECT 8 UNION SELECT 9) b
        CROSS JOIN (SELECT 0 AS N UNION SELECT 1 UNION SELECT 2 UNION SELECT 3) c
) numbers
WHERE n BETWEEN 0 AND 364;


-- ─── (2) daily_store_sales: 365 × 100 매장 = 36,500 row ──────
INSERT IGNORE INTO daily_store_sales (aggregate_date, store_idx, store_name, amount, created_at)
SELECT
    DATE_SUB('2026-05-22', INTERVAL d.n DAY),
    s.store_idx,
    CONCAT('매장 ', s.store_idx),
    FLOOR(500000 + RAND() * 2000000),
    NOW()
FROM
    (SELECT a.N + b.N * 10 + c.N * 100 AS n
     FROM
        (SELECT 0 AS N UNION SELECT 1 UNION SELECT 2 UNION SELECT 3 UNION SELECT 4
         UNION SELECT 5 UNION SELECT 6 UNION SELECT 7 UNION SELECT 8 UNION SELECT 9) a
        CROSS JOIN (SELECT 0 AS N UNION SELECT 1 UNION SELECT 2 UNION SELECT 3 UNION SELECT 4
         UNION SELECT 5 UNION SELECT 6 UNION SELECT 7 UNION SELECT 8 UNION SELECT 9) b
        CROSS JOIN (SELECT 0 AS N UNION SELECT 1 UNION SELECT 2 UNION SELECT 3) c
    ) d
    CROSS JOIN (
        SELECT a.N + b.N * 10 + 1 AS store_idx
        FROM
            (SELECT 0 AS N UNION SELECT 1 UNION SELECT 2 UNION SELECT 3 UNION SELECT 4
             UNION SELECT 5 UNION SELECT 6 UNION SELECT 7 UNION SELECT 8 UNION SELECT 9) a
            CROSS JOIN (SELECT 0 AS N UNION SELECT 1 UNION SELECT 2 UNION SELECT 3 UNION SELECT 4
             UNION SELECT 5 UNION SELECT 6 UNION SELECT 7 UNION SELECT 8 UNION SELECT 9) b
    ) s
WHERE d.n BETWEEN 0 AND 364 AND s.store_idx <= 100;


-- ─── (3) daily_category_sales: 365 × 4 카테고리 = 1,460 row ──
INSERT IGNORE INTO daily_category_sales (aggregate_date, category_name, amount, created_at)
SELECT
    DATE_SUB('2026-05-22', INTERVAL d.n DAY),
    c.category_name,
    FLOOR(20000000 + RAND() * 30000000),
    NOW()
FROM
    (SELECT a.N + b.N * 10 + c.N * 100 AS n
     FROM
        (SELECT 0 AS N UNION SELECT 1 UNION SELECT 2 UNION SELECT 3 UNION SELECT 4
         UNION SELECT 5 UNION SELECT 6 UNION SELECT 7 UNION SELECT 8 UNION SELECT 9) a
        CROSS JOIN (SELECT 0 AS N UNION SELECT 1 UNION SELECT 2 UNION SELECT 3 UNION SELECT 4
         UNION SELECT 5 UNION SELECT 6 UNION SELECT 7 UNION SELECT 8 UNION SELECT 9) b
        CROSS JOIN (SELECT 0 AS N UNION SELECT 1 UNION SELECT 2 UNION SELECT 3) c
    ) d
    CROSS JOIN (
        SELECT '커피' AS category_name UNION ALL
        SELECT '음료' UNION ALL
        SELECT '디저트' UNION ALL
        SELECT '푸드'
    ) c
WHERE d.n BETWEEN 0 AND 364;


-- ─── (4) daily_menu_sales: 365 × 15 메뉴 = 5,475 row ─────────
INSERT IGNORE INTO daily_menu_sales (aggregate_date, menu_idx, menu_name, quantity, created_at)
SELECT
    DATE_SUB('2026-05-22', INTERVAL d.n DAY),
    m.menu_idx,
    CONCAT('메뉴 ', m.menu_idx),
    FLOOR(50 + RAND() * 500),
    NOW()
FROM
    (SELECT a.N + b.N * 10 + c.N * 100 AS n
     FROM
        (SELECT 0 AS N UNION SELECT 1 UNION SELECT 2 UNION SELECT 3 UNION SELECT 4
         UNION SELECT 5 UNION SELECT 6 UNION SELECT 7 UNION SELECT 8 UNION SELECT 9) a
        CROSS JOIN (SELECT 0 AS N UNION SELECT 1 UNION SELECT 2 UNION SELECT 3 UNION SELECT 4
         UNION SELECT 5 UNION SELECT 6 UNION SELECT 7 UNION SELECT 8 UNION SELECT 9) b
        CROSS JOIN (SELECT 0 AS N UNION SELECT 1 UNION SELECT 2 UNION SELECT 3) c
    ) d
    CROSS JOIN (
        SELECT 1 AS menu_idx UNION SELECT 2 UNION SELECT 3 UNION SELECT 4 UNION SELECT 5
        UNION SELECT 6 UNION SELECT 7 UNION SELECT 8 UNION SELECT 9 UNION SELECT 10
        UNION SELECT 11 UNION SELECT 12 UNION SELECT 13 UNION SELECT 14 UNION SELECT 15
    ) m
WHERE d.n BETWEEN 0 AND 364;


-- ─── 검증 쿼리 ─────────────────────────────────────────────
SELECT COUNT(*) AS total_cnt    FROM daily_total_sales;     -- 약 365
SELECT COUNT(*) AS store_cnt    FROM daily_store_sales;     -- 약 36,500
SELECT COUNT(*) AS category_cnt FROM daily_category_sales;  -- 약 1,460
SELECT COUNT(*) AS menu_cnt     FROM daily_menu_sales;      -- 약 5,475
