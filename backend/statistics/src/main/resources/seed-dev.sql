-- ============================================================
-- Nexus Statistics MSA 로컬 dev seed
-- daily_*_sales 7개 테이블 (어제 ~ 30일 전, 영구 보존 통계)
-- ============================================================
-- 일치 조건 (모놀리식 seed-dev.sql 의 결제 식과 동일):
--   매장 s 의 매일 결제 5건:
--     i=1: 시간 9,  amount=1000+((s*100+1*1500)%9001), method=CARD
--     i=2: 시간 12, amount=1000+((s*100+2*1500)%9001), method=CARD
--     i=3: 시간 15, amount=1000+((s*100+3*1500)%9001), method=CARD
--     i=4: 시간 18, amount=1000+((s*100+4*1500)%9001), method=CASH
--     i=5: 시간 21, amount=1000+((s*100+5*1500)%9001), method=CASH
--   pos_orders_item:
--     항목 1: menu = 1 + ((s + d + i) % 15), qty = 1 + (i % 2)
--     항목 2 (i % 2 = 0): menu = 1 + ((s + d + i + 7) % 15), qty = 1
--
-- 범위: aggregate_date = 어제 (CURDATE - 1) ~ 30일 전 (CURDATE - 30)
--   (오늘은 Redis 에서, 모놀리식 pos_pay 의 오늘 데이터)
-- ============================================================

SET FOREIGN_KEY_CHECKS = 0;
-- TRUNCATE: 이전 데이터 + UNIQUE constraint 누적 모두 정리 (dev 환경)
TRUNCATE TABLE daily_total_sales;
TRUNCATE TABLE daily_store_sales;
TRUNCATE TABLE daily_menu_sales;
TRUNCATE TABLE daily_category_sales;
TRUNCATE TABLE daily_hourly_sales;
TRUNCATE TABLE daily_payment_sales;
TRUNCATE TABLE daily_dump_log;
SET FOREIGN_KEY_CHECKS = 1;


-- ============================================================
-- 메뉴 / 카테고리 매핑 (모놀리식 seed-dev.sql 과 동일)
-- ============================================================
-- menu_idx 1~5  → 음료
-- menu_idx 6~8  → 디저트
-- menu_idx 9~12 → 베이커리
-- menu_idx 13~15→ 푸드
--
-- 메뉴명 / 가격 (모놀리식과 동일):
-- 1=아메리카노(3000), 2=카페라떼(4000), 3=카푸치노(4500), 4=바닐라라떼(4500), 5=자몽에이드(5500)
-- 6=티라미수(6000), 7=치즈케이크(6500), 8=마카롱(2500)
-- 9=크로와상(3500), 10=소금빵(3000), 11=초코머핀(3500), 12=베이글(4000)
-- 13=샌드위치(7000), 14=파니니(8000), 15=샐러드(8500)


