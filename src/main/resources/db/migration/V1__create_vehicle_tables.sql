-- ============================================================================
-- Vehicle Management System - Database Schema
-- Hệ Thống Quản Lý Phương Tiện Cứu Hộ - Lũ Lụt
-- ============================================================================

-- Create VEHICLE Status Enumeration Type
-- (MySQL doesn't have built-in enum type, so we'll use VARCHAR with CHECK constraint)

-- Create vehicles table
CREATE TABLE IF NOT EXISTS vehicles (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT 'ID phương tiện',
    code VARCHAR(30) NOT NULL UNIQUE COMMENT 'Mã phương tiện (ví dụ: VH001, VH002)',
    name VARCHAR(120) NOT NULL COMMENT 'Tên phương tiện (ví dụ: Xuồng máy số 1)',
    vehicle_type VARCHAR(50) NOT NULL COMMENT 'Loại phương tiện (MOTORBOAT, AMBULANCE, CARGO_TRUCK, ...)',
    status VARCHAR(20) NOT NULL DEFAULT 'AVAILABLE' COMMENT 'Trạng thái (AVAILABLE, IN_USE, MAINTENANCE, BROKEN, INACTIVE)',
    capacity INT COMMENT 'Sức chứa/Công suất (số người hoặc tấn)',
    location VARCHAR(255) COMMENT 'Vị trí hiện tại',
    license_plate VARCHAR(20) COMMENT 'Biển số xe',
    vin_number VARCHAR(50) COMMENT 'VIN (Vehicle Identification Number)',
    last_maintenance_date DATETIME COMMENT 'Ngày bảo dưỡng lần cuối',
    next_maintenance_date DATETIME COMMENT 'Ngày bảo dưỡng tiếp theo',
    description TEXT COMMENT 'Mô tả chi tiết về phương tiện',
    contact_number VARCHAR(20) COMMENT 'Số điện thoại liên lạc',
    assigned_team_id BIGINT COMMENT 'ID của đội được gán phương tiện này',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Ngày tạo',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Ngày cập nhật',
    is_deleted BOOLEAN DEFAULT FALSE COMMENT 'Đánh dấu xóa mềm (soft delete)',
    
    -- Foreign Keys
    CONSTRAINT fk_vehicles_team FOREIGN KEY (assigned_team_id) REFERENCES teams(id) ON DELETE SET NULL ON UPDATE CASCADE,
    
    -- Indexes for better query performance
    KEY idx_vehicles_status (status),
    KEY idx_vehicles_type (vehicle_type),
    KEY idx_vehicles_team (assigned_team_id),
    KEY idx_vehicles_location (location),
    KEY idx_vehicles_code (code),
    KEY idx_vehicles_created_at (created_at),
    KEY idx_vehicles_deleted (is_deleted),
    
    -- Constraints
    CONSTRAINT chk_vehicle_status CHECK (status IN ('AVAILABLE', 'IN_USE', 'MAINTENANCE', 'BROKEN', 'INACTIVE')),
    CONSTRAINT chk_vehicle_capacity CHECK (capacity IS NULL OR capacity > 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci 
COMMENT='Bảng quản lý phương tiện cứu hộ, cứu trợ';

-- ============================================================================
-- Insert Sample Data / Dữ liệu mẫu cho các loại phương tiện
-- ============================================================================

-- Phương tiện cứu người (Rescue Vehicles)
INSERT INTO vehicles (code, name, vehicle_type, status, capacity, location, description) VALUES
('VH001', 'Xuồng máy số 1', 'MOTORBOAT', 'AVAILABLE', 6, 'Trạm Quận 1', 'Xuồng máy cứu hộ chuyên dụng, động cơ diesel'),
('VH002', 'Xuồng máy số 2', 'MOTORBOAT', 'IN_USE', 6, 'Trạm Bình Thạnh', 'Xuồng máy cứu hộ, động cơ xăng'),
('VH003', 'Thuyền cao su số 1', 'INFLATABLE_BOAT', 'AVAILABLE', 8, 'Kho Tây Hồ', 'Thuyền cao su 5m x 2.5m, bơm điện'),
('VH004', 'Xe cứu thương 01', 'AMBULANCE', 'AVAILABLE', 4, 'Bệnh viện Quốc Tế', 'Xe Isuzu, trang bị AED, máy thở'),
('VH005', 'Xe cứu thương 02', 'AMBULANCE', 'MAINTENANCE', 4, 'Gara Quận 7', 'Xe Ford, đang bảo dưỡng'),
('VH006', 'Xe cứu hộ đa năng 01', 'RESCUE_TRUCK', 'AVAILABLE', 3, 'Trạm PCCC Quận 1', 'Xe Hino, trang bị cắt, nâng, phá dỡ'),
('VH007', 'Trực thăng cứu hộ 01', 'RESCUE_HELICOPTER', 'AVAILABLE', 8, 'Sân bay Tân Sơn Nhất', 'Trực thăng Robinson R44, đã qua bảo dưỡng định kỳ');

-- Phương tiện vận chuyển hàng cứu trợ (Supply Transport)
INSERT INTO vehicles (code, name, vehicle_type, status, capacity, location, description) VALUES
('VH101', 'Xe tải cứu trợ 01', 'CARGO_TRUCK', 'AVAILABLE', 50, 'Kho Cứu trợ Quận 1', 'Xe tải Thaco 5 tấn, chở lương thực'),
('VH102', 'Xe tải cứu trợ 02', 'CARGO_TRUCK', 'IN_USE', 50, 'Khu vực ngập nước', 'Xe tải FAW 6 tấn, chở nước uống'),
('VH103', 'Xe bán tải 01', 'PICKUP_TRUCK', 'AVAILABLE', 20, 'Kho phân phối Quận 3', 'Xe Isuzu D-Max, chở hàng cứu trợ nội đô'),
('VH104', 'Container di động 01', 'MOBILE_CONTAINER', 'AVAILABLE', 100, 'Cảng Sài Gòn', 'Container 20ft, kho tạm thời'),
('VH105', 'Container di động 02', 'MOBILE_CONTAINER', 'AVAILABLE', 100, 'Kho Tân Bình', 'Container 40ft, lưu trữ dài hạn');

-- Phương tiện hỗ trợ kỹ thuật (Technical Support)
INSERT INTO vehicles (code, name, vehicle_type, status, capacity, location, description) VALUES
('VH201', 'Xe múc 01', 'EXCAVATOR', 'AVAILABLE', NULL, 'Công ty xây dựng Vạn Phúc', 'Excavator Komatsu PC200, công suất 20 tấn'),
('VH202', 'Máy bơm nước 01', 'WATER_PUMP', 'AVAILABLE', NULL, 'Trạm bơm Tây Hồ', 'Máy bơm nước 100 m3/h, diesel'),
('VH203', 'Máy phát điện 01', 'GENERATOR', 'AVAILABLE', 200, 'Kho điện Quận 6', 'Máy phát 200kVA, xăng, âm thanh thấp'),
('VH204', 'Máy phát điện 02', 'GENERATOR', 'AVAILABLE', 100, 'Kho điện Quận 6', 'Máy phát 100kVA, diesel'),
('VH205', 'Xe chỉ huy lưu động 01', 'COMMAND_CENTER', 'AVAILABLE', 10, 'Trung tâm Chỉ huy PCCC', 'Xe thành phố, trang bị trạm phát sóng, máy fax');

-- Phương tiện nhỏ / Thiết bị đi kèm (Equipment)
INSERT INTO vehicles (code, name, vehicle_type, status, capacity, location, description) VALUES
('VH301', 'Bộ áo phao 01', 'LIFE_VEST', 'AVAILABLE', 50, 'Kho Quận 1', 'Áo phao neoprene, chống nước'),
('VH302', 'Bộ phao cứu sinh 01', 'LIFE_BUOY', 'AVAILABLE', 20, 'Sân Tây Hồ', 'Phao cứu sinh cao su, đường kính 75cm'),
('VH303', 'Bộ đàm 01', 'RADIO', 'AVAILABLE', 100, 'Trạm chỉ huy PCCC', 'Bộ đàm quân sự, tần số 400-430MHz'),
('VH304', 'Drone theo dõi 01', 'DRONE', 'AVAILABLE', 2, 'Sân bay Tân Sơn Nhất', 'DJI M300 RTK, camera 12MP, thời gian bay 50 phút');

-- ============================================================================
-- Sample Team Assignment / Gán phương tiện cho đội (nếu cần)
-- ============================================================================
-- Các query gán phương tiện cho đội sẽ được thực hiện thông qua API

-- ============================================================================
-- Views và Stored Procedures (tùy chọn)
-- ============================================================================

-- View: Danh sách phương tiện có sẵn
CREATE OR REPLACE VIEW v_available_vehicles AS
SELECT 
    id,
    code,
    name,
    vehicle_type,
    status,
    capacity,
    location,
    assigned_team_id,
    created_at,
    updated_at
FROM vehicles
WHERE is_deleted = FALSE
  AND status = 'AVAILABLE'
ORDER BY created_at DESC;

-- View: Thống kê số lượng phương tiện theo loại
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

-- View: Thống kê số lượng phương tiện theo trạng thái
CREATE OR REPLACE VIEW v_vehicle_count_by_status AS
SELECT 
    status,
    COUNT(*) as count,
    SUM(CASE WHEN assigned_team_id IS NOT NULL THEN 1 ELSE 0 END) as assigned_count,
    SUM(CASE WHEN assigned_team_id IS NULL THEN 1 ELSE 0 END) as unassigned_count
FROM vehicles
WHERE is_deleted = FALSE
GROUP BY status;

-- View: Phương tiện được gán cho các đội
CREATE OR REPLACE VIEW v_vehicles_by_team AS
SELECT 
    t.id as team_id,
    t.name as team_name,
    COUNT(v.id) as vehicle_count,
    GROUP_CONCAT(v.code ORDER BY v.code SEPARATOR ', ') as vehicle_codes
FROM teams t
LEFT JOIN vehicles v ON t.id = v.assigned_team_id AND v.is_deleted = FALSE
GROUP BY t.id, t.name;

-- Stored Procedure: Cập nhật trạng thái phương tiện theo định kỳ
DELIMITER $$

CREATE PROCEDURE sp_check_maintenance_due()
BEGIN
    UPDATE vehicles
    SET status = 'MAINTENANCE'
    WHERE is_deleted = FALSE 
      AND status != 'INACTIVE'
      AND next_maintenance_date IS NOT NULL
      AND next_maintenance_date <= NOW()
      AND status != 'MAINTENANCE';
END$$

DELIMITER ;
