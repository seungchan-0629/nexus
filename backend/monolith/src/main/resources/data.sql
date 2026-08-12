-- ============================================================
-- Nexus 더벤티 더미데이터
-- 실행 순서: FK 의존성 기준 정렬
-- ============================================================

-- ============================================================
-- 0. 기존 데이터 삭제 (FK 역순)
-- ============================================================
SET FOREIGN_KEY_CHECKS = 0;

TRUNCATE TABLE pos_orders_item;
TRUNCATE TABLE pos_pay;
TRUNCATE TABLE pos_store_inventory;
TRUNCATE TABLE store_notification;
TRUNCATE TABLE head_notification;
TRUNCATE TABLE head_income;
TRUNCATE TABLE news_summary;
TRUNCATE TABLE waste_log;
TRUNCATE TABLE report;
TRUNCATE TABLE delivery;
TRUNCATE TABLE orders_item;
TRUNCATE TABLE orders;
TRUNCATE TABLE menu_item;
TRUNCATE TABLE menu_category;
TRUNCATE TABLE menu;
TRUNCATE TABLE store_inventory;
TRUNCATE TABLE head_inventory;
TRUNCATE TABLE danger;
TRUNCATE TABLE product;
TRUNCATE TABLE category;
TRUNCATE TABLE store;
TRUNCATE TABLE user;

SET FOREIGN_KEY_CHECKS = 1;

-- ============================================================
-- 1. User (본사 관리자 + 가맹점주)
-- ============================================================
-- 비밀번호: 모두 'password123' BCrypt 인코딩
INSERT INTO user (email, password, user_name, tel, role, is_deleted) VALUES
('admin@theventi.co.kr', '{bcrypt}$2b$10$lvGJMgshX87086lRcSRGT.NXXzXyKXIXc2PgpAgAeK10LKeWfW2MS', '김본사', '010-1234-5678', 'ADMIN', false),
('manager01@theventi.co.kr', '{bcrypt}$2b$10$lvGJMgshX87086lRcSRGT.NXXzXyKXIXc2PgpAgAeK10LKeWfW2MS', '이정우', '010-2345-6789', 'MANAGER', false),
('store01@theventi.co.kr', '{bcrypt}$2b$10$lvGJMgshX87086lRcSRGT.NXXzXyKXIXc2PgpAgAeK10LKeWfW2MS', '박민수', '010-3456-7890', 'STORE', false),
('store02@theventi.co.kr', '{bcrypt}$2b$10$lvGJMgshX87086lRcSRGT.NXXzXyKXIXc2PgpAgAeK10LKeWfW2MS', '최유진', '010-4567-8901', 'STORE', false),
('store03@theventi.co.kr', '{bcrypt}$2b$10$lvGJMgshX87086lRcSRGT.NXXzXyKXIXc2PgpAgAeK10LKeWfW2MS', '정수아', '010-5678-9012', 'STORE', false),
('store04@theventi.co.kr', '{bcrypt}$2b$10$lvGJMgshX87086lRcSRGT.NXXzXyKXIXc2PgpAgAeK10LKeWfW2MS', '한지민', '010-6789-0123', 'STORE', false),
('store05@theventi.co.kr', '{bcrypt}$2b$10$lvGJMgshX87086lRcSRGT.NXXzXyKXIXc2PgpAgAeK10LKeWfW2MS', '윤서준', '010-7890-1234', 'STORE', false),
('store06@theventi.co.kr', '{bcrypt}$2b$10$lvGJMgshX87086lRcSRGT.NXXzXyKXIXc2PgpAgAeK10LKeWfW2MS', '강도훈', '010-8901-2345', 'STORE', false),
('store07@theventi.co.kr', '{bcrypt}$2b$10$lvGJMgshX87086lRcSRGT.NXXzXyKXIXc2PgpAgAeK10LKeWfW2MS', '조예은', '010-9012-3456', 'STORE', false),
('store08@theventi.co.kr', '{bcrypt}$2b$10$lvGJMgshX87086lRcSRGT.NXXzXyKXIXc2PgpAgAeK10LKeWfW2MS', '임지우', '010-0123-4567', 'STORE', false),
('store09@theventi.co.kr', '{bcrypt}$2b$10$lvGJMgshX87086lRcSRGT.NXXzXyKXIXc2PgpAgAeK10LKeWfW2MS', '서현우', '010-1122-3344', 'STORE', false),
('store10@theventi.co.kr', '{bcrypt}$2b$10$lvGJMgshX87086lRcSRGT.NXXzXyKXIXc2PgpAgAeK10LKeWfW2MS', '오지현', '010-5566-7788', 'STORE', false),
('store11@theventi.co.kr', '{bcrypt}$2b$10$lvGJMgshX87086lRcSRGT.NXXzXyKXIXc2PgpAgAeK10LKeWfW2MS', '권태윤', '010-9988-7766', 'STORE', false),
('store12@theventi.co.kr', '{bcrypt}$2b$10$lvGJMgshX87086lRcSRGT.NXXzXyKXIXc2PgpAgAeK10LKeWfW2MS', '송민경', '010-4433-2211', 'STORE', false),
('store13@theventi.co.kr', '{bcrypt}$2b$10$lvGJMgshX87086lRcSRGT.NXXzXyKXIXc2PgpAgAeK10LKeWfW2MS', '백승호', '010-6677-8899', 'STORE', false),
('store14@theventi.co.kr', '{bcrypt}$2b$10$lvGJMgshX87086lRcSRGT.NXXzXyKXIXc2PgpAgAeK10LKeWfW2MS', '신아름', '010-2233-4455', 'STORE', false),
('store15@theventi.co.kr', '{bcrypt}$2b$10$lvGJMgshX87086lRcSRGT.NXXzXyKXIXc2PgpAgAeK10LKeWfW2MS', '유재혁', '010-8877-6655', 'STORE', false);


-- ============================================================
-- 2. Store (가맹점 5개)
-- ============================================================
INSERT INTO store (user_idx, store_name, address, address_detail, file_path, business, postcode, created_at, closed_at, is_deleted) VALUES
(3, '더벤티 강남역점', '서울특별시 강남구 강남대로 396', '1층 101호', '/uploads/store/gangnam.jpg', '123-45-67890', 06232, '2023-03-15 09:00:00', NULL, false),
(4, '더벤티 홍대입구점', '서울특별시 마포구 양화로 160', '1층', '/uploads/store/hongdae.jpg', '234-56-78901', 04051, '2023-06-01 09:00:00', '2023-09-10 09:00:00', true),
(5, '더벤티 신촌점', '서울특별시 서대문구 신촌로 141', '지하1층 B102호', '/uploads/store/sinchon.jpg', '345-67-89012', 03780, '2023-09-10 09:00:00', NULL, false),
(6, '더벤티 건대입구점', '서울특별시 광진구 능동로 120', '1층 103호', '/uploads/store/konkuk.jpg', '456-78-90123', 05029, '2024-01-08 09:00:00', '2024-04-20 09:00:00', true),
(7, '더벤티 잠실점', '서울특별시 송파구 올림픽로 240', '1층', '/uploads/store/jamsil.jpg', '567-89-01234', 05554, '2024-04-20 09:00:00', NULL, false),
(8, '더벤티 부산역점', '부산광역시 동구 중앙대로 206', '1층', '/uploads/store/busan.jpg', '678-90-12345', 48733, '2024-05-01 09:00:00', NULL, false),
(9, '더벤티 대구중앙로점', '대구광역시 중구 국채보상로 585', '1층 105호', '/uploads/store/daegu.jpg', '789-01-23456', 41907, '2024-05-02 10:00:00', NULL, false),
(10, '더벤티 광주상무점', '광주광역시 서구 상무중앙로 80', '1층', '/uploads/store/gwangju.jpg', '890-12-34567', 61947, '2024-05-03 09:30:00', '2024-06-10 18:00:00', true),
(11, '더벤티 수원인계점', '경기도 수원시 팔달구 인계로 138', '1층', '/uploads/store/suwon.jpg', '234-56-78902', 16488, '2024-05-15 09:00:00', '2024-07-01 09:00:00', true),
(12, '더벤티 천안터미널점', '충청남도 천안시 동남구 만남로 43', '1층 106호', '/uploads/store/cheonan.jpg', '567-89-01235', 31120, '2024-05-25 09:00:00', '2024-05-30 09:00:00', true);





-- ============================================================
-- 3. Category (원자재 카테고리)
-- ============================================================
INSERT INTO category (category_name, is_deleted) VALUES
('원두/커피', false),
('유제품', false),
('시럽/소스', false),
('과일/퓨레', false),
('티/파우더', false),
('포장재', false),
('기타', false);

