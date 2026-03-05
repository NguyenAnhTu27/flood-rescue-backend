-- ================================================================
-- SQL QUERIES - RESCUE VEHICLES MANAGEMENT
-- Các câu lệnh SQL tiện dụng để quản lý phương tiện cứu hộ
-- ================================================================

USE rescue_management_db;

-- ================================================================
-- 1. DANH SÁCH TẤT CẢ PHƯƠNG TIỆN
-- ================================================================

SELECT 
    rv.id,
    rv.code,
    rv.name,
    rv.vehicle_type,
    rv.icon,
    rv.status,
    rv.capacity,
    u.full_name as dispatcher_name,
    t.name as team_name,
    rv.license_plate,
    rv.contact_number,
    rv.created_at
FROM rescue_vehicles rv
LEFT JOIN users u ON rv.dispatcher_id = u.id
LEFT JOIN teams t ON rv.assigned_team_id = t.id
ORDER BY rv.vehicle_type, rv.code;

-- ================================================================
-- 2. DANH SÁCH PHƯƠNG TIỆN CỨU NGƯỜI (🚑)
-- ================================================================

SELECT 
    rv.id,
    rv.code,
    CONCAT(rv.icon, ' ', rv.name) as vehicle_info,
    rv.capacity,
    rv.status,
    u.full_name as dispatcher_name,
    t.name as team_name
FROM rescue_vehicles rv
LEFT JOIN users u ON rv.dispatcher_id = u.id
LEFT JOIN teams t ON rv.assigned_team_id = t.id
WHERE rv.vehicle_type = 'rescue_person'
ORDER BY rv.code;

-- ================================================================
-- 3. DANH SÁCH PHƯƠNG TIỆN VẬN CHUYỂN HÀNG CỨU TRỢ (🚛)
-- ================================================================

SELECT 
    rv.id,
    rv.code,
    CONCAT(rv.icon, ' ', rv.name) as vehicle_info,
    rv.capacity,
    rv.status,
    u.full_name as dispatcher_name,
    t.name as team_name
FROM rescue_vehicles rv
LEFT JOIN users u ON rv.dispatcher_id = u.id
LEFT JOIN teams t ON rv.assigned_team_id = t.id
WHERE rv.vehicle_type = 'transport_supplies'
ORDER BY rv.code;

-- ================================================================
-- 4. DANH SÁCH PHƯƠNG TIỆN HỖ TRỢ KỸ THUẬT (🏗)
-- ================================================================

SELECT 
    rv.id,
    rv.code,
    CONCAT(rv.icon, ' ', rv.name) as vehicle_info,
    rv.capacity,
    rv.status,
    u.full_name as dispatcher_name,
    t.name as team_name
FROM rescue_vehicles rv
LEFT JOIN users u ON rv.dispatcher_id = u.id
LEFT JOIN teams t ON rv.assigned_team_id = t.id
WHERE rv.vehicle_type = 'technical_support'
ORDER BY rv.code;

-- ================================================================
-- 5. DANH SÁCH PHƯƠNG TIỆN NHỎ / THIẾT BỊ ĐI KÈM (📦)
-- ================================================================

SELECT 
    rv.id,
    rv.code,
    CONCAT(rv.icon, ' ', rv.name) as vehicle_info,
    rv.capacity,
    rv.status,
    u.full_name as dispatcher_name,
    t.name as team_name
FROM rescue_vehicles rv
LEFT JOIN users u ON rv.dispatcher_id = u.id
LEFT JOIN teams t ON rv.assigned_team_id = t.id
WHERE rv.vehicle_type = 'small_equipment'
ORDER BY rv.code;

-- ================================================================
-- 6. PHƯƠNG TIỆN CÓ SẴN (AVAILABLE) THEO LOẠI
-- ================================================================

SELECT 
    rv.vehicle_type,
    rv.icon,
    COUNT(*) as total_available,
    GROUP_CONCAT(CONCAT(rv.code, ' - ', rv.name) SEPARATOR ', ') as vehicles
