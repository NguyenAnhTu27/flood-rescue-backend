-- ================================================================
-- SEED DATA — Dữ liệu mẫu để test 4 API chính
-- Chạy SAU schema.sql
-- ================================================================

USE rescue_management_db;

-- ================================================================
-- TEAMS
-- ================================================================
INSERT INTO teams (code, name, team_type, status) VALUES
('T-001', 'Đội Cứu Hộ Alpha',   'rescue',    'active'),
('T-002', 'Đội Y Tế Beta',       'medical',   'active'),
('T-003', 'Đội Hậu Cần Gamma',   'logistics', 'inactive'),
('T-004', 'Đội Tìm Kiếm Delta',  'search',    'active');

-- ================================================================
-- USERS (password = '123456' plain-text cho dev)
-- ================================================================
INSERT INTO users (full_name, phone, email, password_hash, role, team_id, status) VALUES
('Nguyễn Quản Trị',   '0900000001', 'admin@rescue.vn',       '123456', 'admin',       NULL, 'active'),
('Trần Điều Phối',    '0900000002', 'dispatcher@rescue.vn',  '123456', 'dispatcher',  NULL, 'active'),
('Lê Trưởng Alpha',   '0900000003', 'alpha@rescue.vn',       '123456', 'team_leader', 1,    'active'),
('Phạm Trưởng Beta',  '0900000004', 'beta@rescue.vn',        '123456', 'team_leader', 2,    'active'),
('Hoàng Văn Dân',     '0900000005', 'citizen1@gmail.com',    '123456', 'citizen',     NULL, 'active'),
('Vũ Thị Linh',       '0900000006', 'citizen2@gmail.com',    '123456', 'citizen',     NULL, 'active');

-- ================================================================
-- ASSETS (Phương tiện)
-- ================================================================
INSERT INTO assets (code, name, asset_type, status, assigned_team) VALUES
('AS-001', 'Xe tải cứu thương số 1', 'vehicle',   'available', 1),
('AS-002', 'Xuồng máy cứu hộ',       'vehicle',   'available', 1),
('AS-003', 'Bộ dụng cụ y tế',        'medical',   'available', 2),
('AS-004', 'Máy phát điện',          'equipment', 'available', NULL);

-- ================================================================
-- RESCUE VEHICLES (Phương tiện cứu hộ do điều phối thực hiện)
-- ================================================================
INSERT INTO rescue_vehicles (code, name, vehicle_type, icon, description, status, capacity, dispatcher_id, assigned_team_id, license_plate, contact_number) VALUES
-- 1. Phương tiện cứu người (🚑 Rescue vehicles for saving people)
('RV-001', 'Xe Cứu Thương Alpha 1',      'rescue_person',        '🚑', 'Xe cứu thương trang bị dụng cụ cứu hộ hiện đại', 'available', 6, 2, 1, '51A-123.45', '0919999001'),
('RV-002', 'Xe Cứu Hộ Nhanh Alpha 2',    'rescue_person',        '🚑', 'Xe cứu hộ nhanh cho các vụ tai nạn giao thông', 'available', 4, 2, 1, '51A-124.45', '0919999002'),
('RV-003', 'Xe Du Kích Beta 1',          'rescue_person',        '🚑', 'Xe du kích cứu hộ đa năng', 'maintenance', 8, 3, 2, '51A-125.45', '0919999003'),

-- 2. Phương tiện vận chuyển hàng cứu trợ (🚛 Supply transport vehicles)
('RV-004', 'Xe Tải Gamma 1 - Hàng Cứu Trợ', 'transport_supplies', '🚛', 'Xe tải chuyên chở hàng cứu trợ, nước uống, lương thực', 'available', 5000, 2, 3, '51B-126.45', '0919999004'),
('RV-005', 'Xe Tải Gamma 2 - Hàng Cứu Trợ', 'transport_supplies', '🚛', 'Xe tải cải tiến chuyên chở hàng cứu trợ', 'available', 4500, 2, 3, '51B-127.45', '0919999005'),
('RV-006', 'Xe Tải Gamma 3 - Hàng Cứu Trợ', 'transport_supplies', '🚛', 'Xe tải chuyên chở nước sạch', 'in_use', 3000, 3, 3, '51B-128.45', '0919999006'),