-- ============================================================
-- 1. 메인 PROCEDURE — 30일치 daily_*_sales 채움
-- ============================================================
DELIMITER $$
DROP PROCEDURE IF EXISTS seed_daily_stats$$
CREATE PROCEDURE seed_daily_stats()
BEGIN
    DECLARE d INT;        -- 1~30 (어제부터)
    DECLARE s INT;        -- 1~100 (매장)
    DECLARE i INT;        -- 1~5 (결제 건)
    DECLARE target_date DATE;
    DECLARE amount BIGINT;
    DECLARE menu1 INT;
    DECLARE qty1 INT;
    DECLARE menu2 INT;
    DECLARE store_sum BIGINT;
    DECLARE day_total BIGINT;

    -- 임시 테이블 (일자별/매장별/메뉴별/시간별/결제수단별/카테고리별 누적)
    DROP TEMPORARY TABLE IF EXISTS tmp_daily;
    CREATE TEMPORARY TABLE tmp_daily (
        agg_date DATE,
        store_idx INT,
        store_name VARCHAR(100),
        amount BIGINT
    );

    DROP TEMPORARY TABLE IF EXISTS tmp_menu;
    CREATE TEMPORARY TABLE tmp_menu (
        agg_date DATE,
        menu_idx INT,
        qty BIGINT
    );

    DROP TEMPORARY TABLE IF EXISTS tmp_hourly;
    CREATE TEMPORARY TABLE tmp_hourly (
        agg_date DATE,
        hour INT,
        amount BIGINT
    );

    DROP TEMPORARY TABLE IF EXISTS tmp_payment;
    CREATE TEMPORARY TABLE tmp_payment (
        agg_date DATE,
        method VARCHAR(20),
        amount BIGINT
    );

    -- ====================================================
    -- 결정적 식으로 30일 × 100매장 × 5건 누적 계산
    -- ====================================================
    SET d = 1;
    WHILE d <= 30 DO
        SET target_date = DATE_SUB(CURDATE(), INTERVAL d DAY);
        SET s = 1;
        WHILE s <= 100 DO
            SET store_sum = 0;
            SET i = 1;
            WHILE i <= 7 DO
                -- 모놀리식과 동일한 결정적 식 (d 포함 — 일별 변동성)
                SET amount = 1000 + ((s * 100 + i * 1500 + d * 700) MOD 9001);
                SET store_sum = store_sum + amount;

                -- 시간대 누적 (i=1→9, 2→11, 3→13, 4→15, 5→17, 6→19, 7→21)
                INSERT INTO tmp_hourly (agg_date, hour, amount) VALUES (
                    target_date,
                    CASE i WHEN 1 THEN 9 WHEN 2 THEN 11 WHEN 3 THEN 13 WHEN 4 THEN 15 WHEN 5 THEN 17 WHEN 6 THEN 19 ELSE 21 END,
                    amount
                );

                -- 결제 수단 누적 (i<=5 CARD, i>=6 CASH)
                INSERT INTO tmp_payment (agg_date, method, amount) VALUES (
                    target_date,
                    IF (i <= 5, 'CARD', 'CASH'),
                    amount
                );

                -- 메뉴 항목 1 (모놀리식 식)
                SET menu1 = 1 + ((s + d + i) MOD 15);
                SET qty1 = 1 + (i MOD 2);
                INSERT INTO tmp_menu (agg_date, menu_idx, qty) VALUES (target_date, menu1, qty1);

                -- 메뉴 항목 2 (i % 2 = 0)
                IF i MOD 2 = 0 THEN
                    SET menu2 = 1 + ((s + d + i + 7) MOD 15);
                    INSERT INTO tmp_menu (agg_date, menu_idx, qty) VALUES (target_date, menu2, 1);
                END IF;

                SET i = i + 1;
            END WHILE;

            -- 매장별 sum 누적 (매장 이름은 모놀리식과 동일 — 더벤티 <지역>점)
            INSERT INTO tmp_daily (agg_date, store_idx, store_name, amount) VALUES (
                target_date, s,
                CONCAT('더벤티 ', ELT(s,
                    '강남','강동','강서','강북','관악','광진','구로','금천','노원','도봉',
                    '동대문','동작','마포','서대문','서초','성동','성북','송파','양천','영등포',
                    '용산','은평','종로','서울중구','중랑',
                    '수원','성남','고양','용인','부천','안산','안양','평택','시흥','광명',
                    '의정부','김포','화성','남양주','군포',
                    '해운대','부산진','동래','사상','부산강서','사하','금정','기장','부산남구','부산북구',
                    '대구중구','수성','달서','달성','대구동구',
                    '인천중구','미추홀','연수','남동','부평',
                    '광주북구','광주서구','광주남구',
                    '대전유성','대전서구','대전동구',
                    '울산남구','울산동구','울산북구',
                    '세종',
                    '제주','서귀포',
                    '춘천','원주','강릉','속초','평창',
                    '청주','충주','제천',
                    '천안','아산','공주','서산',
                    '포항','경주','안동','구미',
                    '창원','김해','진주','양산',
                    '전주','군산','익산',
                    '여수','순천','목포',
                    '김포공항','시청'
                ), '점'),
                store_sum
            );

            SET s = s + 1;
        END WHILE;
        SET d = d + 1;
    END WHILE;

    -- ====================================================
    -- daily_store_sales (일자 × 매장)
    -- ====================================================
    INSERT INTO daily_store_sales (aggregate_date, store_idx, store_name, amount, created_at)
    SELECT agg_date, store_idx, store_name, amount, NOW() FROM tmp_daily;

    -- ====================================================
    -- daily_total_sales (일자별 총)
    -- ====================================================
    INSERT INTO daily_total_sales (aggregate_date, total_amount, created_at)
    SELECT agg_date, SUM(amount), NOW() FROM tmp_daily GROUP BY agg_date;

    -- ====================================================
    -- daily_hourly_sales (일자 × 시간)
    -- ====================================================
    INSERT INTO daily_hourly_sales (aggregate_date, hour, amount, created_at)
    SELECT agg_date, hour, SUM(amount), NOW() FROM tmp_hourly GROUP BY agg_date, hour;

    -- ====================================================
    -- daily_payment_sales (일자 × 결제수단)
    -- ====================================================
    INSERT INTO daily_payment_sales (aggregate_date, payment_method, amount, created_at)
    SELECT agg_date, method, SUM(amount), NOW() FROM tmp_payment GROUP BY agg_date, method;

    -- ====================================================
    -- daily_menu_sales (일자 × 메뉴)
    -- ====================================================
    INSERT INTO daily_menu_sales (aggregate_date, menu_idx, menu_name, quantity, created_at)
    SELECT
        agg_date,
        menu_idx,
        CASE menu_idx
            WHEN 1 THEN '아메리카노' WHEN 2 THEN '카페라떼' WHEN 3 THEN '카푸치노' WHEN 4 THEN '바닐라라떼' WHEN 5 THEN '자몽에이드'
            WHEN 6 THEN '티라미수' WHEN 7 THEN '치즈케이크' WHEN 8 THEN '마카롱'
            WHEN 9 THEN '크로와상' WHEN 10 THEN '소금빵' WHEN 11 THEN '초코머핀' WHEN 12 THEN '베이글'
            WHEN 13 THEN '샌드위치' WHEN 14 THEN '파니니' ELSE '샐러드' END,
        SUM(qty),
        NOW()
    FROM tmp_menu
    GROUP BY agg_date, menu_idx;

    -- ====================================================
    -- daily_category_sales (일자 × 카테고리)
    -- menu_idx 1~5 = 음료, 6~8 = 디저트, 9~12 = 베이커리, 13~15 = 푸드
    -- 카테고리별 매출 = SUM(menu_price * quantity)
    -- ====================================================
    INSERT INTO daily_category_sales (aggregate_date, category_name, amount, created_at)
    SELECT
        agg_date,
        CASE
            WHEN menu_idx BETWEEN 1 AND 5  THEN '음료'
            WHEN menu_idx BETWEEN 6 AND 8  THEN '디저트'
            WHEN menu_idx BETWEEN 9 AND 12 THEN '베이커리'
            ELSE '푸드'
        END AS cat,
        SUM(qty *
            CASE menu_idx
                WHEN 1 THEN 3000 WHEN 2 THEN 4000 WHEN 3 THEN 4500 WHEN 4 THEN 4500 WHEN 5 THEN 5500
                WHEN 6 THEN 6000 WHEN 7 THEN 6500 WHEN 8 THEN 2500
                WHEN 9 THEN 3500 WHEN 10 THEN 3000 WHEN 11 THEN 3500 WHEN 12 THEN 4000
                WHEN 13 THEN 7000 WHEN 14 THEN 8000 ELSE 8500 END
        ),
        NOW()
    FROM tmp_menu
    GROUP BY agg_date, cat;

    -- ====================================================
    -- daily_dump_log (일자별 SUCCESS 로그)
    -- ====================================================
    SET d = 1;
    WHILE d <= 30 DO
        SET target_date = DATE_SUB(CURDATE(), INTERVAL d DAY);
        INSERT INTO daily_dump_log (aggregate_date, dump_status, record_count, error_message, dumped_at)
        VALUES (target_date, 'SUCCESS', 500, NULL, NOW());   -- 일별 500건 (매장 100 × 일 5건)
        SET d = d + 1;
    END WHILE;

    -- 정리
    DROP TEMPORARY TABLE tmp_daily;
    DROP TEMPORARY TABLE tmp_menu;
    DROP TEMPORARY TABLE tmp_hourly;
    DROP TEMPORARY TABLE tmp_payment;