-- ============================================================
-- 4. Product (원자재 - 더벤티 실제 메뉴 기반)
-- ============================================================
INSERT INTO product (category_idx, product_name, product_unit, max_stock, min_stock, unit_price, danger_days, is_deleted) VALUES
-- 원두/커피 (category 1)
(1, '에스프레소 원두', 'kg', 50, 10, 15000, '30', false),
(1, '디카페인 원두', 'kg', 30, 5, 18000, '30', false),
-- 유제품 (category 2)
(2, '우유', 'L', 100, 20, 2500, '7', false),
(2, '저지방우유', 'L', 50, 10, 2800, '7', false),
(2, '오트밀크', 'L', 40, 8, 4500, '14', false),
(2, '휘핑크림', 'L', 30, 5, 6000, '5', false),
-- 시럽/소스 (category 3)
(3, '바닐라시럽', '병(750ml)', 20, 4, 8500, '180', false),
(3, '카라멜시럽', '병(750ml)', 20, 4, 8500, '180', false),
(3, '헤이즐넛시럽', '병(750ml)', 15, 3, 8500, '180', false),
(3, '카라멜소스', '병(500ml)', 15, 3, 7000, '90', false),
(3, '초코소스', '병(500ml)', 15, 3, 6500, '90', false),
-- 과일/퓨레 (category 4)
(4, '딸기퓨레', 'kg', 20, 4, 12000, '14', false),
(4, '망고퓨레', 'kg', 20, 4, 13000, '14', false),
(4, '복숭아퓨레', 'kg', 20, 4, 11000, '14', false),
(4, '레몬즙', 'L', 15, 3, 9000, '30', false),
(4, '자몽농축액', 'L', 15, 3, 10000, '30', false),
-- 티/파우더 (category 5)
(5, '녹차파우더', 'kg', 10, 2, 25000, '180', false),
(5, '초코파우더', 'kg', 15, 3, 12000, '180', false),
(5, '얼그레이티백', '박스(100개입)', 10, 2, 15000, '365', false),
(5, '캐모마일티백', '박스(100개입)', 8, 2, 14000, '365', false),
(5, '히비스커스티백', '박스(100개입)', 8, 2, 14000, '365', false),
-- 포장재 (category 6)
(6, '라지컵(24oz)', '박스(500개)', 10, 2, 35000, '999', false),
(6, '레귤러컵(16oz)', '박스(500개)', 10, 2, 28000, '999', false),
(6, '아이스컵 뚜껑', '박스(500개)', 10, 2, 18000, '999', false),
(6, '빨대', '박스(1000개)', 8, 2, 12000, '999', false),
(6, '캐리어(2구)', '박스(200개)', 8, 2, 15000, '999', false),
-- 기타 (category 7)
(7, '얼음', 'kg', 200, 50, 500, '1', false),
(7, '설탕', 'kg', 20, 5, 2000, '365', false),
(7, '연유', 'kg', 10, 2, 8000, '30', false);

-- ============================================================
-- 5. MenuCategoryItem (메뉴별 원자재 레시피)
-- ============================================================
INSERT INTO menu_category (menu_category_name, is_deleted) VALUES
('커피', false),
('베버리지', false),
('아이스블렌디드', false),
('티 & 에이드', false);

-- ============================================================
-- 5. Menu (더벤티 대표 메뉴)
-- ============================================================
INSERT INTO menu (menu_name, price, img_path, is_deleted, menu_category_idx) VALUES
-- 커피 (idx: 1)
('아메리카노', 1500, 'theVenti_logo.png ', false, 1),
('카페라떼', 2500, 'theVenti_logo.png ', false, 1),
('바닐라라떼', 3000, 'theVenti_logo.png ', false, 1),
('카라멜마끼아또', 3500, 'theVenti_logo.png ', false, 1),
('카푸치노', 2500, 'theVenti_logo.png ', false, 1),

-- 베버리지 (idx: 2)
('녹차라떼', 3000, 'theVenti_logo.png ', false, 2),
('초코라떼', 3000, 'theVenti_logo.png ', false, 2),

-- 아이스블렌디드 (idx: 3)
('딸기스무디', 3500, 'theVenti_logo.png ', false, 3),
('망고스무디', 3500, 'theVenti_logo.png ', false, 3),

-- 티 & 에이드 (idx: 4)
('복숭아아이스티', 2500, 'theVenti_logo.png ', false, 4),
('레몬에이드', 2500, 'theVenti_logo.png ', false, 4),
('자몽에이드', 3000, 'theVenti_logo.png ', false, 4),
('얼그레이티', 2000, 'theVenti_logo.png ', false, 4),
('캐모마일티', 2000, 'theVenti_logo.png ', false, 4),
('히비스커스티', 2000, 'theVenti_logo.png ', false, 4);


-- ============================================================
-- 6. MenuItem (메뉴별 원자재 레시피)
-- ============================================================
INSERT INTO menu_item (menu_idx, product_idx, quantity, menu_unit) VALUES
-- 아메리카노: 원두 20g, 얼음 200g, 라지컵 1개
(1, 1, 20, 'g'),
(1, 27, 200, 'g'),
(1, 22, 1, 'ea'),
-- 카페라떼: 원두 20g, 우유 300ml, 얼음 150g, 라지컵 1개
(2, 1, 20, 'g'),
(2, 3, 300, 'ml'),
(2, 27, 150, 'g'),
(2, 22, 1, 'ea'),
-- 바닐라라떼: 원두 20g, 우유 300ml, 바닐라시럽 20ml, 얼음 150g, 라지컵 1개
(3, 1, 20, 'g'),
(3, 3, 300, 'ml'),
(3, 7, 20, 'ml'),
(3, 27, 150, 'g'),
(3, 22, 1, 'ea'),
-- 카라멜마끼아또: 원두 20g, 우유 300ml, 바닐라시럽 15ml, 카라멜소스 15ml, 얼음 150g, 라지컵 1개
(4, 1, 20, 'g'),
(4, 3, 300, 'ml'),
(4, 7, 15, 'ml'),
(4, 10, 15, 'ml'),
(4, 27, 150, 'g'),
(4, 22, 1, 'ea'),
-- 카푸치노: 원두 20g, 우유 250ml, 라지컵 1개
(5, 1, 20, 'g'),
(5, 3, 250, 'ml'),
(5, 22, 1, 'ea'),
-- 딸기스무디: 딸기퓨레 80g, 우유 200ml, 얼음 200g, 라지컵 1개
(6, 12, 80, 'g'),
(6, 3, 200, 'ml'),
(6, 27, 200, 'g'),
(6, 22, 1, 'ea'),
-- 망고스무디: 망고퓨레 80g, 우유 200ml, 얼음 200g, 라지컵 1개
(7, 13, 80, 'g'),
(7, 3, 200, 'ml'),
(7, 27, 200, 'g'),
(7, 22, 1, 'ea'),
-- 복숭아아이스티: 복숭아퓨레 50g, 얼그레이티백 1개, 얼음 200g, 라지컵 1개
(8, 14, 50, 'g'),
(8, 19, 1, 'ea'),
(8, 27, 200, 'g'),
(8, 22, 1, 'ea'),
-- 레몬에이드: 레몬즙 40ml, 설탕 15g, 얼음 200g, 라지컵 1개
(9, 15, 40, 'ml'),
(9, 28, 15, 'g'),
(9, 27, 200, 'g'),
(9, 22, 1, 'ea'),
-- 자몽에이드: 자몽농축액 50ml, 설탕 15g, 얼음 200g, 라지컵 1개
(10, 16, 50, 'ml'),
(10, 28, 15, 'g'),
(10, 27, 200, 'g'),
(10, 22, 1, 'ea'),
-- 녹차라떼: 녹차파우더 15g, 우유 350ml, 얼음 150g, 라지컵 1개
(11, 17, 15, 'g'),
(11, 3, 350, 'ml'),
(11, 27, 150, 'g'),
(11, 22, 1, 'ea'),
-- 초코라떼: 초코파우더 25g, 우유 350ml, 초코소스 10ml, 얼음 150g, 라지컵 1개
(12, 18, 25, 'g'),
(12, 3, 350, 'ml'),
(12, 11, 10, 'ml'),
(12, 27, 150, 'g'),
(12, 22, 1, 'ea'),
-- 얼그레이티: 얼그레이티백 2개, 라지컵 1개
(13, 19, 2, 'ea'),
(13, 22, 1, 'ea'),
-- 캐모마일티: 캐모마일티백 2개, 라지컵 1개
(14, 20, 2, 'ea'),
(14, 22, 1, 'ea'),
-- 히비스커스티: 히비스커스티백 2개, 라지컵 1개
(15, 21, 2, 'ea'),
(15, 22, 1, 'ea');

-- ============================================================
-- 7. Danger (위험 발주 설정)
-- ============================================================
INSERT INTO danger (ratio, period) VALUES
(30, 7);

