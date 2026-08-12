-- ============================================================
-- Nexus Monolith 로컬 dev seed
-- 매장 100개 + 30일치 결제 + 발주 + 알림 (store0001 집중)
-- ============================================================
-- 실행:
--   docker exec -i <monolith-mariadb> mariadb -uroot -p<pw> nexus < seed-dev.sql
--   또는 IntelliJ DB tool 에서 직접 실행
-- 멱등성: 여러 번 실행 안전 (기존 시드 정리 후 재생성)
-- 비밀번호: 모두 password123 (bcrypt)
-- 로그인:
--   본사: admin@theventi.co.kr / password123
--   점주: store0001@theventi.co.kr ~ store0100@theventi.co.kr / password123
-- ============================================================

SET FOREIGN_KEY_CHECKS = 0;

-- 기존 시드 정리 (TRUNCATE: 전체 + AUTO_INCREMENT 리셋)
TRUNCATE TABLE delivery;
TRUNCATE TABLE store_notification;
TRUNCATE TABLE head_notification;
TRUNCATE TABLE pos_orders_item;
TRUNCATE TABLE pos_pay;
TRUNCATE TABLE orders_item;
TRUNCATE TABLE orders;
TRUNCATE TABLE store_inventory;
TRUNCATE TABLE pos_store_inventory;
TRUNCATE TABLE head_inventory;
TRUNCATE TABLE menu_item;
TRUNCATE TABLE menu;
TRUNCATE TABLE menu_category;
TRUNCATE TABLE product;
TRUNCATE TABLE category;
TRUNCATE TABLE store;
TRUNCATE TABLE user;

-- TRUNCATE 가 AUTO_INCREMENT 자동 리셋

SET FOREIGN_KEY_CHECKS = 1;


-- ============================================================
-- 1. 본사 유저 1명 (admin@theventi.co.kr, password=password123)
-- ============================================================
INSERT INTO user (email, password, user_name, tel, role, is_deleted) VALUES
('admin@theventi.co.kr', '{bcrypt}$2b$10$lvGJMgshX87086lRcSRGT.NXXzXyKXIXc2PgpAgAeK10LKeWfW2MS',
 '본사 관리자', '010-0000-0001', 'ADMIN', false);