END$$
DELIMITER ;

CALL seed_daily_stats();
DROP PROCEDURE seed_daily_stats;


-- ============================================================
-- 2. 장기 통계 (작년 ~ 1.5년 전 매월 1일 데이터)
-- LongTermStatisticsService 의 월별/분기별/연도별 차트용
-- 매월 1일에 1건 INSERT (그 달 합계)
-- 운영 패턴: 30일 이전 raw 데이터는 폐기, daily_*_sales 만 영구 보존
-- ============================================================
DELIMITER $$
DROP PROCEDURE IF EXISTS seed_long_term$$
CREATE PROCEDURE seed_long_term()
BEGIN
    DECLARE m INT;
    DECLARE s INT;
    DECLARE i INT;
    DECLARE p INT;
    DECLARE target_date DATE;
    DECLARE store_daily_sum BIGINT;
    DECLARE store_monthly BIGINT;
    DECLARE total_monthly BIGINT;
    DECLARE month_days INT;

    -- 매월 1일 데이터 (m=2..18, 2개월 전 ~ 18개월 전 = 작년 + 그 이전 일부)
    -- m=1 (지난달) 은 seed_daily_stats 의 일별 데이터로 커버됨
    SET m = 2;
    WHILE m <= 18 DO
        SET target_date = DATE_FORMAT(DATE_SUB(CURDATE(), INTERVAL m MONTH), '%Y-%m-01');
        SET total_monthly = 0;
        -- 한 달 일수 변동 (25~30일)
        SET month_days = 25 + (m % 6);

        -- 매장별 매월 매출
        SET s = 1;
        WHILE s <= 100 DO
            SET store_daily_sum = 0;
            SET i = 1;
            WHILE i <= 7 DO
                SET store_daily_sum = store_daily_sum + (1000 + ((s * 100 + i * 1500) MOD 9001));
                SET i = i + 1;
            END WHILE;
            SET store_monthly = store_daily_sum * month_days;
            SET total_monthly = total_monthly + store_monthly;

            INSERT INTO daily_store_sales (aggregate_date, store_idx, store_name, amount, created_at)
            VALUES (target_date, s,
                CONCAT('더벤티 ', ELT(s,
                    '강남','강동','강서','강북','관악','광진','구로','금천','노원','도봉',
                    '동대문','동작','마포','서대문','서초','성동','성북','송파','양천','영등포',
                    '용산','은평','종로','서울중구','중랑',
                    '수원','성남','고양','용인','부천','안산','안양','평택','시흥','광명',
                    '의정부','김포','화성','남양주','군포',
                    '해운대','부산진','동래','사상','부산강서','사하','금정','기장','부산남구','부산북구',
                    '대구중구','수성','달서','달성','대구동구',
                    '인천중구','미추홀','연수','남동','부평',
                    '광주북구','광주서구','광주남구',
                    '대전유성','대전서구','대전동구',
                    '울산남구','울산동구','울산북구',
                    '세종',
                    '제주','서귀포',
                    '춘천','원주','강릉','속초','평창',
                    '청주','충주','제천',
                    '천안','아산','공주','서산',
                    '포항','경주','안동','구미',
                    '창원','김해','진주','양산',
                    '전주','군산','익산',
                    '여수','순천','목포',
                    '김포공항','시청'
                ), '점'),
                store_monthly, NOW());

            SET s = s + 1;
        END WHILE;

        -- 그 달 총 매출
        INSERT INTO daily_total_sales (aggregate_date, total_amount, created_at)
        VALUES (target_date, total_monthly, NOW());

        -- 메뉴별 판매량 (15개)
        SET p = 1;
        WHILE p <= 15 DO
            INSERT INTO daily_menu_sales (aggregate_date, menu_idx, menu_name, quantity, created_at)
            VALUES (target_date, p,
                CASE p
                    WHEN 1 THEN '아메리카노' WHEN 2 THEN '카페라떼' WHEN 3 THEN '카푸치노' WHEN 4 THEN '바닐라라떼' WHEN 5 THEN '자몽에이드'
                    WHEN 6 THEN '티라미수' WHEN 7 THEN '치즈케이크' WHEN 8 THEN '마카롱'
                    WHEN 9 THEN '크로와상' WHEN 10 THEN '소금빵' WHEN 11 THEN '초코머핀' WHEN 12 THEN '베이글'
                    WHEN 13 THEN '샌드위치' WHEN 14 THEN '파니니' ELSE '샐러드' END,
                30000 + (p * 1500) + ((m * 200) MOD 5000),  -- 메뉴 idx + 월별 변동
                NOW());
            SET p = p + 1;
        END WHILE;

        -- 카테고리별 (총 매출 분배)
        INSERT INTO daily_category_sales (aggregate_date, category_name, amount, created_at) VALUES
        (target_date, '음료',     total_monthly * 40 DIV 100, NOW()),
        (target_date, '디저트',   total_monthly * 20 DIV 100, NOW()),
        (target_date, '베이커리', total_monthly * 25 DIV 100, NOW()),
        (target_date, '푸드',     total_monthly * 15 DIV 100, NOW());

        -- 시간대별 (7개, 1일 평균 분배)
        INSERT INTO daily_hourly_sales (aggregate_date, hour, amount, created_at) VALUES
        (target_date, 9,  total_monthly DIV (month_days * 7), NOW()),
        (target_date, 11, total_monthly DIV (month_days * 7), NOW()),
        (target_date, 13, total_monthly DIV (month_days * 5), NOW()),
        (target_date, 15, total_monthly DIV (month_days * 6), NOW()),
        (target_date, 17, total_monthly DIV (month_days * 6), NOW()),
        (target_date, 19, total_monthly DIV (month_days * 7), NOW()),
        (target_date, 21, total_monthly DIV (month_days * 7), NOW());

        -- 결제 수단별 (CARD 71%, CASH 29%)
        INSERT INTO daily_payment_sales (aggregate_date, payment_method, amount, created_at) VALUES
        (target_date, 'CARD', total_monthly * 71 DIV 100, NOW()),
        (target_date, 'CASH', total_monthly * 29 DIV 100, NOW());

        -- dump log
        INSERT INTO daily_dump_log (aggregate_date, dump_status, record_count, error_message, dumped_at)
        VALUES (target_date, 'SUCCESS', 700 * month_days, NULL, NOW());

        SET m = m + 1;
    END WHILE;