-- ============================================================
-- 8. HeadInventory (본사 재고)
-- ============================================================
INSERT INTO head_inventory (product_idx, count, status, manufactured_date) VALUES
(1, 500, 'NORMAL', '2026-04-20 08:00:00'),
(2, 200, 'NORMAL', '2026-04-20 08:00:00'),
(3, 800, 'NORMAL', '2026-04-27 06:00:00'),
(4, 300, 'NORMAL', '2026-04-27 06:00:00'),
(5, 200, 'NORMAL', '2026-04-25 06:00:00'),
(6, 150, 'NORMAL', '2026-04-26 06:00:00'),
(7, 100, 'NORMAL', '2026-03-15 08:00:00'),
(8, 100, 'NORMAL', '2026-03-15 08:00:00'),
(9, 80, 'NORMAL', '2026-03-15 08:00:00'),
(10, 60, 'NORMAL', '2026-03-20 08:00:00'),
(11, 60, 'NORMAL', '2026-03-20 08:00:00'),
(12, 100, 'NORMAL', '2026-04-25 08:00:00'),
(13, 100, 'NORMAL', '2026-04-25 08:00:00'),
(14, 100, 'NORMAL', '2026-04-25 08:00:00'),
(15, 80, 'NORMAL', '2026-04-15 08:00:00'),
(16, 80, 'NORMAL', '2026-04-15 08:00:00'),
(17, 40, 'NORMAL', '2026-02-10 08:00:00'),
(18, 60, 'NORMAL', '2026-02-10 08:00:00'),
(19, 50, 'NORMAL', '2026-01-05 08:00:00'),
(20, 40, 'NORMAL', '2026-01-05 08:00:00'),
(21, 40, 'NORMAL', '2026-01-05 08:00:00'),
(22, 50, 'NORMAL', '2026-04-01 08:00:00'),
(23, 50, 'NORMAL', '2026-04-01 08:00:00'),
(24, 50, 'NORMAL', '2026-04-01 08:00:00'),
(25, 40, 'NORMAL', '2026-04-01 08:00:00'),
(26, 40, 'NORMAL', '2026-04-01 08:00:00'),
(27, 1000, 'NORMAL', '2026-04-29 05:00:00'),
(28, 100, 'NORMAL', '2026-01-10 08:00:00'),
(29, 50, 'NORMAL', '2026-04-10 08:00:00');

-- ============================================================
-- 9. StoreInventory (가맹점 재고 - 5개 매장)
-- ============================================================
    -- 강남역점 (store 1) - 데모·통합 테스트용 초과 재고 (pos_store_inventory와 동일 스냅샷)
INSERT INTO store_inventory (store_idx, product_idx, count, status, manufactured_date, avg_stock) VALUES
(1, 1, 20000, 'NORMAL', '2026-04-25 08:00:00', 800),
(1, 1, 20000, 'NORMAL', '2026-04-27 08:00:00', 800),
(1, 2, 8000, 'NORMAL', '2026-04-25 08:00:00', 400),
(1, 3, 20000, 'NORMAL', '2026-04-28 06:00:00', 800),
(1, 3, 20000, 'NORMAL', '2026-04-29 06:00:00', 800),
(1, 4, 8000, 'NORMAL', '2026-04-28 06:00:00', 400),
(1, 5, 8000, 'NORMAL', '2026-04-26 06:00:00', 400),
(1, 6, 8000, 'NORMAL', '2026-04-27 06:00:00', 400),
(1, 7, 15000, 'NORMAL', '2026-04-10 08:00:00', 600),
(1, 7, 15000, 'NORMAL', '2026-04-12 08:00:00', 600),
(1, 8, 8000, 'NORMAL', '2026-04-10 08:00:00', 400),
(1, 9, 8000, 'NORMAL', '2026-04-10 08:00:00', 400),
(1, 10, 8000, 'NORMAL', '2026-04-15 08:00:00', 400),
(1, 11, 8000, 'NORMAL', '2026-04-15 08:00:00', 400),
(1, 12, 8000, 'NORMAL', '2026-04-26 08:00:00', 400),
(1, 13, 8000, 'NORMAL', '2026-04-26 08:00:00', 400),
(1, 14, 8000, 'NORMAL', '2026-04-26 08:00:00', 400),
(1, 15, 8000, 'NORMAL', '2026-04-20 08:00:00', 400),
(1, 16, 8000, 'NORMAL', '2026-04-20 08:00:00', 400),
(1, 17, 8000, 'NORMAL', '2026-03-01 08:00:00', 400),
(1, 18, 8000, 'NORMAL', '2026-03-01 08:00:00', 400),
(1, 19, 8000, 'NORMAL', '2026-02-01 08:00:00', 400),
(1, 20, 8000, 'NORMAL', '2026-02-01 08:00:00', 400),
(1, 21, 8000, 'NORMAL', '2026-02-01 08:00:00', 400),
(1, 22, 8000, 'NORMAL', '2026-04-15 08:00:00', 400),
(1, 23, 8000, 'NORMAL', '2026-04-15 08:00:00', 400),
(1, 24, 8000, 'NORMAL', '2026-04-15 08:00:00', 400),
(1, 25, 8000, 'NORMAL', '2026-04-15 08:00:00', 400),
(1, 26, 8000, 'NORMAL', '2026-04-15 08:00:00', 400),
(1, 27, 50000, 'NORMAL', '2026-04-28 05:00:00', 2000),
(1, 27, 50000, 'NORMAL', '2026-04-29 05:00:00', 2000),
(1, 28, 8000, 'NORMAL', '2026-01-15 08:00:00', 400),
(1, 29, 8000, 'NORMAL', '2026-04-15 08:00:00', 400);

-- 홍대입구점 (store 2) - 일부 재고 부족 (원두만 마감 테스트용으로 보강)
INSERT INTO store_inventory (store_idx, product_idx, count, status, manufactured_date, avg_stock) VALUES
(2, 1, 400, 'LOW', '2026-04-23 08:00:00', 30),
(2, 1, 400, 'LOW', '2026-04-26 08:00:00', 30),
(2, 2, 10, 'NORMAL', '2026-04-23 08:00:00', 8),
(2, 3, 18, 'LOW', '2026-04-27 06:00:00', 60),
(2, 4, 12, 'NORMAL', '2026-04-27 06:00:00', 10),
(2, 5, 6, 'LOW', '2026-04-25 06:00:00', 18),
(2, 6, 4, 'LOW', '2026-04-26 06:00:00', 10),
(2, 7, 8, 'NORMAL', '2026-04-08 08:00:00', 7),
(2, 8, 3, 'LOW', '2026-04-08 08:00:00', 8),
(2, 9, 5, 'NORMAL', '2026-04-08 08:00:00', 4),
(2, 10, 5, 'NORMAL', '2026-04-12 08:00:00', 4),
(2, 11, 4, 'NORMAL', '2026-04-12 08:00:00', 4),
(2, 12, 1, 'CRITICAL', '2026-04-24 08:00:00', 8),
(2, 12, 2, 'CRITICAL', '2026-04-26 08:00:00', 8),
(2, 13, 8, 'NORMAL', '2026-04-24 08:00:00', 7),
(2, 14, 7, 'NORMAL', '2026-04-24 08:00:00', 6),
(2, 15, 5, 'NORMAL', '2026-04-18 08:00:00', 5),
(2, 16, 6, 'NORMAL', '2026-04-18 08:00:00', 5),
(2, 17, 3, 'NORMAL', '2026-02-20 08:00:00', 3),
(2, 18, 5, 'NORMAL', '2026-02-20 08:00:00', 5),
(2, 19, 4, 'NORMAL', '2026-01-20 08:00:00', 3),
(2, 20, 3, 'NORMAL', '2026-01-20 08:00:00', 3),
(2, 21, 3, 'NORMAL', '2026-01-20 08:00:00', 3),
(2, 22, 1, 'CRITICAL', '2026-04-10 08:00:00', 5),
(2, 23, 4, 'NORMAL', '2026-04-10 08:00:00', 4),
(2, 24, 2, 'LOW', '2026-04-10 08:00:00', 5),
(2, 25, 3, 'NORMAL', '2026-04-10 08:00:00', 3),
(2, 26, 3, 'NORMAL', '2026-04-10 08:00:00', 3),
(2, 27, 80, 'LOW', '2026-04-29 05:00:00', 120),
(2, 28, 7, 'NORMAL', '2026-01-10 08:00:00', 6),
(2, 29, 3, 'NORMAL', '2026-04-10 08:00:00', 3);

-- 신촌점 (store 3) - 보통
INSERT INTO store_inventory (store_idx, product_idx, count, status, manufactured_date, avg_stock) VALUES
(3, 1, 25, 'NORMAL', '2026-04-24 08:00:00', 22),
(3, 3, 30, 'NORMAL', '2026-04-28 06:00:00', 45),
(3, 3, 20, 'NORMAL', '2026-04-30 06:00:00', 45),
(3, 7, 6, 'NORMAL', '2026-04-05 08:00:00', 5),
(3, 8, 7, 'NORMAL', '2026-04-05 08:00:00', 6),
(3, 12, 6, 'NORMAL', '2026-04-25 08:00:00', 5),
(3, 13, 5, 'NORMAL', '2026-04-25 08:00:00', 5),
(3, 17, 4, 'NORMAL', '2026-02-15 08:00:00', 3),
(3, 18, 6, 'NORMAL', '2026-02-15 08:00:00', 5),
(3, 22, 4, 'NORMAL', '2026-04-12 08:00:00', 4),
(3, 27, 120, 'NORMAL', '2026-04-29 05:00:00', 100);

-- 건대입구점 (store 4) - 재고 위험
INSERT INTO store_inventory (store_idx, product_idx, count, status, manufactured_date, avg_stock) VALUES
(4, 1, 5, 'CRITICAL', '2026-04-22 08:00:00', 25),
(4, 3, 10, 'CRITICAL', '2026-04-26 06:00:00', 50),
(4, 7, 2, 'CRITICAL', '2026-04-01 08:00:00', 6),
(4, 8, 2, 'CRITICAL', '2026-04-01 08:00:00', 6),
(4, 12, 2, 'CRITICAL', '2026-04-23 08:00:00', 7),
(4, 13, 3, 'LOW', '2026-04-23 08:00:00', 7),
(4, 22, 1, 'CRITICAL', '2026-04-08 08:00:00', 5),
(4, 27, 40, 'CRITICAL', '2026-04-29 05:00:00', 100);

