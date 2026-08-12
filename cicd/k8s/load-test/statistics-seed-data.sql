-- ============================================================
-- 부하 테스트용 Statistics MSA 더미데이터 (slim schema 기반)
-- 용도: test-statistics-db에 매장 3000개 + 결제 30000건 투입
-- 실행: kubectl exec -i -n test test-statistics-db-statefulset-0 -- \
--         mariadb -uroot -pqwer1234 nexus < statistics-seed-data.sql
-- 멱등성: 여러 번 실행해도 안전 (기존 시드 정리 후 재생성)
--
-- monolith seed-data.sql과의 차이:
--  - user/pos_store_inventory 테이블 없음 (statistics 무관)
--  - store는 slim schema (storeName + isDeleted만)
--  - pos_pay는 @GeneratedValue 제거되어 idx 직접 명시 (100001~)
--  - 결제 분포 패턴은 동일 (3000 매장, 30000건, 피크타임 11~13시 40%)
-- ============================================================

-- ============================================================
-- 0. 기존 시드 데이터 정리 (FK 순서: 자식 → 부모)
-- ============================================================
SET FOREIGN_KEY_CHECKS = 0;

-- 결제 관련 (data.sql 시드 + 부하테스트 시드 둘 다 정리)
DELETE FROM pos_orders_item WHERE pos_pay_idx IN (SELECT pos_pay_idx FROM pos_pay WHERE paid_at >= CURDATE());
DELETE FROM pos_pay WHERE paid_at >= CURDATE();

-- 시드로 생성한 매장 정리 (data.sql의 1~5번은 유지, 6번 이후 삭제)
DELETE FROM store WHERE store_idx > 5;

-- pos_orders_item은 AUTO_INCREMENT 유지, 리셋만 (FK 영향 없음)
ALTER TABLE pos_orders_item AUTO_INCREMENT = 1;

SET FOREIGN_KEY_CHECKS = 1;

-- ============================================================
-- 1. 테스트용 매장 6~3000 생성 (slim schema: idx + storeName + isDeleted)
-- ============================================================
DELIMITER $$
DROP PROCEDURE IF EXISTS seed_stores$$
CREATE PROCEDURE seed_stores()
BEGIN
    DECLARE i INT DEFAULT 6;

    SET autocommit = 0;

    WHILE i <= 3000 DO
        INSERT IGNORE INTO store (store_idx, store_name, is_deleted)
        VALUES (
            i,
            CONCAT('더벤티 테스트', LPAD(i, 4, '0'), '점'),
            0
        );

        IF i MOD 500 = 0 THEN
            COMMIT;
        END IF;
        SET i = i + 1;
    END WHILE;

    COMMIT;
    SET autocommit = 1;
END$$
DELIMITER ;

CALL seed_stores();
DROP PROCEDURE IF EXISTS seed_stores;