END$$
DELIMITER ;

CALL seed_long_term();
DROP PROCEDURE seed_long_term;


-- ============================================================
-- 완료 확인
-- ============================================================
SELECT 'daily_total_sales',  COUNT(*) AS cnt FROM daily_total_sales
UNION ALL SELECT 'daily_store_sales', COUNT(*) FROM daily_store_sales
UNION ALL SELECT 'daily_menu_sales',  COUNT(*) FROM daily_menu_sales
UNION ALL SELECT 'daily_category_sales', COUNT(*) FROM daily_category_sales
UNION ALL SELECT 'daily_hourly_sales', COUNT(*) FROM daily_hourly_sales
UNION ALL SELECT 'daily_payment_sales', COUNT(*) FROM daily_payment_sales
UNION ALL SELECT 'daily_dump_log', COUNT(*) FROM daily_dump_log;

-- 검증: 매장 1 의 일일 매출 (모놀리식 SUM 과 동일해야 함)
SELECT aggregate_date, amount FROM daily_store_sales WHERE store_idx = 1 ORDER BY aggregate_date DESC LIMIT 5;

-- 검증: daily_total (매일 동일 sum)
SELECT aggregate_date, total_amount FROM daily_total_sales ORDER BY aggregate_date DESC LIMIT 5;