-- 잠실점 (store 5) - 보통
INSERT INTO store_inventory (store_idx, product_idx, count, status, manufactured_date, avg_stock) VALUES
(5, 1, 28, 'NORMAL', '2026-04-26 08:00:00', 25),
(5, 3, 60, 'NORMAL', '2026-04-28 06:00:00', 55),
(5, 5, 20, 'NORMAL', '2026-04-26 06:00:00', 15),
(5, 7, 9, 'NORMAL', '2026-04-12 08:00:00', 7),
(5, 12, 8, 'NORMAL', '2026-04-26 08:00:00', 7),
(5, 17, 3, 'NORMAL', '2026-03-01 08:00:00', 3),
(5, 22, 5, 'NORMAL', '2026-04-14 08:00:00', 4),
(5, 27, 130, 'NORMAL', '2026-04-29 05:00:00', 110);

-- ============================================================
-- 10. PosStoreInventory (POS 재고 — store_inventory와 동일 행, avg_stock 제외)
-- ============================================================
INSERT INTO pos_store_inventory (store_idx, product_idx, count, status, manufactured_date) VALUES
(1, 1, 20000, 'NORMAL', '2026-04-25 08:00:00'),
(1, 1, 20000, 'NORMAL', '2026-04-27 08:00:00'),
(1, 2, 8000, 'NORMAL', '2026-04-25 08:00:00'),
(1, 3, 20000, 'NORMAL', '2026-04-28 06:00:00'),
(1, 3, 20000, 'NORMAL', '2026-04-29 06:00:00'),
(1, 4, 8000, 'NORMAL', '2026-04-28 06:00:00'),
(1, 5, 8000, 'NORMAL', '2026-04-26 06:00:00'),
(1, 6, 8000, 'NORMAL', '2026-04-27 06:00:00'),
(1, 7, 15000, 'NORMAL', '2026-04-10 08:00:00'),
(1, 7, 15000, 'NORMAL', '2026-04-12 08:00:00'),
(1, 8, 8000, 'NORMAL', '2026-04-10 08:00:00'),
(1, 9, 8000, 'NORMAL', '2026-04-10 08:00:00'),
(1, 10, 8000, 'NORMAL', '2026-04-15 08:00:00'),
(1, 11, 8000, 'NORMAL', '2026-04-15 08:00:00'),
(1, 12, 8000, 'NORMAL', '2026-04-26 08:00:00'),
(1, 13, 8000, 'NORMAL', '2026-04-26 08:00:00'),
(1, 14, 8000, 'NORMAL', '2026-04-26 08:00:00'),
(1, 15, 8000, 'NORMAL', '2026-04-20 08:00:00'),
(1, 16, 8000, 'NORMAL', '2026-04-20 08:00:00'),
(1, 17, 8000, 'NORMAL', '2026-03-01 08:00:00'),
(1, 18, 8000, 'NORMAL', '2026-03-01 08:00:00'),
(1, 19, 8000, 'NORMAL', '2026-02-01 08:00:00'),
(1, 20, 8000, 'NORMAL', '2026-02-01 08:00:00'),
(1, 21, 8000, 'NORMAL', '2026-02-01 08:00:00'),
(1, 22, 8000, 'NORMAL', '2026-04-15 08:00:00'),
(1, 23, 8000, 'NORMAL', '2026-04-15 08:00:00'),
(1, 24, 8000, 'NORMAL', '2026-04-15 08:00:00'),
(1, 25, 8000, 'NORMAL', '2026-04-15 08:00:00'),
(1, 26, 8000, 'NORMAL', '2026-04-15 08:00:00'),
(1, 27, 50000, 'NORMAL', '2026-04-28 05:00:00'),
(1, 27, 50000, 'NORMAL', '2026-04-29 05:00:00'),
(1, 28, 8000, 'NORMAL', '2026-01-15 08:00:00'),
(1, 29, 8000, 'NORMAL', '2026-04-15 08:00:00'),
(2, 1, 400, 'LOW', '2026-04-23 08:00:00'),
(2, 1, 400, 'LOW', '2026-04-26 08:00:00'),
(2, 2, 10, 'NORMAL', '2026-04-23 08:00:00'),
(2, 3, 18, 'LOW', '2026-04-27 06:00:00'),
(2, 4, 12, 'NORMAL', '2026-04-27 06:00:00'),
(2, 5, 6, 'LOW', '2026-04-25 06:00:00'),
(2, 6, 4, 'LOW', '2026-04-26 06:00:00'),
(2, 7, 8, 'NORMAL', '2026-04-08 08:00:00'),
(2, 8, 3, 'LOW', '2026-04-08 08:00:00'),
(2, 9, 5, 'NORMAL', '2026-04-08 08:00:00'),
(2, 10, 5, 'NORMAL', '2026-04-12 08:00:00'),
(2, 11, 4, 'NORMAL', '2026-04-12 08:00:00'),
(2, 12, 1, 'CRITICAL', '2026-04-24 08:00:00'),
(2, 12, 2, 'CRITICAL', '2026-04-26 08:00:00'),
(2, 13, 8, 'NORMAL', '2026-04-24 08:00:00'),
(2, 14, 7, 'NORMAL', '2026-04-24 08:00:00'),
(2, 15, 5, 'NORMAL', '2026-04-18 08:00:00'),
(2, 16, 6, 'NORMAL', '2026-04-18 08:00:00'),
(2, 17, 3, 'NORMAL', '2026-02-20 08:00:00'),
(2, 18, 5, 'NORMAL', '2026-02-20 08:00:00'),
(2, 19, 4, 'NORMAL', '2026-01-20 08:00:00'),
(2, 20, 3, 'NORMAL', '2026-01-20 08:00:00'),
(2, 21, 3, 'NORMAL', '2026-01-20 08:00:00'),
(2, 22, 1, 'CRITICAL', '2026-04-10 08:00:00'),
(2, 23, 4, 'NORMAL', '2026-04-10 08:00:00'),
(2, 24, 2, 'LOW', '2026-04-10 08:00:00'),
(2, 25, 3, 'NORMAL', '2026-04-10 08:00:00'),
(2, 26, 3, 'NORMAL', '2026-04-10 08:00:00'),
(2, 27, 80, 'LOW', '2026-04-29 05:00:00'),
(2, 28, 7, 'NORMAL', '2026-01-10 08:00:00'),
(2, 29, 3, 'NORMAL', '2026-04-10 08:00:00'),
(3, 1, 25, 'NORMAL', '2026-04-24 08:00:00'),
(3, 3, 30, 'NORMAL', '2026-04-28 06:00:00'),
(3, 3, 20, 'NORMAL', '2026-04-30 06:00:00'),
(3, 7, 6, 'NORMAL', '2026-04-05 08:00:00'),
(3, 8, 7, 'NORMAL', '2026-04-05 08:00:00'),
(3, 12, 6, 'NORMAL', '2026-04-25 08:00:00'),
(3, 13, 5, 'NORMAL', '2026-04-25 08:00:00'),
(3, 17, 4, 'NORMAL', '2026-02-15 08:00:00'),
(3, 18, 6, 'NORMAL', '2026-02-15 08:00:00'),
(3, 22, 4, 'NORMAL', '2026-04-12 08:00:00'),
(3, 27, 120, 'NORMAL', '2026-04-29 05:00:00'),
(4, 1, 5, 'CRITICAL', '2026-04-22 08:00:00'),
(4, 3, 10, 'CRITICAL', '2026-04-26 06:00:00'),
(4, 7, 2, 'CRITICAL', '2026-04-01 08:00:00'),
(4, 8, 2, 'CRITICAL', '2026-04-01 08:00:00'),
(4, 12, 2, 'CRITICAL', '2026-04-23 08:00:00'),
(4, 13, 3, 'LOW', '2026-04-23 08:00:00'),
(4, 22, 1, 'CRITICAL', '2026-04-08 08:00:00'),
(4, 27, 40, 'CRITICAL', '2026-04-29 05:00:00'),
(5, 1, 28, 'NORMAL', '2026-04-26 08:00:00'),
(5, 3, 60, 'NORMAL', '2026-04-28 06:00:00'),
(5, 5, 20, 'NORMAL', '2026-04-26 06:00:00'),
(5, 7, 9, 'NORMAL', '2026-04-12 08:00:00'),
(5, 12, 8, 'NORMAL', '2026-04-26 08:00:00'),
(5, 17, 3, 'NORMAL', '2026-03-01 08:00:00'),
(5, 22, 5, 'NORMAL', '2026-04-14 08:00:00'),
(5, 27, 130, 'NORMAL', '2026-04-29 05:00:00');

-- ============================================================
-- 11. Orders (발주서)
-- ============================================================
-- 강남역점 발주 (승인 완료)
INSERT INTO orders (store_idx, price, order_type, order_status, is_danger, created_at) VALUES
(1, 310000, 'MANUAL', 'APPROVE', false, '2026-04-10 09:30:00'),
(1, 180000, 'AUTO', 'APPROVE', false, '2026-04-17 07:00:00'),
(1, 106000, 'MANUAL', 'APPROVE', false, '2026-04-24 10:15:00');