FROM rescue_vehicles rv
WHERE rv.status = 'available'
GROUP BY rv.vehicle_type, rv.icon
ORDER BY 
    CASE 
        WHEN rv.vehicle_type = 'rescue_person' THEN 1
        WHEN rv.vehicle_type = 'transport_supplies' THEN 2
        WHEN rv.vehicle_type = 'technical_support' THEN 3
        WHEN rv.vehicle_type = 'small_equipment' THEN 4
    END;

-- ================================================================
-- 7. THỐNG KÊ PHƯƠNG TIỆN THEO LOẠI VÀ TRẠNG THÁI
-- ================================================================

SELECT 
    vehicle_type,
    icon,
    COUNT(*) as total,
    SUM(CASE WHEN status = 'available' THEN 1 ELSE 0 END) as available,
    SUM(CASE WHEN status = 'in_use' THEN 1 ELSE 0 END) as in_use,
    SUM(CASE WHEN status = 'maintenance' THEN 1 ELSE 0 END) as maintenance,
    SUM(CASE WHEN status = 'retired' THEN 1 ELSE 0 END) as retired
FROM rescue_vehicles
GROUP BY vehicle_type, icon
ORDER BY 
    CASE 
        WHEN vehicle_type = 'rescue_person' THEN 1
        WHEN vehicle_type = 'transport_supplies' THEN 2
        WHEN vehicle_type = 'technical_support' THEN 3
        WHEN vehicle_type = 'small_equipment' THEN 4
    END;

-- ================================================================
-- 8. PHƯƠNG TIỆN ĐANG SỬ DỤNG (IN_USE)
-- ================================================================

SELECT 
    rv.code,
    rv.name,
    rv.icon,
    u.full_name as dispatcher_name,
    t.name as team_name,
    rv.updated_at as last_updated
FROM rescue_vehicles rv
LEFT JOIN users u ON rv.dispatcher_id = u.id
LEFT JOIN teams t ON rv.assigned_team_id = t.id
WHERE rv.status = 'in_use'
ORDER BY rv.updated_at DESC;

-- ================================================================
-- 9. PHƯƠNG TIỆN CẦN BẢO TRÌ (MAINTENANCE)
-- ================================================================

SELECT 
    rv.code,
    rv.name,
    rv.icon,
    u.full_name as dispatcher_name,
    t.name as team_name,
    rv.updated_at as last_updated
FROM rescue_vehicles rv
LEFT JOIN users u ON rv.dispatcher_id = u.id
LEFT JOIN teams t ON rv.assigned_team_id = t.id
WHERE rv.status = 'maintenance'
ORDER BY rv.updated_at DESC;

-- ================================================================
-- 10. PHƯƠNG TIỆN CỦA MỖI ĐỘI
-- ================================================================

SELECT 
    t.code as team_code,
    t.name as team_name,
    COUNT(rv.id) as total_vehicles,
    SUM(CASE WHEN rv.status = 'available' THEN 1 ELSE 0 END) as available,
    SUM(CASE WHEN rv.status = 'in_use' THEN 1 ELSE 0 END) as in_use,
    GROUP_CONCAT(CONCAT(rv.icon, ' ', rv.code) SEPARATOR ', ') as vehicles
FROM teams t
LEFT JOIN rescue_vehicles rv ON t.id = rv.assigned_team_id
GROUP BY t.id, t.code, t.name
ORDER BY t.code;

-- ================================================================
-- 11. PHƯƠNG TIỆN ĐƯỢC VẬN HÀNH BỞI TỪNG ĐIỀU PHỐI VIÊN
-- ================================================================

SELECT 
    u.id as dispatcher_id,
    u.full_name as dispatcher_name,
    COUNT(rv.id) as total_vehicles,
    SUM(CASE WHEN rv.status = 'available' THEN 1 ELSE 0 END) as available,
    SUM(CASE WHEN rv.status = 'in_use' THEN 1 ELSE 0 END) as in_use,
    GROUP_CONCAT(CONCAT(rv.icon, ' ', rv.code) SEPARATOR ', ') as vehicles
FROM users u
LEFT JOIN rescue_vehicles rv ON u.id = rv.dispatcher_id
WHERE u.role = 'dispatcher'
GROUP BY u.id, u.full_name
ORDER BY u.full_name;

