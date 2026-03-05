-- ============================================================================
-- QUICK VEHICLE TABLE SETUP FOR MYSQL
-- Hệ Thống Quản Lý Phương Tiện Cứu Hộ - Lũ Lụt
-- ============================================================================
-- Copy and paste this SQL into your MySQL client to create the vehicle table
-- and sample data

-- Create vehicles table for Flood Rescue System
CREATE TABLE IF NOT EXISTS vehicles (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT 'ID phương tiện',
    code VARCHAR(30) NOT NULL UNIQUE COMMENT 'Code',
    name VARCHAR(120) NOT NULL COMMENT 'Tên phương tiện',
    vehicle_type VARCHAR(50) NOT NULL COMMENT 'Type: MOTORBOAT, AMBULANCE, CARGO_TRUCK, etc',
    status VARCHAR(20) NOT NULL DEFAULT 'AVAILABLE' COMMENT 'AVAILABLE, IN_USE, MAINTENANCE, BROKEN, INACTIVE',
    capacity INT COMMENT 'Sức chứa',
    location VARCHAR(255) COMMENT 'Vị trí hiện tại',
    license_plate VARCHAR(20) COMMENT 'Biển số xe',
    vin_number VARCHAR(50) COMMENT 'VIN Number',
    last_maintenance_date DATETIME COMMENT 'Ngày bảo dưỡng lần cuối',
    next_maintenance_date DATETIME COMMENT 'Ngày bảo dưỡng tiếp theo',
    description TEXT COMMENT 'Mô tả',
    contact_number VARCHAR(20) COMMENT 'Số điện thoại',
    assigned_team_id BIGINT COMMENT 'ID đội',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    is_deleted BOOLEAN DEFAULT FALSE,
    
    CONSTRAINT fk_vehicles_team FOREIGN KEY (assigned_team_id) REFERENCES teams(id),
    CONSTRAINT chk_vehicle_status CHECK (status IN ('AVAILABLE', 'IN_USE', 'MAINTENANCE', 'BROKEN', 'INACTIVE')),
    CONSTRAINT chk_vehicle_capacity CHECK (capacity IS NULL OR capacity > 0),
    
    KEY idx_vehicles_status (status),
    KEY idx_vehicles_type (vehicle_type),
    KEY idx_vehicles_team (assigned_team_id),
    KEY idx_vehicles_location (location)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================================
-- INSERT SAMPLE DATA
-- ============================================================================

-- Clear existing data (if needed, uncomment)
-- DELETE FROM vehicles;
-- ALTER TABLE vehicles AUTO_INCREMENT = 1;

-- 1. Phương tiện cứu người (Rescue Vehicles)
INSERT INTO vehicles (code, name, vehicle_type, status, capacity, location, description) 
VALUES 
('VH001', 'Xuồng máy số 1', 'MOTORBOAT', 'AVAILABLE', 6, 'Trạm Quận 1', 'Xuồng máy cứu hộ - động cơ diesel'),
('VH002', 'Xuồng máy số 2', 'MOTORBOAT', 'IN_USE', 6, 'Trạm Bình Thạnh', 'Xuồng máy cứu hộ - động cơ xăng'),
('VH003', 'Thuyền cao su số 1', 'INFLATABLE_BOAT', 'AVAILABLE', 8, 'Kho Tây Hồ', 'Thuyền cao su 5m x 2.5m'),
('VH004', 'Xe cứu thương 01', 'AMBULANCE', 'AVAILABLE', 4, 'Bệnh viện Quốc Tế', 'Xe Isuzu cứu thương'),
('VH005', 'Xe cứu thương 02', 'AMBULANCE', 'MAINTENANCE', 4, 'Gara Quận 7', 'Xe Ford cứu thương'),
('VH006', 'Xe cứu hộ đa năng 01', 'RESCUE_TRUCK', 'AVAILABLE', 3, 'Trạm PCCC Quận 1', 'Xe Hino công nghệ'),
('VH007', 'Trực thăng cứu hộ', 'RESCUE_HELICOPTER', 'AVAILABLE', 8, 'Sân bay', 'Trực thăng Robinson R44');

-- 2. Phương tiện vận chuyển hàng cứu trợ (Supply Transport)
INSERT INTO vehicles (code, name, vehicle_type, status, capacity, location, description) 
VALUES 
('VH101', 'Xe tải cứu trợ 01', 'CARGO_TRUCK', 'AVAILABLE', 50, 'Kho Cứu trợ', 'Xe tải Thaco 5 tấn'),
('VH102', 'Xe tải cứu trợ 02', 'CARGO_TRUCK', 'IN_USE', 50, 'Khu vực ngập', 'Xe tải FAW 6 tấn'),
('VH103', 'Xe bán tải 01', 'PICKUP_TRUCK', 'AVAILABLE', 20, 'Kho phân phối', 'Xe Isuzu D-Max'),
('VH104', 'Container di động 01', 'MOBILE_CONTAINER', 'AVAILABLE', 100, 'Cảng Sài Gòn', 'Container 20ft'),
('VH105', 'Container di động 02', 'MOBILE_CONTAINER', 'AVAILABLE', 100, 'Kho Tân Bình', 'Container 40ft');

-- 3. Phương tiện hỗ trợ kỹ thuật (Technical Support)
INSERT INTO vehicles (code, name, vehicle_type, status, capacity, location, description) 
VALUES 
('VH201', 'Xe múc 01', 'EXCAVATOR', 'AVAILABLE', NULL, 'Công ty xây dựng', 'Excavator Komatsu PC200'),
('VH202', 'Máy bơm nước 01', 'WATER_PUMP', 'AVAILABLE', NULL, 'Trạm bơm Tây Hồ', 'Bơm nước 100 m3/h'),
('VH203', 'Máy phát điện 01', 'GENERATOR', 'AVAILABLE', 200, 'Kho điện Quận 6', 'Máy phát 200kVA'),
('VH204', 'Máy phát điện 02', 'GENERATOR', 'AVAILABLE', 100, 'Kho điện Quận 6', 'Máy phát 100kVA'),
('VH205', 'Xe chỉ huy lưu động', 'COMMAND_CENTER', 'AVAILABLE', 10, 'Trung tâm Chỉ huy', 'Xe chỉ huy PCCC');

-- 4. Phương tiện nhỏ / Thiết bị (Equipment)
INSERT INTO vehicles (code, name, vehicle_type, status, capacity, location, description) 
VALUES 
('VH301', 'Bộ áo phao', 'LIFE_VEST', 'AVAILABLE', 50, 'Kho Quận 1', 'Áo phao neoprene'),
('VH302', 'Bộ phao cứu sinh', 'LIFE_BUOY', 'AVAILABLE', 20, 'Sân Tây Hồ', 'Phao cứu sinh cao su'),
('VH303', 'Bộ đàm', 'RADIO', 'AVAILABLE', 100, 'Trạm chỉ huy', 'Bộ đàm quân sự'),
('VH304', 'Drone theo dõi', 'DRONE', 'AVAILABLE', 2, 'Sân bay', 'DJI M300 RTK');

-- ============================================================================
-- CREATE VIEWS (Optional)
-- ============================================================================

-- View phương tiện có sẵn
CREATE OR REPLACE VIEW v_available_vehicles AS
SELECT 
    id, code, name, vehicle_type, status, capacity, location, created_at
FROM vehicles
WHERE is_deleted = FALSE AND status = 'AVAILABLE'
ORDER BY created_at DESC;

-- View thống kê theo loại
CREATE OR REPLACE VIEW v_vehicle_count_by_type AS
SELECT 
    vehicle_type,
    COUNT(*) as total_count,
    SUM(CASE WHEN status = 'AVAILABLE' THEN 1 ELSE 0 END) as available_count,
    SUM(CASE WHEN status = 'IN_USE' THEN 1 ELSE 0 END) as in_use_count,
    SUM(CASE WHEN status = 'MAINTENANCE' THEN 1 ELSE 0 END) as maintenance_count
FROM vehicles
WHERE is_deleted = FALSE
GROUP BY vehicle_type;

-- View thống kê theo trạng thái
CREATE OR REPLACE VIEW v_vehicle_count_by_status AS
SELECT 
    status,
    COUNT(*) as count
FROM vehicles
WHERE is_deleted = FALSE
GROUP BY status;

-- ============================================================================
-- SAMPLE QUERIES / Các truy vấn mẫu
-- ============================================================================

-- 1. Lấy tất cả phương tiện có sẵn
-- SELECT * FROM vehicles WHERE status = 'AVAILABLE' AND is_deleted = FALSE;

-- 2. Lấy phương tiện theo loại (ví dụ: Xuồng máy)
-- SELECT * FROM vehicles WHERE vehicle_type = 'MOTORBOAT' AND is_deleted = FALSE;

-- 3. Lấy phương tiện đang cần bảo dưỡng
-- SELECT * FROM vehicles WHERE next_maintenance_date <= NOW() AND is_deleted = FALSE;

-- 4. Lấy thống kê
-- SELECT * FROM v_vehicle_count_by_type;
-- SELECT * FROM v_vehicle_count_by_status;

-- 5. Cập nhật vị trí phương tiện
-- UPDATE vehicles SET location = 'Địa điểm mới' WHERE id = 1;

-- 6. Cập nhật trạng thái phương tiện
-- UPDATE vehicles SET status = 'IN_USE' WHERE id = 1;

-- 7. Tìm phương tiện theo mã
-- SELECT * FROM vehicles WHERE code = 'VH001';