-- 홍대입구점 발주 (혼합 상태)
INSERT INTO orders (store_idx, price, order_type, order_status, is_danger, created_at) VALUES
(2, 481000, 'AUTO', 'APPROVE', true, '2026-04-15 07:00:00'),
(2, 201500, 'MANUAL', 'CONFIRMED', false, '2026-04-28 11:20:00'),
(2, 717500, 'AUTO', 'WAITING', true, '2026-04-29 07:00:00');

-- 신촌점 발주
INSERT INTO orders (store_idx, price, order_type, order_status, is_danger, created_at) VALUES
(3, 161000, 'MANUAL', 'APPROVE', false, '2026-04-20 14:00:00');

-- 건대입구점 발주 (위험 발주)
INSERT INTO orders (store_idx, price, order_type, order_status, is_danger, created_at) VALUES
(4, 756000, 'AUTO', 'APPROVE', true, '2026-04-22 07:00:00'),
(4, 405000, 'AUTO', 'WAITING', true, '2026-04-29 07:00:00');

-- 잠실점 발주
INSERT INTO orders (store_idx, price, order_type, order_status, is_danger, created_at) VALUES
(5, 242500, 'MANUAL', 'APPROVE', false, '2026-04-18 16:30:00'),
(5, 75500, 'MANUAL', 'REJECT', false, '2026-04-25 09:45:00');

-- 발주 이력 추가 데이터 (orders 12~34)
-- 강남역점 3월
INSERT INTO orders (store_idx, price, order_type, order_status, is_danger, created_at) VALUES
(1, 220000, 'AUTO', 'APPROVE', false, '2026-03-05 07:00:00'),
(1, 65000, 'MANUAL', 'CANCELLED', false, '2026-03-12 10:00:00');

-- 홍대입구점 3월
INSERT INTO orders (store_idx, price, order_type, order_status, is_danger, created_at) VALUES
(2, 255000, 'AUTO', 'APPROVE', false, '2026-03-08 07:00:00'),
(2, 75000, 'MANUAL', 'REJECT', false, '2026-03-15 11:00:00');

-- 신촌점 3월
INSERT INTO orders (store_idx, price, order_type, order_status, is_danger, created_at) VALUES
(3, 347500, 'AUTO', 'APPROVE', false, '2026-03-10 07:00:00'),
(3, 116000, 'MANUAL', 'APPROVE', false, '2026-03-20 14:30:00');

-- 건대입구점 3월
INSERT INTO orders (store_idx, price, order_type, order_status, is_danger, created_at) VALUES
(4, 357500, 'AUTO', 'APPROVE', false, '2026-03-12 07:00:00'),
(4, 213000, 'MANUAL', 'CANCELLED', false, '2026-03-25 09:00:00');

-- 잠실점 3월
INSERT INTO orders (store_idx, price, order_type, order_status, is_danger, created_at) VALUES
(5, 285000, 'AUTO', 'APPROVE', false, '2026-03-15 07:00:00'),
(5, 30000, 'MANUAL', 'REJECT', false, '2026-03-22 15:00:00');

-- 강남역점 3~4월
INSERT INTO orders (store_idx, price, order_type, order_status, is_danger, created_at) VALUES
(1, 180000, 'AUTO', 'APPROVE', false, '2026-03-25 07:00:00'),
(1, 112000, 'MANUAL', 'APPROVE', false, '2026-04-01 10:00:00');

-- 홍대입구점 4월
INSERT INTO orders (store_idx, price, order_type, order_status, is_danger, created_at) VALUES
(2, 276500, 'AUTO', 'APPROVE', false, '2026-04-03 07:00:00'),
(2, 56000, 'MANUAL', 'CANCELLED', false, '2026-04-05 11:30:00');

-- 신촌점 4월
INSERT INTO orders (store_idx, price, order_type, order_status, is_danger, created_at) VALUES
(3, 222000, 'AUTO', 'APPROVE', false, '2026-04-02 07:00:00'),
(3, 34000, 'MANUAL', 'REJECT', false, '2026-04-08 09:30:00');

-- 건대입구점 4월
INSERT INTO orders (store_idx, price, order_type, order_status, is_danger, created_at) VALUES
(4, 285500, 'AUTO', 'APPROVE', false, '2026-04-05 07:00:00'),
(4, 224000, 'MANUAL', 'APPROVE', false, '2026-04-10 13:00:00');

-- 잠실점 4월
INSERT INTO orders (store_idx, price, order_type, order_status, is_danger, created_at) VALUES
(5, 211500, 'AUTO', 'APPROVE', false, '2026-04-07 07:00:00'),
(5, 98000, 'MANUAL', 'CANCELLED', false, '2026-04-12 16:00:00');

-- 강남역점 4월 추가
INSERT INTO orders (store_idx, price, order_type, order_status, is_danger, created_at) VALUES
(1, 550000, 'AUTO', 'REJECT', true, '2026-04-14 07:00:00');

-- 홍대입구점 4월 추가
INSERT INTO orders (store_idx, price, order_type, order_status, is_danger, created_at) VALUES
(2, 216000, 'AUTO', 'APPROVE', false, '2026-04-12 07:00:00');

-- 신촌점 4월 추가
INSERT INTO orders (store_idx, price, order_type, order_status, is_danger, created_at) VALUES
(3, 110500, 'MANUAL', 'APPROVE', false, '2026-04-15 11:00:00');

-- 강남역점 제안 발주서 (WAITING)
INSERT INTO orders (store_idx, price, order_type, order_status, is_danger, created_at) VALUES
(1, 280000, 'AUTO', 'WAITING', false, '2026-05-01 07:00:00'),
(1, 126500, 'AUTO', 'WAITING', false, '2026-05-02 07:00:00');

-- ============================================================
-- 12. OrdersItem (발주 항목)
-- ============================================================
-- 강남역점 1차 발주 (orders 1): 원두, 우유, 시럽
INSERT INTO orders_item (orders_idx, product_idx, count) VALUES
(1, 1, 10),
(1, 3, 30),
(1, 7, 5),
(1, 8, 5);

-- 강남역점 2차 발주 (orders 2): 과일퓨레
INSERT INTO orders_item (orders_idx, product_idx, count) VALUES
(2, 12, 5),
(2, 13, 5),
(2, 14, 5);

-- 강남역점 3차 발주 (orders 3): 포장재
INSERT INTO orders_item (orders_idx, product_idx, count) VALUES
(3, 22, 2),
(3, 24, 2);

-- 홍대입구점 1차 발주 (orders 4): 긴급 대량
INSERT INTO orders_item (orders_idx, product_idx, count) VALUES
(4, 1, 15),
(4, 3, 40),
(4, 6, 10),
(4, 12, 8);

-- 홍대입구점 2차 발주 (orders 5): 대기중
INSERT INTO orders_item (orders_idx, product_idx, count) VALUES
(5, 8, 5),
(5, 22, 3),
(5, 24, 3);

-- 홍대입구점 3차 발주 (orders 6): 위험발주 대기
INSERT INTO orders_item (orders_idx, product_idx, count) VALUES
(6, 1, 20),
(6, 3, 50),
(6, 5, 15),
(6, 22, 5),
(6, 27, 100);

-- 신촌점 발주 (orders 7)
INSERT INTO orders_item (orders_idx, product_idx, count) VALUES
(7, 1, 5),
(7, 3, 20),
(7, 18, 3);

-- 건대입구점 1차 발주 (orders 8): 위험발주 승인
INSERT INTO orders_item (orders_idx, product_idx, count) VALUES
(8, 1, 20),
(8, 3, 40),
(8, 7, 5),
(8, 8, 5),
(8, 12, 8),
(8, 22, 5);

-- 건대입구점 2차 발주 (orders 9): 위험발주 대기
INSERT INTO orders_item (orders_idx, product_idx, count) VALUES
(9, 1, 15),
(9, 3, 30),
(9, 13, 5),
(9, 27, 80);

-- 잠실점 1차 발주 (orders 10): 승인
INSERT INTO orders_item (orders_idx, product_idx, count) VALUES
(10, 1, 8),
(10, 3, 25),
(10, 12, 5);

-- 잠실점 2차 발주 (orders 11): 반려
INSERT INTO orders_item (orders_idx, product_idx, count) VALUES
(11, 7, 3),
(11, 17, 2);

-- 강남역점 3월 1차 (orders 12): 원두+우유+얼음 = 220000
INSERT INTO orders_item (orders_idx, product_idx, count) VALUES
(12, 1, 8),
(12, 3, 20),
(12, 27, 100);

-- 강남역점 3월 2차 (orders 13): 시럽류 = 65000 (취소)
INSERT INTO orders_item (orders_idx, product_idx, count) VALUES
(13, 7, 3),
(13, 8, 3),
(13, 10, 2);

-- 홍대입구점 3월 1차 (orders 14): 원두+우유+휘핑크림 = 255000
INSERT INTO orders_item (orders_idx, product_idx, count) VALUES
(14, 1, 10),
(14, 3, 30),
(14, 6, 5);

-- 홍대입구점 3월 2차 (orders 15): 퓨레 = 75000 (반려)
INSERT INTO orders_item (orders_idx, product_idx, count) VALUES
(15, 12, 3),
(15, 13, 3);