-- ================================================================
-- 12. TÌM PHƯƠNG TIỆN THEO BIỂN SỐ XE
-- ================================================================

SELECT * FROM rescue_vehicles
WHERE license_plate = '51A-123.45';

-- ================================================================
-- 13. TÌM PHƯƠNG TIỆN THEO SỐ ĐIỆN THOẠI LIÊN HỆ
-- ================================================================

SELECT * FROM rescue_vehicles
WHERE contact_number LIKE '%9999%';

-- ================================================================
-- 14. PHƯƠNG TIỆN CÓ SỰC CHỨA LỚN NHẤT
-- ================================================================

SELECT 
    rv.code,
    rv.name,
    rv.icon,
    rv.capacity,
    rv.vehicle_type
FROM rescue_vehicles rv
WHERE rv.capacity = (SELECT MAX(capacity) FROM rescue_vehicles)
ORDER BY rv.vehicle_type;

-- ================================================================
-- 15. PHƯƠNG TIỆN CÓ SỨC CHỨA NHỎ NHẤT
-- ================================================================

SELECT 
    rv.code,
    rv.name,
    rv.icon,
    rv.capacity,
    rv.vehicle_type
FROM rescue_vehicles rv
WHERE rv.capacity > 0 AND rv.capacity = (
    SELECT MIN(capacity) FROM rescue_vehicles WHERE capacity > 0
)
ORDER BY rv.vehicle_type;

-- ================================================================
-- 16. CẬP NHẬT TRẠNG THÁI PHƯƠNG TIỆN (Example: RV-001 → in_use)
-- ================================================================

UPDATE rescue_vehicles
SET status = 'in_use', updated_at = NOW()
WHERE code = 'RV-001';

-- ================================================================
-- 17. CẬP NHẬT TRẠNG THÁI PHƯƠNG TIỆN (Example: RV-001 → available)
-- ================================================================

UPDATE rescue_vehicles
SET status = 'available', updated_at = NOW()
WHERE code = 'RV-001';

-- ================================================================
-- 18. GÁN PHƯƠNG TIỆN CHO ĐỘI
-- ================================================================

UPDATE rescue_vehicles
SET assigned_team_id = 1, updated_at = NOW()
WHERE code = 'RV-001';

-- ================================================================
-- 19. HỦY GÁN PHƯƠNG TIỆN KHỎI ĐỘI
-- ================================================================

UPDATE rescue_vehicles
SET assigned_team_id = NULL, updated_at = NOW()
WHERE code = 'RV-001';

-- ================================================================
-- 20. GÁN ĐIỀU PHỐI VIÊN CHO PHƯƠNG TIỆN
-- ================================================================

UPDATE rescue_vehicles
SET dispatcher_id = 2, updated_at = NOW()
WHERE code = 'RV-001';

-- ================================================================
-- 21. THỐNG KÊ TỔNG QUÁT
-- ================================================================

SELECT 
    COUNT(*) as total_vehicles,
    SUM(CASE WHEN status = 'available' THEN 1 ELSE 0 END) as available,
    SUM(CASE WHEN status = 'in_use' THEN 1 ELSE 0 END) as in_use,
    SUM(CASE WHEN status = 'maintenance' THEN 1 ELSE 0 END) as maintenance,
    SUM(CASE WHEN status = 'retired' THEN 1 ELSE 0 END) as retired,
    SUM(capacity) as total_capacity
FROM rescue_vehicles;

-- ================================================================
-- 22. PHƯƠNG TIỆN KHÔNG CÓ ĐỘI (UNASSIGNED)
-- ================================================================

SELECT 
    rv.code,
    rv.name,
    rv.icon,
    rv.vehicle_type,
    rv.status,
    u.full_name as dispatcher_name
FROM rescue_vehicles rv
LEFT JOIN users u ON rv.dispatcher_id = u.id
WHERE rv.assigned_team_id IS NULL
ORDER BY rv.vehicle_type;

-- ================================================================
-- 23. PHƯƠNG TIỆN KHÔNG CÓ ĐIỀU PHỐI VIÊN (UNASSIGNED)
-- ================================================================