-- ============================================================
-- 2. 대량 결제 데이터 생성 (오늘 날짜 30000건)
--    3000개 매장에 분산, 피크타임(11~13시)에 40% 집중
--    pos_pay_idx는 100001부터 (monolith 결제 idx와 충돌 회피)
-- ============================================================
DELIMITER $$
DROP PROCEDURE IF EXISTS seed_pos_pay$$
CREATE PROCEDURE seed_pos_pay()
BEGIN
    DECLARE i INT DEFAULT 1;
    DECLARE total INT DEFAULT 30000;
    DECLARE start_idx BIGINT DEFAULT 100000;
    DECLARE store_id INT;
    DECLARE pay_method VARCHAR(10);
    DECLARE pay_hour INT;
    DECLARE pay_minute INT;
    DECLARE pay_amount BIGINT;
    DECLARE menu_id INT;
    DECLARE qty INT;
    DECLARE paid DATETIME;
    DECLARE rand_val DOUBLE;
    DECLARE pay_id BIGINT;

    SET autocommit = 0;

    WHILE i <= total DO
        -- 매장 랜덤 선택 (1~3000)
        SET store_id = 1 + FLOOR(RAND() * 3000);

        -- 결제수단: CARD 70%, CASH 30%
        IF RAND() < 0.7 THEN SET pay_method = 'CARD';
        ELSE SET pay_method = 'CASH';
        END IF;

        -- 시간 분포: 피크(11~13시) 40%, 오전(8~10) 15%, 오후(14~17) 20%, 저녁(18~21) 25%
        SET rand_val = RAND();
        IF rand_val < 0.40 THEN
            SET pay_hour = 11 + FLOOR(RAND() * 3);
        ELSEIF rand_val < 0.55 THEN
            SET pay_hour = 8 + FLOOR(RAND() * 3);
        ELSEIF rand_val < 0.75 THEN
            SET pay_hour = 14 + FLOOR(RAND() * 4);
        ELSE
            SET pay_hour = 18 + FLOOR(RAND() * 4);
        END IF;
        SET pay_minute = FLOOR(RAND() * 60);

        SET paid = CONCAT(CURDATE(), ' ',
                         LPAD(pay_hour, 2, '0'), ':',
                         LPAD(pay_minute, 2, '0'), ':',
                         LPAD(FLOOR(RAND() * 60), 2, '0'));

        -- 메뉴 1~15 중 랜덤, 가격 매핑
        SET menu_id = 1 + FLOOR(RAND() * 15);
        SET qty = 1 + FLOOR(RAND() * 3);

        CASE menu_id
            WHEN 1  THEN SET pay_amount = 1500 * qty;
            WHEN 2  THEN SET pay_amount = 2500 * qty;
            WHEN 3  THEN SET pay_amount = 3000 * qty;
            WHEN 4  THEN SET pay_amount = 3500 * qty;
            WHEN 5  THEN SET pay_amount = 2500 * qty;
            WHEN 6  THEN SET pay_amount = 3000 * qty;
            WHEN 7  THEN SET pay_amount = 3000 * qty;
            WHEN 8  THEN SET pay_amount = 3500 * qty;
            WHEN 9  THEN SET pay_amount = 3500 * qty;
            WHEN 10 THEN SET pay_amount = 2500 * qty;
            WHEN 11 THEN SET pay_amount = 2500 * qty;
            WHEN 12 THEN SET pay_amount = 3000 * qty;
            WHEN 13 THEN SET pay_amount = 2000 * qty;
            WHEN 14 THEN SET pay_amount = 2000 * qty;
            WHEN 15 THEN SET pay_amount = 2000 * qty;
        END CASE;

        -- statistics의 pos_pay는 @GeneratedValue 없음 → idx 직접 지정
        SET pay_id = start_idx + i;

        INSERT INTO pos_pay (pos_pay_idx, store_idx, method, paid_at, pay_amount)
        VALUES (pay_id, store_id, pay_method, paid, pay_amount);

        -- pos_orders_item은 AUTO_INCREMENT (idx 자동 채번)
        INSERT INTO pos_orders_item (pos_pay_idx, menu_idx, quantity)
        VALUES (pay_id, menu_id, qty);

        -- 30% 확률로 추가 메뉴 아이템
        IF RAND() < 0.3 THEN
            SET menu_id = 1 + FLOOR(RAND() * 15);
            SET qty = 1;
            INSERT INTO pos_orders_item (pos_pay_idx, menu_idx, quantity)
            VALUES (pay_id, menu_id, qty);
        END IF;

        SET i = i + 1;

        -- 5000건마다 커밋
        IF i MOD 5000 = 0 THEN
            COMMIT;
        END IF;
    END WHILE;

    COMMIT;
    SET autocommit = 1;
END$$
DELIMITER ;

CALL seed_pos_pay();
DROP PROCEDURE IF EXISTS seed_pos_pay;

-- ============================================================
-- 확인 쿼리
-- ============================================================
SELECT '=== Statistics 시드 데이터 확인 ===' AS info;
SELECT COUNT(*) AS total_stores FROM store;
SELECT COUNT(*) AS total_pos_pay FROM pos_pay WHERE paid_at >= CURDATE();
SELECT COUNT(*) AS total_pos_orders_item FROM pos_orders_item;
SELECT HOUR(paid_at) AS h, COUNT(*) AS cnt
FROM pos_pay
WHERE paid_at >= CURDATE()
GROUP BY HOUR(paid_at)
ORDER BY h;