-- 신촌점 3월 1차 (orders 16): 원두+우유+라지컵 = 347500
INSERT INTO orders_item (orders_idx, product_idx, count) VALUES
(16, 1, 12),
(16, 3, 25),
(16, 22, 3);

-- 신촌점 3월 2차 (orders 17): 파우더+티백 = 116000
INSERT INTO orders_item (orders_idx, product_idx, count) VALUES
(17, 17, 2),
(17, 18, 3),
(17, 19, 2);

-- 건대입구점 3월 1차 (orders 18): 원두+우유+오트밀크 = 357500
INSERT INTO orders_item (orders_idx, product_idx, count) VALUES
(18, 1, 15),
(18, 3, 35),
(18, 5, 10);

-- 건대입구점 3월 2차 (orders 19): 포장재 = 213000 (취소)
INSERT INTO orders_item (orders_idx, product_idx, count) VALUES
(19, 22, 3),
(19, 23, 3),
(19, 25, 2);

-- 잠실점 3월 1차 (orders 20): 원두+우유+딸기퓨레 = 285000
INSERT INTO orders_item (orders_idx, product_idx, count) VALUES
(20, 1, 10),
(20, 3, 30),
(20, 12, 5);

-- 잠실점 3월 2차 (orders 21): 시럽 = 30000 (반려)
INSERT INTO orders_item (orders_idx, product_idx, count) VALUES
(21, 9, 2),
(21, 11, 2);

-- 강남역점 3월 3차 (orders 22): 디카페인+저지방+휘핑 = 180000
INSERT INTO orders_item (orders_idx, product_idx, count) VALUES
(22, 2, 5),
(22, 4, 15),
(22, 6, 8);

-- 강남역점 4월 추가 (orders 23): 퓨레+즙 = 112000
INSERT INTO orders_item (orders_idx, product_idx, count) VALUES
(23, 14, 5),
(23, 15, 3),
(23, 16, 3);

-- 홍대입구점 4월 1차 (orders 24): 원두+우유+시럽 = 276500
INSERT INTO orders_item (orders_idx, product_idx, count) VALUES
(24, 1, 12),
(24, 3, 25),
(24, 7, 4);

-- 홍대입구점 4월 2차 (orders 25): 티백 = 56000 (취소)
INSERT INTO orders_item (orders_idx, product_idx, count) VALUES
(25, 20, 2),
(25, 21, 2);

-- 신촌점 4월 1차 (orders 26): 원두+우유+망고퓨레 = 222000
INSERT INTO orders_item (orders_idx, product_idx, count) VALUES
(26, 1, 8),
(26, 3, 20),
(26, 13, 4);

-- 신촌점 4월 2차 (orders 27): 연유+설탕 = 34000 (반려)
INSERT INTO orders_item (orders_idx, product_idx, count) VALUES
(27, 29, 3),
(27, 28, 5);

-- 건대입구점 4월 1차 (orders 28): 원두+우유+시럽+퓨레 = 285500
INSERT INTO orders_item (orders_idx, product_idx, count) VALUES
(28, 1, 10),
(28, 3, 20),
(28, 8, 3),
(28, 12, 5);

-- 건대입구점 4월 2차 (orders 29): 포장재 = 224000
INSERT INTO orders_item (orders_idx, product_idx, count) VALUES
(29, 22, 4),
(29, 24, 3),
(29, 26, 2);

-- 잠실점 4월 1차 (orders 30): 원두+오트밀크+시럽 = 211500
INSERT INTO orders_item (orders_idx, product_idx, count) VALUES
(30, 1, 10),
(30, 5, 8),
(30, 7, 3);

-- 잠실점 4월 2차 (orders 31): 파우더 = 98000 (취소)
INSERT INTO orders_item (orders_idx, product_idx, count) VALUES
(31, 17, 2),
(31, 18, 4);

-- 강남역점 4월 위험발주 (orders 32): 대량 원두+얼음 = 550000 (반려)
INSERT INTO orders_item (orders_idx, product_idx, count) VALUES
(32, 1, 30),
(32, 27, 200);

-- 홍대입구점 4월 추가 (orders 33): 우유+저지방+휘핑 = 216000
INSERT INTO orders_item (orders_idx, product_idx, count) VALUES
(33, 3, 40),
(33, 4, 20),
(33, 6, 10);

-- 신촌점 4월 추가 (orders 34): 시럽류 = 110500
INSERT INTO orders_item (orders_idx, product_idx, count) VALUES
(34, 7, 5),
(34, 8, 5),
(34, 9, 3);

-- 강남역점 제안 발주서 1 (orders 35): 원두+우유+바닐라시럽 = 237000
INSERT INTO orders_item (orders_idx, product_idx, count) VALUES
(35, 1, 8),
(35, 3, 30),
(35, 7, 10);

-- 강남역점 제안 발주서 2 (orders 36): 카라멜시럽+딸기퓨레+초코파우더 = 149500
INSERT INTO orders_item (orders_idx, product_idx, count) VALUES
(36, 8, 5),
(36, 12, 4),
(36, 18, 3);

-- ============================================================
-- 13. Delivery (배송)
-- ============================================================
INSERT INTO delivery (orders_idx, delivery_status, delay_reason, dep_date, est_des_date, delivered_date) VALUES
-- 강남역점 1차: 배송완료
(1, 'DELIVERED', NULL, '2026-04-10 14:00:00', '2026-04-11 10:00:00', '2026-04-11 09:30:00'),
-- 강남역점 2차: 배송완료
(2, 'DELIVERED', NULL, '2026-04-17 13:00:00', '2026-04-18 10:00:00', '2026-04-18 11:20:00'),
-- 강남역점 3차: 배송완료
(3, 'DELIVERED', NULL, '2026-04-24 14:00:00', '2026-04-25 10:00:00', '2026-04-25 09:45:00'),
-- 홍대입구점 1차: 배송완료
(4, 'DELIVERED', NULL, '2026-04-15 15:00:00', '2026-04-16 10:00:00', '2026-04-16 10:15:00'),
-- 신촌점: 배송중
(7, 'DELIVERYING', NULL, '2026-04-28 14:00:00', '2026-04-29 10:00:00', '2026-04-29 10:00:00'),
-- 건대입구점 1차: 배송 지연
(8, 'DELAY', '물류센터 차량 고장으로 인한 배송 지연', '2026-04-23 14:00:00', '2026-04-24 10:00:00', '2026-04-25 15:00:00'),
-- 잠실점 1차: 배송 출발
(10, 'START', NULL, '2026-04-29 08:00:00', '2026-04-29 14:00:00', '2026-04-29 14:00:00'),
-- 추가 발주 배송 (APPROVE 건)
(12, 'DELIVERED', NULL, '2026-03-05 14:00:00', '2026-03-06 10:00:00', '2026-03-06 09:30:00'),
(14, 'DELIVERED', NULL, '2026-03-08 14:00:00', '2026-03-09 10:00:00', '2026-03-09 10:15:00'),
(16, 'DELIVERED', NULL, '2026-03-10 14:00:00', '2026-03-11 10:00:00', '2026-03-11 09:45:00'),
(17, 'DELIVERED', NULL, '2026-03-20 15:00:00', '2026-03-21 10:00:00', '2026-03-21 10:30:00'),
(18, 'DELIVERED', NULL, '2026-03-12 14:00:00', '2026-03-13 10:00:00', '2026-03-13 11:00:00'),
(20, 'DELIVERED', NULL, '2026-03-15 14:00:00', '2026-03-16 10:00:00', '2026-03-16 09:50:00'),
(22, 'DELIVERED', NULL, '2026-03-25 14:00:00', '2026-03-26 10:00:00', '2026-03-26 10:10:00'),
(23, 'DELIVERED', NULL, '2026-04-01 14:00:00', '2026-04-02 10:00:00', '2026-04-02 09:40:00'),
(24, 'DELIVERED', NULL, '2026-04-03 14:00:00', '2026-04-04 10:00:00', '2026-04-04 10:20:00'),
(26, 'DELIVERED', NULL, '2026-04-02 14:00:00', '2026-04-03 10:00:00', '2026-04-03 09:55:00'),
(28, 'DELIVERED', NULL, '2026-04-05 14:00:00', '2026-04-06 10:00:00', '2026-04-06 10:30:00'),
(29, 'DELIVERED', NULL, '2026-04-10 14:00:00', '2026-04-11 10:00:00', '2026-04-11 09:45:00'),
(30, 'DELIVERED', NULL, '2026-04-07 14:00:00', '2026-04-08 10:00:00', '2026-04-08 10:05:00'),
(33, 'DELIVERED', NULL, '2026-04-12 14:00:00', '2026-04-13 10:00:00', '2026-04-13 09:50:00'),
(34, 'DELIVERED', NULL, '2026-04-15 14:00:00', '2026-04-16 10:00:00', '2026-04-16 10:15:00');