-- 3. Phương tiện hỗ trợ kỹ thuật (🏗 Technical support vehicles)
('RV-007', 'Xe Thang Cứu Hộ Delta 1',    'technical_support',    '🏗', 'Xe thang 30m, trang bị cứu hộ ở độ cao', 'available', 200, 2, 4, '51C-129.45', '0919999007'),
('RV-008', 'Xe Máy Bơm Delta 2',         'technical_support',    '🏗', 'Xe máy bơm công suất lớn hỗ trợ thoát nước', 'available', 500, 2, 4, '51C-130.45', '0919999008'),
('RV-009', 'Máy Phát Điện Di Động',      'technical_support',    '🏗', 'Máy phát điện 50kVA hỗ trợ khu vực mất điện', 'available', 50, 3, 4, '51C-131.45', '0919999009'),

-- 4. Phương tiện nhỏ / thiết bị đi kèm (📦 Small vehicles/accompanying equipment)
('RV-010', 'Xuồng Cao Su Cứu Hộ 1',      'small_equipment',      '📦', 'Xuồng cao su, động cơ 15HP, cứu hộ lũ lụt', 'available', 8, 2, 1, 'XC-001', '0919999010'),
('RV-011', 'Xuồng Cao Su Cứu Hộ 2',      'small_equipment',      '📦', 'Xuồng cao su, động cơ 20HP, tốc độ cao', 'available', 10, 2, 1, 'XC-002', '0919999011'),
('RV-012', 'Xe Máy Đặc Dụng Cứu Hộ',     'small_equipment',      '📦', 'Xe máy chuyên dụng đi vào khu vực hẹp', 'available', 2, 2, 1, '51D-132.45', '0919999012'),
('RV-013', 'Thiết Bị Máy Sục Khí',       'small_equipment',      '📦', 'Máy sục khí di động hỗ trợ cứu hộ dưới nước', 'available', 20, 3, 2, 'DTB-001', '0919999013'),
('RV-014', 'Đập Nước Di Động',           'small_equipment',      '📦', 'Đập nước tạm thời, cao 1.5m, chiều dài 5m', 'maintenance', 100, 2, 3, 'DNT-001', '0919999014');

-- ================================================================
-- TASK GROUPS (gắn sẵn với team)
-- ================================================================
INSERT INTO task_groups (code, status, assigned_team_id, created_by) VALUES
('TG-2026-0001', 'idle', 1, 2),   -- Alpha team, rảnh
('TG-2026-0002', 'idle', 2, 2),   -- Beta team, rảnh
('TG-2026-0003', 'idle', 4, 2),   -- Delta team, rảnh
('TG-2026-0004', 'completed', 1, 2); -- Cũ, đã xong

-- ================================================================
-- RESCUE REQUEST mẫu (completed, không chặn citizen1 tạo mới)
-- ================================================================
INSERT INTO rescue_requests
    (code, caller_id, status, priority, affected_people_count, description, address_text)
VALUES
    ('RR-2026-0001', 5, 'completed', 'high', 5,
     'Lũ lụt tại khu vực Quận 3, cần di dời khẩn cấp 5 hộ dân.',
     '123 Đường Nguyễn Văn Linh, Quận 3, TP.HCM');

-- ================================================================
-- LOGS cho request mẫu
-- ================================================================
INSERT INTO rescue_request_logs
    (rescue_request_id, actor_id, action, note)
VALUES
    (1, 5, 'created',     'Yêu cầu cứu hộ RR-2026-0001 được tạo.'),
    (1, 2, 'assigned',    'Gán team Alpha qua TG-2026-0004.'),
    (1, 2, 'in_progress', 'Bắt đầu thực hiện cứu hộ.'),
    (1, 2, 'completed',   'Hoàn thành, di dời thành công 5 hộ dân.');

-- ================================================================
-- ITEM CATEGORIES
-- ================================================================
INSERT INTO item_categories (code, name, unit) VALUES
('FOOD-001', 'Lương thực khô (gạo, mì)',  'kg'),
('WATER-001','Nước uống đóng chai 500ml',  'thùng'),
('MED-001',  'Bộ thuốc y tế sơ cứu',      'hộp'),
('LIFE-001', 'Áo phao cứu sinh',           'cái');

-- ================================================================
-- STOCK BALANCES
-- ================================================================
INSERT INTO stock_balances (item_category_id, source_type, qty) VALUES
(1, 'warehouse', 500),
(2, 'donation',  200),
(3, 'purchase',   50),
(4, 'warehouse',  30);

-- ================================================================
-- Kiểm tra nhanh
-- Bỏ comment để chạy:
-- SELECT t.code, t.name, t.status FROM teams t;
-- SELECT u.email, u.role, u.status FROM users u;
-- SELECT tg.code, tg.status, tm.name AS team FROM task_groups tg LEFT JOIN teams tm ON tg.assigned_team_id = tm.id;
-- ================================================================