-- ============================================================
-- 2. 가맹점 유저 100명 + 매장 100개 (store0001 ~ store0100)
-- ============================================================
DELIMITER $$
DROP PROCEDURE IF EXISTS seed_stores$$
CREATE PROCEDURE seed_stores()
BEGIN
    DECLARE i INT DEFAULT 1;
    DECLARE user_id BIGINT;
    DECLARE pw VARCHAR(255) DEFAULT '{bcrypt}$2b$10$lvGJMgshX87086lRcSRGT.NXXzXyKXIXc2PgpAgAeK10LKeWfW2MS';

    WHILE i <= 100 DO
        -- 가맹점 유저 — 점주 이름 100명 (흔한 한국 이름)
        INSERT INTO user (email, password, user_name, tel, role, is_deleted) VALUES (
            CONCAT('store', LPAD(i, 4, '0'), '@theventi.co.kr'),
            pw,
            ELT(i,
                '김민준','이서연','박지훈','최예린','정현우','강서윤','조하늘','윤도현','장은지','임우진',
                '한지민','신유리','오재호','서지연','권태영','황민서','안재현','송미경','류성호','홍지수',
                '전다은','고민혁','문수아','양준영','손지원',
                '배현철','백은영','허준석','유나래','남기훈','김도윤','이채원','박성민','최유진','정태우',
                '강민지','조은서','윤재훈','장수빈','임혜원',
                '한주아','신동현','오세준','서민호','권나연','황지호','안소영','송원석','류지은','홍명진',
                '전유미','고승준','문지영','양현준','손가은',
                '배지혜','백승호','허민재','유다은','남재훈',
                '김지환','이수현','박혜진',
                '최성현','정아름','강기범',
                '조현지','윤서영','장민석',
                '임채영',
                '한태규','신경민',
                '오지수','서동민','권은혁','황재성','안나영',
                '송영준','류미경','홍재훈',
                '전세진','고은비','문재호','양다인',
                '손영민','배경수','백지원','허영주',
                '유진우','남수정','김지원','이도현',
                '박서진','최우석','정나래',
                '강석민','조민지','윤도연',
                '장하영','임소미'
            ),
            CONCAT('010-1', LPAD(i, 4, '0'), '-0000'),
            'STORE',
            false
        );
        SET user_id = LAST_INSERT_ID();

        -- 매장 (더벤티 <지역명>점, 100개 실제 지역) — 매장명과 매칭되는 지역 도로명
        INSERT INTO store (
            store_name, address, address_detail, file_path,
            business, postcode, created_at, is_deleted, user_idx
        ) VALUES (
            CONCAT('더벤티 ', ELT(i,
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
            ELT(i,
                '서울 강남구 테헤란로','서울 강동구 천호대로','서울 강서구 공항대로','서울 강북구 도봉로','서울 관악구 신림로',
                '서울 광진구 능동로','서울 구로구 디지털로','서울 금천구 가산디지털로','서울 노원구 동일로','서울 도봉구 마들로',
                '서울 동대문구 왕산로','서울 동작구 노들로','서울 마포구 월드컵북로','서울 서대문구 신촌로','서울 서초구 강남대로',
                '서울 성동구 왕십리로','서울 성북구 정릉로','서울 송파구 올림픽로','서울 양천구 목동로','서울 영등포구 영등포로',
                '서울 용산구 한강대로','서울 은평구 통일로','서울 종로구 종로','서울 중구 명동길','서울 중랑구 망우로',
                '경기 수원시 인계로','경기 성남시 분당구 판교로','경기 고양시 일산동구 중앙로','경기 용인시 처인구 백옥대로','경기 부천시 길주로',
                '경기 안산시 단원구 중앙대로','경기 안양시 만안구 안양로','경기 평택시 중앙로','경기 시흥시 중심상가로','경기 광명시 광명로',
                '경기 의정부시 평화로','경기 김포시 김포대로','경기 화성시 동탄대로','경기 남양주시 경춘로','경기 군포시 군포로',
                '부산 해운대구 해운대로','부산 부산진구 가야대로','부산 동래구 동래시장로','부산 사상구 백양대로','부산 강서구 명지국제대로',
                '부산 사하구 다대로','부산 금정구 부산대학로','부산 기장군 기장대로','부산 남구 수영로','부산 북구 만덕대로',
                '대구 중구 동성로','대구 수성구 들안로','대구 달서구 달구벌대로','대구 달성군 비슬로','대구 동구 동대구로',
                '인천 중구 차이나타운로','인천 미추홀구 인주대로','인천 연수구 송도과학로','인천 남동구 구월로','인천 부평구 부평대로',
                '광주 북구 금남로','광주 서구 상무중앙로','광주 남구 봉선로',
                '대전 유성구 대학로','대전 서구 둔산대로','대전 동구 대전로',
                '울산 남구 삼산로','울산 동구 방어진순환도로','울산 북구 산업로',
                '세종특별자치시 한누리대로',
                '제주 제주시 광양로','제주 서귀포시 일주동로',
                '강원 춘천시 명동길','강원 원주시 원일로','강원 강릉시 경강로','강원 속초시 중앙로','강원 평창군 진부중앙로',
                '충북 청주시 상당구 상당로','충북 충주시 중앙로','충북 제천시 중앙로',
                '충남 천안시 동남구 만남로','충남 아산시 시민로','충남 공주시 봉황로','충남 서산시 호수공원로',
                '경북 포항시 북구 중흥로','경북 경주시 화랑로','경북 안동시 경동로','경북 구미시 송정대로',
                '경남 창원시 의창구 중앙대로','경남 김해시 김해대로','경남 진주시 진주대로','경남 양산시 양산대로',
                '전북 전주시 완산구 전주객사로','전북 군산시 미장로','전북 익산시 익산대로',
                '전남 여수시 중앙로','전남 순천시 장명로','전남 목포시 영산로',
                '서울 강서구 하늘길','서울 중구 세종대로'
            ),
            CONCAT(i, '길 ', ((i * 7) % 100) + 1, '번지'),
            'store-default.png',
            CONCAT('100-00-', LPAD(i, 5, '0')),
            LPAD(i, 5, '0'),
            DATE_SUB(NOW(), INTERVAL (100 - i) DAY),
            false,
            user_id
        );
        SET i = i + 1;
    END WHILE;
END$$
DELIMITER ;

CALL seed_stores();
DROP PROCEDURE seed_stores;


-- ============================================================
-- 3. 메뉴 카테고리 4개 + 메뉴 15개
-- ============================================================
INSERT INTO menu_category (menu_category_name, is_deleted) VALUES
('음료', false),    -- idx 1
('디저트', false),  -- idx 2
('베이커리', false),-- idx 3
('푸드', false);    -- idx 4

INSERT INTO menu (menu_name, price, img_path, is_deleted, menu_category_idx) VALUES
-- 음료 (5)
('아메리카노',        3000, 'menu-default.png', false, 1),
('카페라떼',          4000, 'menu-default.png', false, 1),
('카푸치노',          4500, 'menu-default.png', false, 1),
('바닐라라떼',        4500, 'menu-default.png', false, 1),
('자몽에이드',        5500, 'menu-default.png', false, 1),
-- 디저트 (3)
('티라미수',          6000, 'menu-default.png', false, 2),
('치즈케이크',        6500, 'menu-default.png', false, 2),
('마카롱',            2500, 'menu-default.png', false, 2),
-- 베이커리 (4)
('크로와상',          3500, 'menu-default.png', false, 3),
('소금빵',            3000, 'menu-default.png', false, 3),
('초코머핀',          3500, 'menu-default.png', false, 3),
('베이글',            4000, 'menu-default.png', false, 3),
-- 푸드 (3)
('샌드위치',          7000, 'menu-default.png', false, 4),
('파니니',            8000, 'menu-default.png', false, 4),
('샐러드',            8500, 'menu-default.png', false, 4);


-- ============================================================
-- 4. 본사 상품 카테고리 (재료) + 상품
-- ============================================================
INSERT INTO category (category_name, is_deleted) VALUES
('원두',        false),  -- idx 1
('유제품',      false),  -- idx 2
('베이커리재료',false),  -- idx 3
('과일/시럽',   false),  -- idx 4
('식자재',      false);  -- idx 5

INSERT INTO product (
    product_name, product_unit, max_stock, min_stock, unit_price, danger_days, is_deleted, category_idx
) VALUES
-- 원두 (3)
('원두 A',     'kg', 50,  10, 25000, '7',  false, 1),
('원두 B',     'kg', 50,  10, 28000, '7',  false, 1),
('디카페인',   'kg', 30,   5, 32000, '7',  false, 1),
-- 유제품 (3)
('우유',       'L',  100, 20,  3500, '3',  false, 2),
('두유',       'L',  50,  10,  4500, '5',  false, 2),
('생크림',     'L',  30,   5,  6500, '3',  false, 2),
-- 베이커리재료 (4)
('밀가루',     'kg', 100, 20,  4500, '14', false, 3),
('버터',       'kg', 50,  10, 15000, '7',  false, 3),
('초콜릿',     'kg', 30,   5, 18000, '14', false, 3),
('치즈',       'kg', 30,   5, 22000, '7',  false, 3),
-- 과일/시럽 (3)
('자몽',       'kg', 30,   5,  8000, '5',  false, 4),
('바닐라시럽', 'L',  30,   5, 12000, '30', false, 4),
('초콜릿시럽', 'L',  30,   5, 11000, '30', false, 4),
-- 식자재 (2)
('빵',         'EA', 100, 20,  1500, '3',  false, 5),
('샐러드믹스', 'kg', 30,   5,  9000, '3',  false, 5);


-- ============================================================
-- 4-1. 메뉴 레시피 (menu_item) — 메뉴 15개가 product 1~15 만 참조
-- POS 결제 시 menu → menu_item → product 차감 (pos_store_inventory FIFO)
-- ============================================================
INSERT INTO menu_item (menu_idx, product_idx, quantity, menu_unit) VALUES
-- 1. 아메리카노: 원두A 20g
(1, 1, 20, 'g'),
-- 2. 카페라떼: 원두A 20g, 우유 200ml
(2, 1, 20, 'g'),
(2, 4, 200, 'ml'),
-- 3. 카푸치노: 원두A 20g, 우유 150ml
(3, 1, 20, 'g'),
(3, 4, 150, 'ml'),
-- 4. 바닐라라떼: 원두A 20g, 우유 200ml, 바닐라시럽 20ml
(4, 1, 20, 'g'),
(4, 4, 200, 'ml'),
(4, 12, 20, 'ml'),
-- 5. 자몽에이드: 자몽 30g
(5, 11, 30, 'g'),
-- 6. 티라미수: 우유 50ml, 초콜릿 20g, 빵 1ea
(6, 4, 50, 'ml'),
(6, 9, 20, 'g'),
(6, 14, 1, 'EA'),
-- 7. 치즈케이크: 치즈 30g, 빵 1ea
(7, 10, 30, 'g'),
(7, 14, 1, 'EA'),
-- 8. 마카롱: 초콜릿 10g
(8, 9, 10, 'g'),
-- 9. 크로와상: 밀가루 50g, 버터 10g
(9, 7, 50, 'g'),
(9, 8, 10, 'g'),
-- 10. 소금빵: 밀가루 50g, 버터 5g
(10, 7, 50, 'g'),
(10, 8, 5, 'g'),
-- 11. 초코머핀: 밀가루 30g, 초콜릿 15g, 우유 30ml
(11, 7, 30, 'g'),
(11, 9, 15, 'g'),
(11, 4, 30, 'ml'),
-- 12. 베이글: 밀가루 60g
(12, 7, 60, 'g'),
-- 13. 샌드위치: 빵 1ea, 치즈 20g, 샐러드믹스 20g
(13, 14, 1, 'EA'),
(13, 10, 20, 'g'),
(13, 15, 20, 'g'),
-- 14. 파니니: 빵 1ea, 치즈 30g, 샐러드믹스 15g
(14, 14, 1, 'EA'),
(14, 10, 30, 'g'),
(14, 15, 15, 'g'),
-- 15. 샐러드: 샐러드믹스 50g, 치즈 10g
(15, 15, 50, 'g'),
(15, 10, 10, 'g');


-- ============================================================
-- 5. 매장별 재고 (pos_store_inventory) — 매장당 상품 15개씩, 다양한 상태
-- 일부 매장 일부 상품 LOW/CRITICAL 분포
-- ============================================================
DELIMITER $$
DROP PROCEDURE IF EXISTS seed_inventory$$
CREATE PROCEDURE seed_inventory()
BEGIN
    DECLARE s INT DEFAULT 1;
    DECLARE p INT;
    DECLARE qty INT;
    DECLARE st VARCHAR(20);

    WHILE s <= 100 DO
        SET p = 1;
        WHILE p <= 15 DO
            -- 결정적이지만 다양한 분포 (매장 + 상품 idx 기반)
            -- NORMAL: 충분 (1500~2000), LOW/CRITICAL 만 위험 표시용
            SET qty = 1500 + ((s * 7 + p * 13) % 500);  -- 1500~2000 정상 (재고 풍부)
            SET st = 'NORMAL';

            -- 분포: CRITICAL ~10%, LOW ~14% (위험 표시용 — 결제는 NORMAL 매장만)
            IF (s + p) % 10 = 0 THEN
                SET qty = 5;
                SET st = 'CRITICAL';
            ELSEIF (s + p) % 7 = 0 THEN
                SET qty = 15;
                SET st = 'LOW';
            END IF;

            -- store0001 특별 처리: 위험 재고 표시 (LOW 3, CRITICAL 2)
            -- 단 결제에 쓰이는 product (1,4,7,8,9,10,11,12,14,15) 는 NORMAL 충분 보장
            -- 결제 미사용 product (2,3,5,6,13) 만 위험 표시
            IF s = 1 THEN
                IF p IN (2, 6) THEN
                    SET qty = 3;
                    SET st = 'CRITICAL';
                ELSEIF p IN (3, 5, 13) THEN
                    SET qty = 10;
                    SET st = 'LOW';
                ELSE
                    SET qty = 2000;
                    SET st = 'NORMAL';
                END IF;
            END IF;

            INSERT INTO pos_store_inventory (
                count, status, manufactured_date, store_idx, product_idx
            ) VALUES (
                qty,
                st,
                DATE_SUB(NOW(), INTERVAL ((s + p) % 5) DAY),
                s,
                p
            );

            SET p = p + 1;
        END WHILE;
        SET s = s + 1;
    END WHILE;
END$$
DELIMITER ;

CALL seed_inventory();
DROP PROCEDURE seed_inventory;


-- ============================================================
-- 5-0. 가맹점 재고 (store_inventory) — 가맹점 대시보드 위험 재고 KPI/목록
-- pos_store_inventory 와 별도 테이블 (pos = POS 결제 차감용, store = 위험도 추적 + 발주 관리)
-- pos_store_inventory 데이터 그대로 복제 + avg_stock (평균 재고) 추가
-- ============================================================
INSERT INTO store_inventory (count, status, manufactured_date, avg_stock, store_idx, product_idx, orders_item_idx)
SELECT
    count,
    status,
    manufactured_date,
    -- avg_stock = 정상 평균 (50, status 가 LOW/CRITICAL 이면 count 보다 큼 = 위험도 표시)
    CASE
        WHEN status = 'CRITICAL' THEN 50
        WHEN status = 'LOW'      THEN 40
        ELSE count
    END AS avg_stock,
    store_idx,
    product_idx,
    NULL  -- orders_item_idx: 시드는 발주 추적 X
FROM pos_store_inventory;


-- ============================================================
-- 5-1. 본사 재고 (head_inventory) — 본사 대시보드 KPI + 목록 데이터
-- product 15개 각각 1~3건 (NORMAL/LOW/CRITICAL 분포)
-- ============================================================
-- product 당 head_inventory 1건만 (findByProductIdxForUpdate 가 unique 1건 기대)
-- count 는 모두 2000 (출고 가능 충분) + status 만 다양 (위험도 표시용)
INSERT INTO head_inventory (count, status, manufactured_date, product_idx) VALUES
(2000, 'NORMAL',   DATE_SUB(NOW(), INTERVAL 2 DAY),  1),
(2000, 'NORMAL',   DATE_SUB(NOW(), INTERVAL 3 DAY),  2),
(2000, 'LOW',      DATE_SUB(NOW(), INTERVAL 8 DAY),  3),   -- LOW 표시
(2000, 'NORMAL',   DATE_SUB(NOW(), INTERVAL 4 DAY),  4),
(2000, 'NORMAL',   DATE_SUB(NOW(), INTERVAL 5 DAY),  5),
(2000, 'LOW',      DATE_SUB(NOW(), INTERVAL 9 DAY),  6),   -- LOW 표시
(2000, 'CRITICAL', DATE_SUB(NOW(), INTERVAL 15 DAY), 7),   -- CRITICAL 표시
(2000, 'LOW',      DATE_SUB(NOW(), INTERVAL 10 DAY), 8),   -- LOW 표시
(2000, 'CRITICAL', DATE_SUB(NOW(), INTERVAL 16 DAY), 9),   -- CRITICAL 표시
(2000, 'CRITICAL', DATE_SUB(NOW(), INTERVAL 13 DAY), 10),  -- CRITICAL 표시
(2000, 'LOW',      DATE_SUB(NOW(), INTERVAL 11 DAY), 11),  -- LOW 표시
(2000, 'CRITICAL', DATE_SUB(NOW(), INTERVAL 18 DAY), 12),  -- CRITICAL 표시
(2000, 'LOW',      DATE_SUB(NOW(), INTERVAL 12 DAY), 13),  -- LOW 표시
(2000, 'CRITICAL', DATE_SUB(NOW(), INTERVAL 14 DAY), 14),  -- CRITICAL 표시
(2000, 'NORMAL',   DATE_SUB(NOW(), INTERVAL 6 DAY),  15);


-- ============================================================
-- 6. 결제 (pos_pay + pos_orders_item) — 30일치, 매장당 일 5건
-- 시간대 분산: 09, 12, 15, 18, 21시
-- 금액 결정적 (매장 + 일자 + 건 번호 기반) → 통계 MSA daily_*_sales 와 일치 위해
-- ============================================================
DELIMITER $$
DROP PROCEDURE IF EXISTS seed_payments$$
CREATE PROCEDURE seed_payments()
BEGIN
    DECLARE d INT DEFAULT 0;       -- 0 = 오늘, 1~30 = 어제부터
    DECLARE s INT;                 -- store idx
    DECLARE i INT;                 -- pay idx (1~5)
    DECLARE hr INT;                -- 시간대
    DECLARE amount BIGINT;
    DECLARE method VARCHAR(10);
    DECLARE pay_id BIGINT;
    DECLARE menu_id INT;
    DECLARE qty INT;

    WHILE d <= 30 DO
        SET s = 1;
        WHILE s <= 100 DO
            SET i = 1;
            WHILE i <= 7 DO
                -- 시간대: 09, 11, 13, 15, 17, 19, 21 (2시간 간격)
                SET hr = CASE i WHEN 1 THEN 9 WHEN 2 THEN 11 WHEN 3 THEN 13 WHEN 4 THEN 15 WHEN 5 THEN 17 WHEN 6 THEN 19 ELSE 21 END;
                -- 금액 결정적 (매장 idx, 일자, 건 번호 기반) — d 포함하여 일별 변동성
                SET amount = 1000 + ((s * 100 + i * 1500 + d * 700) % 9001);  -- 1000 ~ 10000
                -- 결제 수단 ~71% CARD, ~29% CASH
                SET method = IF (i <= 5, 'CARD', 'CASH');

                INSERT INTO pos_pay (method, paid_at, pay_amount, store_idx) VALUES (
                    method,
                    DATE_ADD(DATE_SUB(CURDATE(), INTERVAL d DAY), INTERVAL hr HOUR),
                    amount,
                    s
                );
                SET pay_id = LAST_INSERT_ID();

                -- 결제마다 1~2개 메뉴 항목 (결정적)
                SET menu_id = 1 + ((s + d + i) % 15);   -- 1~15
                SET qty = 1 + (i % 2);                  -- 1 or 2
                INSERT INTO pos_orders_item (quantity, menu_idx, pos_pay_idx) VALUES (qty, menu_id, pay_id);

                -- 50% 결제는 메뉴 2개 (다양성)
                IF i % 2 = 0 THEN
                    SET menu_id = 1 + ((s + d + i + 7) % 15);
                    INSERT INTO pos_orders_item (quantity, menu_idx, pos_pay_idx) VALUES (1, menu_id, pay_id);
                END IF;

                SET i = i + 1;
            END WHILE;
            SET s = s + 1;
        END WHILE;
        SET d = d + 1;
    END WHILE;
END$$
DELIMITER ;

CALL seed_payments();
DROP PROCEDURE seed_payments;


-- ============================================================
-- 7. 발주 (orders + orders_item)
-- store0001 집중 (50건) + 나머지 99 매장 각 2~3건 = 총 ~300건
-- ============================================================
DELIMITER $$
DROP PROCEDURE IF EXISTS seed_orders$$
CREATE PROCEDURE seed_orders()
BEGIN
    DECLARE i INT;
    DECLARE s INT;
    DECLARE d INT;
    DECLARE order_type VARCHAR(20);
    DECLARE order_status VARCHAR(20);
    DECLARE total_price BIGINT;
    DECLARE order_id BIGINT;
    DECLARE p INT;
    DECLARE cnt INT;
    DECLARE is_danger BOOLEAN;
    DECLARE reason_val VARCHAR(500);
    DECLARE day_offset INT;

    -- ========================================================
    -- store0001 (store_idx=1) 50건 집중
    -- status 분포 (10건씩): WAITING / APPROVE / REJECT / CONFIRMED / CANCELLED
    -- is_danger 별도 분포: i%4=0 → 12건 (status 와 무관)
    -- reason: AUTO + AI 추천 사유 / is_danger + 이상 발주 사유
    -- created_at: i<=7 → 오늘 (대시보드 KPI 의미)
    -- ========================================================
    SET i = 1;
    WHILE i <= 50 DO
        -- 5건 묶음별 status (각 10건)
        -- 단, 오늘 created_at 인 AUTO 는 모두 WAITING 으로 강제 (KPI = 목록 일치)
        SET day_offset = IF (i <= 7, 0, i - 7);
        IF i % 5 = 1 THEN
            SET order_type = 'MANUAL'; SET order_status = 'CONFIRMED';
        ELSEIF i % 5 = 2 THEN
            SET order_type = 'AUTO';   SET order_status = 'WAITING';
        ELSEIF i % 5 = 3 THEN
            SET order_type = 'AUTO';
            SET order_status = IF (day_offset = 0, 'WAITING', 'APPROVE');
        ELSEIF i % 5 = 4 THEN
            SET order_type = 'AUTO';
            SET order_status = IF (day_offset = 0, 'WAITING', 'REJECT');
        ELSE
            SET order_type = 'MANUAL'; SET order_status = 'CANCELLED';
        END IF;

        -- is_danger 별도 (status 와 분리, ~24% 이상 발주)
        SET is_danger = IF (i % 4 = 0, true, false);

        -- reason: is_danger 또는 AUTO 면 사유 명시
        IF is_danger THEN
            SET reason_val = ELT(1 + (i % 3),
                '평균 대비 200% 초과 - 이상 발주 감지',
                '발주 패턴 이상 - 검토 필요',
                '재고 급증 감지 - 확인 필요'
            );
        ELSEIF order_type = 'AUTO' THEN
            SET reason_val = ELT(1 + (i % 5),
                'AI 추천: 안전 재고 미달 감지',
                'AI 추천: 발주 주기 도래',
                'AI 추천: 재고 부족 예측',
                'AI 추천: 평균 소비량 분석 기반',
                'AI 추천: 계절성 수요 증가 예측'
            );
        ELSE
            SET reason_val = NULL;
        END IF;

        -- 일부 발주 오늘 (i<=7), 나머지 i-7 일 전
        SET day_offset = IF (i <= 7, 0, i - 7);
        SET total_price = 50000 + (i * 1000);

        INSERT INTO orders (
            price, order_type, order_status, is_danger, reason, created_at, store_idx
        ) VALUES (
            total_price, order_type, order_status, is_danger, reason_val,
            DATE_SUB(NOW(), INTERVAL day_offset DAY),
            1
        );
        SET order_id = LAST_INSERT_ID();

        -- 발주 항목 5개 (다양성)
        SET p = 1;
        WHILE p <= 5 DO
            SET cnt = 5 + ((i + p) % 10);
            -- processed: APPROVE 만 true (이미 처리 완료). CONFIRMED 는 false (batch 가 처리할 대상)
            INSERT INTO orders_item (count, processed, orders_idx, product_idx) VALUES (
                cnt,
                IF (order_status = 'APPROVE', true, false),
                order_id,
                1 + ((i + p * 3) % 15)
            );
            SET p = p + 1;
        END WHILE;
        SET i = i + 1;
    END WHILE;

    -- ========================================================
    -- 나머지 99 매장 각 3건 (s=2~100)
    -- status 분포 다양화 (대시보드 그래프 의미)
    -- 매장 % 7 = 0 → is_danger=true (14매장)
    -- 매장 % 5 = 0 → created_at 오늘 (KPI 의미, 20매장)
    -- ========================================================
    SET s = 2;
    WHILE s <= 100 DO
        SET d = 1;
        WHILE d <= 3 DO
            -- order_type
            SET order_type = IF (d <= 2, 'AUTO', 'MANUAL');

            -- day_offset 먼저 계산 (매장 %5=0 인 20매장 → 오늘)
            SET day_offset = IF (s % 5 = 0, 0, (s + d) % 30);

            -- status 다양화 (오늘 created_at + AUTO 는 모두 WAITING — KPI = 목록 일치)
            IF d = 1 THEN
                IF day_offset = 0 THEN
                    SET order_status = 'WAITING';
                ELSE
                    SET order_status = ELT(1 + (s % 5),
                        'APPROVE', 'WAITING', 'REJECT', 'CONFIRMED', 'CANCELLED');
                END IF;
            ELSEIF d = 2 THEN
                IF day_offset = 0 THEN
                    SET order_status = 'WAITING';
                ELSE
                    SET order_status = ELT(1 + ((s + 1) % 4),
                        'APPROVE', 'WAITING', 'CONFIRMED', 'REJECT');
                END IF;
            ELSE
                -- d=3 는 MANUAL 이라 자동 발주 제안 KPI 와 무관
                SET order_status = ELT(1 + ((s + 2) % 3),
                    'CONFIRMED', 'APPROVE', 'CANCELLED');
            END IF;

            -- is_danger: 매장 idx % 7 = 0 (14매장) 의 d=1 발주
            SET is_danger = IF (s % 7 = 0 AND d = 1, true, false);

            -- reason
            IF is_danger THEN
                SET reason_val = '재고 급증 이상 감지';
            ELSEIF order_type = 'AUTO' THEN
                SET reason_val = ELT(1 + (s % 5),
                    'AI 추천: 안전 재고 미달 감지',
                    'AI 추천: 발주 주기 도래',
                    'AI 추천: 재고 부족 예측',
                    'AI 추천: 평균 소비량 분석',
                    'AI 추천: 계절성 수요 증가'
                );
            ELSE
                SET reason_val = NULL;
            END IF;

            -- created_at: 매장 % 5 = 0 인 20매장 → 오늘 (대시보드 KPI)
            SET day_offset = IF (s % 5 = 0, 0, (s + d) % 30);

            SET total_price = 30000 + (s * 100 + d * 1000);

            INSERT INTO orders (price, order_type, order_status, is_danger, reason, created_at, store_idx) VALUES (
                total_price, order_type, order_status, is_danger, reason_val,
                DATE_SUB(NOW(), INTERVAL day_offset DAY),
                s
            );
            SET order_id = LAST_INSERT_ID();

            -- 항목 3개 (processed: APPROVE 만 true. CONFIRMED 는 false → batch 처리 대상)
            INSERT INTO orders_item (count, processed, orders_idx, product_idx) VALUES
              (5 + (s % 5),       IF (order_status = 'APPROVE', true, false), order_id, 1 + (s % 15)),
              (3 + ((s + 1) % 7), IF (order_status = 'APPROVE', true, false), order_id, 1 + ((s + 5) % 15)),
              (4 + ((s + 2) % 6), IF (order_status = 'APPROVE', true, false), order_id, 1 + ((s + 10) % 15));
            SET d = d + 1;
        END WHILE;
        SET s = s + 1;
    END WHILE;
END$$
DELIMITER ;

CALL seed_orders();
DROP PROCEDURE seed_orders;

-- orders.price 정합성: orders_item 의 count × product.unit_price 합으로 맞춤
-- (seed_orders 의 임의 price 값으로 인한 목록/상세 표시 불일치 방지)
UPDATE orders o
SET o.price = (
    SELECT COALESCE(SUM(oi.count * p.unit_price), 0)
    FROM orders_item oi
    JOIN product p ON oi.product_idx = p.product_idx
    WHERE oi.orders_idx = o.orders_idx
);


-- ============================================================
-- 7-1. 배송 (delivery) — 진행 중인 발주 (APPROVE/CONFIRMED) 에 1:1 매핑
-- DeliveryStatus 분포: READY 25% / DELIVERYING 25% / DELIVERED 35% / DELAY 15%
-- ============================================================
INSERT INTO delivery (delivery_status, delay_reason, dep_date, est_des_date, delivered_date, orders_idx)
SELECT
    CASE
        WHEN o.orders_idx % 20 < 3 THEN 'READY'
        WHEN o.orders_idx % 20 < 8 THEN 'DELIVERYING'
        WHEN o.orders_idx % 20 < 15 THEN 'DELIVERED'
        WHEN o.orders_idx % 20 < 17 THEN 'START'
        ELSE 'DELAY'
    END,
    -- delay_reason
    CASE
        WHEN o.orders_idx % 20 >= 17 THEN
            ELT(1 + (o.orders_idx % 4),
                '교통 체증으로 지연', '기상 악화로 지연',
                '차량 점검으로 지연', '배송 인력 부족')
        ELSE NULL
    END,
    -- 출발일
    DATE_SUB(o.created_at, INTERVAL -1 DAY),
    -- 예상 도착일
    DATE_ADD(o.created_at, INTERVAL 2 DAY),
    -- 실제 도착일 (DELIVERED 만)
    CASE WHEN o.orders_idx % 20 >= 8 AND o.orders_idx % 20 < 15
         THEN DATE_ADD(o.created_at, INTERVAL 2 DAY)
         ELSE NULL END,
    o.orders_idx
FROM orders o
WHERE o.order_status IN ('APPROVE', 'CONFIRMED');


-- ============================================================
-- 8. 알림 (head_notification + store_notification)
-- store_notification 은 store0001 (store_idx=1) 에 집중
-- ============================================================

-- 본사 알림 (10건)
INSERT INTO head_notification (type, title, content, is_read, created_at) VALUES
('LOW_STOCK',      '재고 부족 알림',   '원두 A 재고가 부족합니다.',     false, DATE_SUB(NOW(), INTERVAL 1 HOUR)),
('LOW_STOCK',      '재고 부족 알림',   '우유 재고가 부족합니다.',       false, DATE_SUB(NOW(), INTERVAL 3 HOUR)),
('EXPIRY',         '유통기한 임박',    '버터 3일 후 유통기한 만료.',    false, DATE_SUB(NOW(), INTERVAL 5 HOUR)),
('ABNORMAL_ORDER', '이상 발주 감지',   '스토어0010 의 발주량 급증.',    false, DATE_SUB(NOW(), INTERVAL 8 HOUR)),
('DELIVERY_DELAY', '배송 지연',        '스토어0023 배송이 지연됩니다.', true,  DATE_SUB(NOW(), INTERVAL 1 DAY)),
('DELIVERY_START', '배송 시작',        '스토어0045 배송이 시작됨.',     true,  DATE_SUB(NOW(), INTERVAL 1 DAY)),
('DELIVERED',      '배송 완료',        '스토어0067 배송이 완료됨.',     true,  DATE_SUB(NOW(), INTERVAL 2 DAY)),
('LOW_STOCK',      '재고 부족 알림',   '치즈 재고가 부족합니다.',       true,  DATE_SUB(NOW(), INTERVAL 2 DAY)),
('EXPIRY',         '유통기한 임박',    '생크림 1일 후 유통기한 만료.',  true,  DATE_SUB(NOW(), INTERVAL 3 DAY)),
('ABNORMAL_ORDER', '이상 발주 감지',   '스토어0089 의 발주량 급증.',    true,  DATE_SUB(NOW(), INTERVAL 4 DAY));

-- 가맹점 알림 — store0001 (store_idx=1) 집중 (15건)
INSERT INTO store_notification (type, title, content, is_read, created_at, store_idx) VALUES
('LOW_STOCK',      '재고 부족',        '원두 A 재고가 부족합니다.',     false, DATE_SUB(NOW(), INTERVAL 30 MINUTE), 1),
('LOW_STOCK',      '재고 부족',        '우유 재고가 부족합니다.',       false, DATE_SUB(NOW(), INTERVAL 2 HOUR),  1),
('EXPIRY',         '유통기한 임박',    '버터 3일 후 만료.',             false, DATE_SUB(NOW(), INTERVAL 4 HOUR),  1),
('EXPIRY',         '유통기한 임박',    '생크림 1일 후 만료.',           false, DATE_SUB(NOW(), INTERVAL 6 HOUR),  1),
('DELIVERY_START', '배송 시작',        '자동 발주 배송이 시작됨.',      false, DATE_SUB(NOW(), INTERVAL 8 HOUR),  1),
('DELIVERY_DELAY', '배송 지연',        '배송이 지연됩니다.',            false, DATE_SUB(NOW(), INTERVAL 1 DAY),   1),
('DELIVERED',      '배송 완료',        '발주 #1 배송 완료됨.',          true,  DATE_SUB(NOW(), INTERVAL 1 DAY),   1),
('ABNORMAL_ORDER', '이상 발주',        '발주량 급증 감지됨.',           true,  DATE_SUB(NOW(), INTERVAL 2 DAY),   1),
('LOW_STOCK',      '재고 부족',        '치즈 재고가 부족합니다.',       true,  DATE_SUB(NOW(), INTERVAL 2 DAY),   1),
('EXPIRY',         '유통기한 임박',    '빵 2일 후 만료.',               true,  DATE_SUB(NOW(), INTERVAL 3 DAY),   1),
('DELIVERY_START', '배송 시작',        '발주 #5 배송 시작.',            true,  DATE_SUB(NOW(), INTERVAL 4 DAY),   1),
('DELIVERED',      '배송 완료',        '발주 #5 배송 완료.',            true,  DATE_SUB(NOW(), INTERVAL 4 DAY),   1),
('LOW_STOCK',      '재고 부족',        '바닐라시럽 재고 부족.',         true,  DATE_SUB(NOW(), INTERVAL 5 DAY),   1),
('DELIVERED',      '배송 완료',        '발주 #10 배송 완료.',           true,  DATE_SUB(NOW(), INTERVAL 6 DAY),   1),
('LOW_STOCK',      '재고 부족',        '초콜릿 재고 부족.',             true,  DATE_SUB(NOW(), INTERVAL 7 DAY),   1);

-- 다른 매장에도 가벼운 알림 (랜덤 분포)
DELIMITER $$
DROP PROCEDURE IF EXISTS seed_other_store_notifications$$
CREATE PROCEDURE seed_other_store_notifications()
BEGIN
    DECLARE s INT DEFAULT 2;

    WHILE s <= 100 DO
        -- 매장당 2~3건
        INSERT INTO store_notification (type, title, content, is_read, created_at, store_idx) VALUES
        ('LOW_STOCK', '재고 부족', '재고 부족 알림입니다.', false, DATE_SUB(NOW(), INTERVAL (s % 12) HOUR), s),
        ('DELIVERY_START', '배송 시작', '배송이 시작됐습니다.', true,  DATE_SUB(NOW(), INTERVAL (s % 5)  DAY), s);

        IF s % 3 = 0 THEN
            INSERT INTO store_notification (type, title, content, is_read, created_at, store_idx) VALUES
            ('EXPIRY', '유통기한 임박', '유통기한이 임박했습니다.', false, DATE_SUB(NOW(), INTERVAL (s % 24) HOUR), s);
        END IF;

        SET s = s + 1;
    END WHILE;
END$$
DELIMITER ;

CALL seed_other_store_notifications();
DROP PROCEDURE seed_other_store_notifications;


-- ============================================================
-- 완료 확인 (SELECT 결과 보고 정상인지 확인)
-- ============================================================
SELECT 'user',                COUNT(*) AS cnt FROM user
UNION ALL SELECT 'store',     COUNT(*) FROM store
UNION ALL SELECT 'menu_category', COUNT(*) FROM menu_category
UNION ALL SELECT 'menu',      COUNT(*) FROM menu
UNION ALL SELECT 'category',  COUNT(*) FROM category
UNION ALL SELECT 'product',   COUNT(*) FROM product
UNION ALL SELECT 'pos_store_inventory', COUNT(*) FROM pos_store_inventory
UNION ALL SELECT 'pos_pay',   COUNT(*) FROM pos_pay
UNION ALL SELECT 'pos_orders_item', COUNT(*) FROM pos_orders_item
UNION ALL SELECT 'orders',    COUNT(*) FROM orders
UNION ALL SELECT 'orders_item', COUNT(*) FROM orders_item
UNION ALL SELECT 'head_notification', COUNT(*) FROM head_notification
UNION ALL SELECT 'store_notification', COUNT(*) FROM store_notification;