-- ============================================================
-- 14. PosPay (POS 결제 - 최근 매출 데이터)
-- ============================================================
-- 강남역점 4/28 매출
INSERT INTO pos_pay (store_idx, method, paid_at, pay_amount) VALUES
(1, 'CARD', '2026-04-28 08:12:00', 1500),
(1, 'CARD', '2026-04-28 08:35:00', 3000),
(1, 'CARD', '2026-04-28 09:02:00', 5000),
(1, 'CASH', '2026-04-28 09:15:00', 2500),
(1, 'CARD', '2026-04-28 09:42:00', 6500),
(1, 'CARD', '2026-04-28 10:10:00', 3500),
(1, 'CARD', '2026-04-28 10:33:00', 4500),
(1, 'CASH', '2026-04-28 11:05:00', 3000),
(1, 'CARD', '2026-04-28 11:28:00', 7000),
(1, 'CARD', '2026-04-28 12:15:00', 5500),
(1, 'CARD', '2026-04-28 12:45:00', 8500),
(1, 'CARD', '2026-04-28 13:20:00', 3000),
(1, 'CASH', '2026-04-28 14:00:00', 2000),
(1, 'CARD', '2026-04-28 14:35:00', 6000),
(1, 'CARD', '2026-04-28 15:10:00', 4000),
(1, 'CARD', '2026-04-28 16:22:00', 5500),
(1, 'CARD', '2026-04-28 17:05:00', 3500),
(1, 'CARD', '2026-04-28 17:45:00', 7000),
(1, 'CASH', '2026-04-28 18:10:00', 2500),
(1, 'CARD', '2026-04-28 18:55:00', 4500);

-- 강남역점 4/29 매출 (오늘)
INSERT INTO pos_pay (store_idx, method, paid_at, pay_amount) VALUES
(1, 'CARD', '2026-04-29 07:55:00', 1500),
(1, 'CARD', '2026-04-29 08:10:00', 3000),
(1, 'CARD', '2026-04-29 08:22:00', 2500),
(1, 'CARD', '2026-04-29 08:40:00', 5000),
(1, 'CASH', '2026-04-29 09:05:00', 3500);

-- 홍대입구점 4/28 매출
INSERT INTO pos_pay (store_idx, method, paid_at, pay_amount) VALUES
(2, 'CARD', '2026-04-28 09:10:00', 3000),
(2, 'CARD', '2026-04-28 09:45:00', 5500),
(2, 'CARD', '2026-04-28 10:20:00', 7000),
(2, 'CASH', '2026-04-28 10:55:00', 2500),
(2, 'CARD', '2026-04-28 11:30:00', 4000),
(2, 'CARD', '2026-04-28 12:05:00', 6500),
(2, 'CARD', '2026-04-28 12:40:00', 8000),
(2, 'CARD', '2026-04-28 13:15:00', 3500),
(2, 'CARD', '2026-04-28 14:00:00', 5000),
(2, 'CARD', '2026-04-28 14:45:00', 3000),
(2, 'CASH', '2026-04-28 15:30:00', 4500),
(2, 'CARD', '2026-04-28 16:20:00', 7000),
(2, 'CARD', '2026-04-28 17:10:00', 6000),
(2, 'CARD', '2026-04-28 18:00:00', 3500),
(2, 'CARD', '2026-04-28 19:30:00', 5000);

-- 신촌점 4/28 매출
INSERT INTO pos_pay (store_idx, method, paid_at, pay_amount) VALUES
(3, 'CARD', '2026-04-28 10:00:00', 2500),
(3, 'CARD', '2026-04-28 10:30:00', 4000),
(3, 'CASH', '2026-04-28 11:15:00', 3000),
(3, 'CARD', '2026-04-28 12:00:00', 5500),
(3, 'CARD', '2026-04-28 13:30:00', 6000),
(3, 'CARD', '2026-04-28 14:20:00', 3500),
(3, 'CARD', '2026-04-28 15:45:00', 4000),
(3, 'CARD', '2026-04-28 17:00:00', 5000),
(3, 'CASH', '2026-04-28 18:30:00', 2500),
(3, 'CARD', '2026-04-28 19:00:00', 3000);

-- 건대입구점 4/28 매출
INSERT INTO pos_pay (store_idx, method, paid_at, pay_amount) VALUES
(4, 'CARD', '2026-04-28 09:30:00', 3000),
(4, 'CARD', '2026-04-28 10:15:00', 5000),
(4, 'CASH', '2026-04-28 11:00:00', 2500),
(4, 'CARD', '2026-04-28 12:30:00', 7000),
(4, 'CARD', '2026-04-28 13:45:00', 4500),
(4, 'CARD', '2026-04-28 15:00:00', 3500),
(4, 'CARD', '2026-04-28 16:30:00', 6000),
(4, 'CARD', '2026-04-28 18:00:00', 5000);

-- 잠실점 4/28 매출
INSERT INTO pos_pay (store_idx, method, paid_at, pay_amount) VALUES
(5, 'CARD', '2026-04-28 08:30:00', 1500),
(5, 'CARD', '2026-04-28 09:15:00', 4000),
(5, 'CARD', '2026-04-28 10:00:00', 5500),
(5, 'CASH', '2026-04-28 10:45:00', 3000),
(5, 'CARD', '2026-04-28 11:30:00', 6500),
(5, 'CARD', '2026-04-28 12:15:00', 7000),
(5, 'CARD', '2026-04-28 13:00:00', 3500),
(5, 'CARD', '2026-04-28 14:30:00', 4000),
(5, 'CARD', '2026-04-28 16:00:00', 5500),
(5, 'CASH', '2026-04-28 17:30:00', 2500),
(5, 'CARD', '2026-04-28 18:45:00', 4500),
(5, 'CARD', '2026-04-28 19:30:00', 3000);

-- ============================================================
-- 15. PosOrdersItem (POS 주문 항목 - 일부 결제건)
-- ============================================================
-- 강남역점 4/28 첫 주문: 아메리카노 1잔
INSERT INTO pos_orders_item (pos_pay_idx, menu_idx, quantity) VALUES
(1, 1, 1);

-- 강남역점 4/28 두번째: 바닐라라떼 1잔
INSERT INTO pos_orders_item (pos_pay_idx, menu_idx, quantity) VALUES
(2, 3, 1);

-- 강남역점 4/28 세번째: 아메리카노 2잔 + 카페라떼 1잔
INSERT INTO pos_orders_item (pos_pay_idx, menu_idx, quantity) VALUES
(3, 1, 2),
(3, 2, 1);

-- 강남역점 4/28 네번째: 카푸치노 1잔
INSERT INTO pos_orders_item (pos_pay_idx, menu_idx, quantity) VALUES
(4, 5, 1);

-- 강남역점 4/28 다섯번째: 카라멜마끼아또 1잔 + 바닐라라떼 1잔
INSERT INTO pos_orders_item (pos_pay_idx, menu_idx, quantity) VALUES
(5, 4, 1),
(5, 3, 1);

-- 강남역점 4/28 여섯번째: 딸기스무디 1잔
INSERT INTO pos_orders_item (pos_pay_idx, menu_idx, quantity) VALUES
(6, 6, 1);

-- 강남역점 4/28 일곱번째: 망고스무디 1잔 + 아메리카노 1잔
INSERT INTO pos_orders_item (pos_pay_idx, menu_idx, quantity) VALUES
(7, 7, 1),
(7, 1, 1);

-- 강남역점 4/28 여덟번째: 녹차라떼 1잔
INSERT INTO pos_orders_item (pos_pay_idx, menu_idx, quantity) VALUES
(8, 11, 1);

-- 강남역점 4/28 아홉번째: 초코라떼 1잔 + 레몬에이드 1잔 + 아메리카노 1잔
INSERT INTO pos_orders_item (pos_pay_idx, menu_idx, quantity) VALUES
(9, 12, 1),
(9, 9, 1),
(9, 1, 1);

-- 강남역점 4/28 열번째: 카페라떼 1잔 + 바닐라라떼 1잔
INSERT INTO pos_orders_item (pos_pay_idx, menu_idx, quantity) VALUES
(10, 2, 1),
(10, 3, 1);

-- 홍대입구점 4/28: 주요 주문들
INSERT INTO pos_orders_item (pos_pay_idx, menu_idx, quantity) VALUES
(26, 3, 1),
(27, 6, 1),
(27, 7, 1),
(28, 4, 2),
(29, 1, 1),
(30, 2, 1),
(30, 9, 1);

-- ============================================================
-- 16. WasteLog (폐기 기록)
-- ============================================================
INSERT INTO waste_log (store_idx, product_idx, quantity, amount_loss, waste_date, waste_reason) VALUES
-- 강남역점
(1, 3, 5, 12500, '2026-04-20 22:00:00', '유통기한 만료'),
(1, 6, 2, 12000, '2026-04-22 22:00:00', '유통기한 만료'),
(1, 12, 1, 12000, '2026-04-25 22:00:00', '변질 (냉장고 온도 이상)'),

-- 홍대입구점
(2, 3, 8, 20000, '2026-04-18 22:00:00', '유통기한 만료'),
(2, 14, 2, 22000, '2026-04-21 22:00:00', '유통기한 만료'),
(2, 6, 3, 18000, '2026-04-24 22:00:00', '유통기한 만료 (과다 발주)'),

-- 신촌점
(3, 3, 3, 7500, '2026-04-19 22:00:00', '유통기한 만료'),

-- 건대입구점
(4, 3, 10, 25000, '2026-04-15 22:00:00', '냉장 보관 실패 (정전)'),
(4, 12, 3, 36000, '2026-04-17 22:00:00', '냉장 보관 실패 (정전)'),