SELECT 
    rv.code,
    rv.name,
    rv.icon,
    rv.vehicle_type,
    rv.status,
    t.name as team_name
FROM rescue_vehicles rv
LEFT JOIN teams t ON rv.assigned_team_id = t.id
WHERE rv.dispatcher_id IS NULL
ORDER BY rv.vehicle_type;

-- ================================================================
-- 24. PHƯƠNG TIỆN TẠO GẦN ĐÂY (7 NGÀY)
-- ================================================================

SELECT 
    rv.code,
    rv.name,
    rv.icon,
    rv.vehicle_type,
    DATEDIFF(NOW(), rv.created_at) as days_ago,
    u.full_name as dispatcher_name,
    t.name as team_name
FROM rescue_vehicles rv
LEFT JOIN users u ON rv.dispatcher_id = u.id
LEFT JOIN teams t ON rv.assigned_team_id = t.id
WHERE rv.created_at >= DATE_SUB(NOW(), INTERVAL 7 DAY)
ORDER BY rv.created_at DESC;

-- ================================================================
-- 25. PHƯƠNG TIỆN CẬP NHẬT GẦN ĐÂY (1 NGÀY)
-- ================================================================

SELECT 
    rv.code,
    rv.name,
    rv.icon,
    rv.status,
    rv.updated_at,
    TIMESTAMPDIFF(HOUR, rv.updated_at, NOW()) as hours_ago
FROM rescue_vehicles rv
WHERE rv.updated_at >= DATE_SUB(NOW(), INTERVAL 24 HOUR)
ORDER BY rv.updated_at DESC;

-- ================================================================
-- 26. CÔNG SUẤT THEO LOẠI (TOTAL CAPACITY)
-- ================================================================

SELECT 
    vehicle_type,
    icon,
    COUNT(*) as total_vehicles,
    SUM(CASE WHEN capacity IS NOT NULL THEN capacity ELSE 0 END) as total_capacity,
    AVG(CASE WHEN capacity IS NOT NULL THEN capacity ELSE NULL END) as avg_capacity
FROM rescue_vehicles
GROUP BY vehicle_type, icon
ORDER BY SUM(CASE WHEN capacity IS NOT NULL THEN capacity ELSE 0 END) DESC;

-- ================================================================
-- 27. XÓA PHƯƠNG TIỆN (Example: RV-020)
-- ================================================================

DELETE FROM rescue_vehicles
WHERE code = 'RV-020';

-- ================================================================
-- 28. KIỂM TRA CONSTRAINT INTEGRITY
-- ================================================================

SELECT 
    COUNT(*) as vehicles_with_missing_dispatcher,
    SUM(CASE WHEN dispatcher_id IS NULL THEN 1 ELSE 0 END) as no_dispatcher,
    SUM(CASE WHEN assigned_team_id IS NULL THEN 1 ELSE 0 END) as no_team
FROM rescue_vehicles;

-- ================================================================
-- 29. DANH SÁCH VỚI CHỈ SỐ (INDEXED QUERY)
-- ================================================================

SELECT 
    rv.id,
    rv.code,
    rv.name,
    rv.vehicle_type,
    rv.status,
    rv.capacity
FROM rescue_vehicles rv
USE INDEX(idx_rv_type, idx_rv_status)
WHERE rv.vehicle_type = 'rescue_person' 
  AND rv.status = 'available'
ORDER BY rv.code;

-- ================================================================
-- 30. EXPORT DỮ LIỆU (CSV FORMAT)
-- ================================================================

SELECT 
    rv.code,
    rv.name,
    rv.vehicle_type,
    rv.icon,
    rv.status,
    rv.capacity,
    COALESCE(u.full_name, 'Chưa gán') as dispatcher,
    COALESCE(t.name, 'Chưa gán') as team,
    rv.license_plate,
    rv.contact_number,
    rv.created_at
FROM rescue_vehicles rv
LEFT JOIN users u ON rv.dispatcher_id = u.id
LEFT JOIN teams t ON rv.assigned_team_id = t.id
ORDER BY rv.code;

-- ================================================================
-- END OF QUERIES
-- ================================================================