-- 잠실점
(5, 3, 4, 10000, '2026-04-23 22:00:00', '유통기한 만료'),
(5, 5, 2, 9000, '2026-04-26 22:00:00', '유통기한 만료');

-- ============================================================
-- 17. News (뉴스 요약 - AI 생성)
-- ============================================================
INSERT INTO news_summary (store_idx, summary_date, target, summary_title, summary_contents, url, category) VALUES
-- 본사 대상 뉴스 (store_idx NULL)
(NULL, '2026-04-29 07:00:00', 'HQ', '커피 원두 국제 시세 상승 전망',
'브라질 가뭄으로 아라비카 원두 선물 가격이 전주 대비 8% 상승했습니다. 향후 2-3개월간 원두 수급 불안정이 예상되며, 선제적 재고 확보를 권장합니다. 로부스타 원두는 베트남 생산량 증가로 상대적으로 안정적입니다.',
'https://example.com/news/coffee-price-2026', 'RISK'),

(NULL, '2026-04-29 07:00:00', 'HQ', '식품위생법 개정안 국회 통과',
'1회용 컵 보증금제가 2026년 7월부터 전면 시행됩니다. 매장당 보증금 관리 시스템 도입이 필요하며, POS 시스템 업데이트가 요구됩니다. 위반 시 과태료 최대 300만원이 부과됩니다.',
'https://example.com/news/cup-deposit-law', 'RISK'),

(NULL, '2026-04-28 07:00:00', 'HQ', '프랜차이즈 가맹점 상생 지원 정책 발표',
'공정거래위원회가 프랜차이즈 가맹본부의 가맹점 지원 의무를 강화하는 시행령을 발표했습니다. 원자재 가격 급등 시 본부가 일정 비율을 부담하는 조항이 포함되어 있습니다.',
'https://example.com/news/franchise-policy', 'RISK'),

-- 강남역점 대상 뉴스
(1, '2026-04-29 07:00:00', 'STORE', '강남역 일대 대규모 축제 개최 예정',
'5월 1-5일 강남역 일대에서 "강남 페스티벌 2026"이 개최됩니다. 예상 방문객 50만명으로, 매출 증가가 예상됩니다. 원자재 사전 비축을 권장합니다.',
'https://example.com/news/gangnam-festival', 'LOCAL_EVENT'),

(1, '2026-04-29 07:00:00', 'STORE', '서울 강남구 내일 기온 30도 돌파 예상',
'4월 30일 서울 강남구 최고기온 31도로 예보되었습니다. 아이스 음료 및 스무디 수요 급증이 예상되며, 얼음과 과일퓨레 재고 확인을 권장합니다.',
'https://example.com/weather/gangnam-0430', 'WEATHER'),

-- 홍대입구점 대상 뉴스
(2, '2026-04-29 07:00:00', 'STORE', '홍대 앞 도로 공사로 유동인구 감소 예상',
'마포구 양화로 160 앞 도로 보수 공사가 5/1-5/15 진행됩니다. 공사 기간 동안 유동인구 20-30% 감소가 예상되므로, 발주량 조정을 권장합니다.',
'https://example.com/news/hongdae-construction', 'TRAFFIC'),

(2, '2026-04-28 07:00:00', 'STORE', '홍대 버스킹 축제 5월 개최',
'5월 매주 토요일 홍대 걷고싶은거리에서 버스킹 축제가 열립니다. 주말 매출 증가가 예상됩니다.',
'https://example.com/news/hongdae-busking', 'LOCAL_EVENT'),

-- 건대입구점 대상 뉴스
(4, '2026-04-29 07:00:00', 'STORE', '건국대학교 축제 기간 매출 증가 예상',
'건국대학교 대동제가 5/7-5/9 개최됩니다. 작년 동기간 매출이 평소 대비 180% 증가했으며, 올해도 유사한 수준이 예상됩니다. 특히 아이스 음료와 스무디 수요가 집중됩니다.',
'https://example.com/news/konkuk-festival', 'LOCAL_EVENT'),

-- 잠실점 대상 뉴스
(5, '2026-04-29 07:00:00', 'STORE', '잠실 롯데월드 연휴 특별 이벤트',
'어린이날 연휴(5/3-5/6) 롯데월드 방문객 대폭 증가 예상. 잠실역 일대 유동인구 증가에 따른 매출 상승이 기대됩니다.',
'https://example.com/news/jamsil-lotteworld', 'LOCAL_EVENT');

-- ============================================================
-- 18. HeadIncome (본사 수익)
-- ============================================================
INSERT INTO head_income (store_idx, orders_idx, price, status) VALUES
(1, 1, 310000, true),
(1, 2, 180000, true),
(1, 3, 106000, false),
(2, 4, 481000, false),
(3, 7, 161000, false),
(4, 8, 756000, false),
(5, 10, 242500, true),
-- 추가 발주 수익
(1, 12, 220000, true),
(2, 14, 255000, true),
(3, 16, 347500, false),
(3, 17, 116000, false),
(4, 18, 357500, false),
(5, 20, 285000, false),
(1, 22, 180000, false),
(1, 23, 112000, true),
(2, 24, 276500, true),
(3, 26, 222000, false),
(4, 28, 285500, false),
(4, 29, 224000, false),
(5, 30, 211500, true),
(2, 33, 216000, false),
(3, 34, 110500, true);

-- ============================================================
-- 19. HeadNotification (본사 알림)
-- ============================================================
INSERT INTO head_notification (type, title, content, is_read, created_at) VALUES
('LOW_STOCK', '재고 위험 - 한우 등심', '본사 창고 재고가 위험 수준입니다. 현재 수량: 5kg', true, '2026-04-22 07:05:00'),
('DELIVERY_DELAY', '배송 지연 - 홍대입구점', '발주 #1031 배송이 1일 지연되고 있습니다.', true, '2026-04-24 11:00:00'),
('LOW_STOCK', '재고 부족 - 올리브오일', '본사 창고 재고가 최소 기준 이하입니다. 현재 수량: 3L', false, '2026-04-28 07:00:00'),
('EXPIRY', '유통기한 임박 - 생크림', '유통기한 3일 이내 제품이 있습니다.', false, '2026-04-29 07:00:00'),
('ABNORMAL_ORDER', '비정상 발주 감지 - 역삼점', '한우 등심 50kg 발주 (평균 대비 250% 초과)', false, '2026-04-29 07:05:00');

-- ============================================================
-- 20. StoreNotification (가맹점 알림)
-- ============================================================
INSERT INTO store_notification (store_idx, type, title, is_read, created_at) VALUES
-- 강남역점
(1, 'DELIVERED', '배송 완료', true, '2026-04-11 09:35:00'),
(1, 'DELIVERED', '배송 완료', true, '2026-04-18 11:25:00'),
(1, 'DELIVERED', '배송 완료', true, '2026-04-25 09:50:00'),
(1, 'APPROVE', '승인 완료', true, '2026-04-10 10:00:00'),

-- 홍대입구점
(2, 'CRITICAL', '위험 알림(중요)', true, '2026-04-28 07:00:00'),
(2, 'APPROVE', '승인 완료', true, '2026-04-15 08:00:00'),
(2, 'DELIVERED', '배송 완료', true, '2026-04-16 10:20:00'),
(2, 'LOW', '재고 부족(저)', false, '2026-04-29 07:00:00'),
(2, 'CRITICAL', '위험 알림(중요)', false, '2026-04-29 07:00:00'),

-- 신촌점
(3, 'APPROVE', '승인 완료', true, '2026-04-20 15:00:00'),

-- 건대입구점
(4, 'CRITICAL', '위험 알림(중요)', true, '2026-04-22 07:05:00'),
(4, 'APPROVE', '승인 완료', true, '2026-04-22 07:30:00'),
(4, 'DELAY', '배송 지연', true, '2026-04-24 11:00:00'),
(4, 'DELIVERED', '배송 완료', true, '2026-04-25 15:05:00'),
(4, 'CRITICAL', '위험 알림(중요)', false, '2026-04-29 07:00:00'),

-- 잠실점
(5, 'APPROVE', '승인 완료', true, '2026-04-18 17:00:00'),
(5, 'REJECT', '반려', true, '2026-04-25 10:00:00');

-- ============================================================
-- 21. Report (리포트)
-- ============================================================
INSERT INTO report (report_title, report_file_path, created_at) VALUES
('2026년 3월 월간 매출 리포트', '/uploads/report/monthly_sales_202603.pdf', '2026-04-01 09:00:00'),
('2026년 3월 재고 현황 리포트', '/uploads/report/inventory_202603.pdf', '2026-04-01 09:30:00'),
('2026년 3월 폐기 분석 리포트', '/uploads/report/waste_analysis_202603.pdf', '2026-04-02 10:00:00'),
('2026년 4월 1주차 주간 리포트', '/uploads/report/weekly_202604_w1.pdf', '2026-04-07 09:00:00'),
('2026년 4월 2주차 주간 리포트', '/uploads/report/weekly_202604_w2.pdf', '2026-04-14 09:00:00'),
('2026년 4월 3주차 주간 리포트', '/uploads/report/weekly_202604_w3.pdf', '2026-04-21 09:00:00'),
('2026년 4월 4주차 주간 리포트', '/uploads/report/weekly_202604_w4.pdf', '2026-04-28 09:00:00');
