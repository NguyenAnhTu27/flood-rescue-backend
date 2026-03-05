# 🚑 PHƯƠNG TIỆN CỨU HỘ - HƯỚNG DẪN HOÀN CHỈNH

## 📋 Mục Lục
1. [Giới Thiệu](#giới-thiệu)
2. [Cấu Trúc Cơ Sở Dữ Liệu](#cấu-trúc-cơ-sở-dữ-liệu)
3. [Cài Đặt](#cài-đặt)
4. [Chạy SQL](#chạy-sql)
5. [API Endpoints](#api-endpoints)
6. [Ví Dụ Sử Dụng](#ví-dụ-sử-dụng)

---

## Giới Thiệu

Hệ thống quản lý **Phương Tiện Cứu Hộ** là một phần của dự án **Rescue Management System**.

### 4 Loại Phương Tiện Cứu Hộ

| Icon | Loại | Mã Code | Ví Dụ |
|------|------|--------|-------|
| 🚑 | **Phương tiện cứu người** (rescue_person) | RV-001, RV-002, RV-003 | Xe cứu thương, xe du kích cứu hộ |
| 🚛 | **Phương tiện vận chuyển hàng cứu trợ** (transport_supplies) | RV-004, RV-005, RV-006 | Xe tải hàng cứu trợ, xe chở nước sạch |
| 🏗 | **Phương tiện hỗ trợ kỹ thuật** (technical_support) | RV-007, RV-008, RV-009 | Xe thang cứu hộ, máy bơm, máy phát điện |
| 📦 | **Phương tiện nhỏ / thiết bị đi kèm** (small_equipment) | RV-010, RV-011, RV-012 | Xuồng cao su, xe máy đặc dụng, thiết bị máy sục |

---

## Cấu Trúc Cơ Sở Dữ Liệu

### Bảng: `rescue_vehicles`

```sql
CREATE TABLE IF NOT EXISTS rescue_vehicles (
    id               BIGINT       NOT NULL AUTO_INCREMENT PRIMARY KEY,
    code             VARCHAR(50)  NOT NULL UNIQUE    COMMENT 'RV-001, RV-002, ...',
    name             VARCHAR(100) NOT NULL,
    vehicle_type     VARCHAR(50)  NOT NULL           COMMENT 'rescue_person|transport_supplies|technical_support|small_equipment',
    icon             VARCHAR(10)                     COMMENT '🚑|🚛|🏗|📦',
    description      TEXT,
    status           VARCHAR(20)  NOT NULL DEFAULT 'available'
                                  COMMENT 'available|in_use|maintenance|retired',
    capacity         INT                             COMMENT 'Sức chứa, số lượng người hoặc kg',
    dispatcher_id    BIGINT                          COMMENT 'FK → users.id (điều phối viên vận hành)',
    assigned_team_id BIGINT                          COMMENT 'FK → teams.id',
    license_plate    VARCHAR(20)                     COMMENT 'Biển số xe',
    contact_number   VARCHAR(20),
    created_at       DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at       DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_rv_dispatcher FOREIGN KEY (dispatcher_id) REFERENCES users(id) ON DELETE SET NULL,
    CONSTRAINT fk_rv_team       FOREIGN KEY (assigned_team_id) REFERENCES teams(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Index cho tìm kiếm nhanh
CREATE INDEX idx_rv_status ON rescue_vehicles(status);
CREATE INDEX idx_rv_type ON rescue_vehicles(vehicle_type);
CREATE INDEX idx_rv_dispatcher ON rescue_vehicles(dispatcher_id);
CREATE INDEX idx_rv_team ON rescue_vehicles(assigned_team_id);
```

### Mối Quan Hệ (Relationships)

- **RescueVehicle → User** (ManyToOne): Điều phối viên vận hành phương tiện
- **RescueVehicle → Team** (ManyToOne): Đội được gán phương tiện

---

## Cài Đặt

### 1. Cấu Hình `pom.xml`

Không cần thêm dependency, dự án đã có:
- JPA/Hibernate
- Spring Data
- Lombok
- Jakarta Persistence

### 2. Cấu Hình Application Properties

```properties
# application.properties
spring.jpa.hibernate.ddl-auto=validate
spring.datasource.url=jdbc:mysql://localhost:3306/rescue_management_db
spring.datasource.username=root
spring.datasource.password=your-password
```

---

## Chạy SQL

### Bước 1: Tạo Database

Chạy file `schema.sql` trước:

```bash
cd d:\SWP391\backend-backup\src\main\resources\db
mysql -u root -p < schema.sql
```

Hoặc trong **MySQL Workbench**:
1. Mở file `schema.sql`
2. Chọn **Query** → **Execute All** (Ctrl+Shift+Enter)

### Bước 2: Insert Dữ Liệu Mẫu

Chạy file `data.sql` sau khi schema:

```bash
mysql -u root -p rescue_management_db < data.sql
```

**Hoặc trong MySQL Workbench**: Mở file `data.sql` → Execute All

### Bước 3: Xác Minh Dữ Liệu

```sql
-- Kiểm tra bảng rescue_vehicles
SELECT rv.code, rv.name, rv.vehicle_type, rv.icon, rv.status, rv.capacity, u.full_name as dispatcher, t.name as team_name
FROM rescue_vehicles rv
LEFT JOIN users u ON rv.dispatcher_id = u.id
LEFT JOIN teams t ON rv.assigned_team_id = t.id
ORDER BY rv.vehicle_type;

-- Đếm số lượng phương tiện
SELECT 
    vehicle_type, 
    icon,
    COUNT(*) as total,
    SUM(CASE WHEN status = 'available' THEN 1 ELSE 0 END) as available,
    SUM(CASE WHEN status = 'in_use' THEN 1 ELSE 0 END) as in_use,
    SUM(CASE WHEN status = 'maintenance' THEN 1 ELSE 0 END) as maintenance
FROM rescue_vehicles
GROUP BY vehicle_type, icon;
```

**Dữ liệu mẫu sẽ insert:**
- **14 phương tiện cứu hộ** với các loại khác nhau
- Được gán cho các đội (team) khác nhau
- Có điều phối viên quản lý
- Các trạng thái khác nhau

---

## API Endpoints

### Base URL
```
http://localhost:8080/api/rescue-vehicles
```

### 1. Lấy Tất Cả Phương Tiện
```http
GET /api/rescue-vehicles
```

**Response:**
```json
{
  "success": true,
  "message": "Danh sách 14 phương tiện",
  "data": [
    {
      "id": 1,
      "code": "RV-001",
      "name": "Xe Cứu Thương Alpha 1",
      "vehicleType": "rescue_person",
      "icon": "🚑",
      "description": "Xe cứu thương trang bị dụng cụ cứu hộ hiện đại",
      "status": "available",
      "capacity": 6,
      "dispatcherId": 2,
      "dispatcherName": "Trần Điều Phối",
      "assignedTeamId": 1,
      "assignedTeamName": "Đội Cứu Hộ Alpha",
      "licensePlate": "51A-123.45",
      "contactNumber": "0919999001",
      "createdAt": "2026-03-05T10:15:00",
      "updatedAt": "2026-03-05T10:15:00"
    },
    ...
  ]
}
```

### 2. Lấy Chi Tiết Một Phương Tiện
```http
GET /api/rescue-vehicles/{id}
```

**Example:**
```http
GET /api/rescue-vehicles/1
```

### 3. Tạo Mới Phương Tiện
```http
POST /api/rescue-vehicles
Content-Type: application/json
Authorization: Bearer <token>

{
  "code": "RV-015",
  "name": "Xe Cứu Thương Mới",
  "vehicleType": "rescue_person",
  "icon": "🚑",
  "description": "Xe cứu thương mới trang bị hiện đại",
  "status": "available",
  "capacity": 8,
  "dispatcherId": 2,
  "assignedTeamId": 1,
  "licensePlate": "51A-999.99",
  "contactNumber": "0919999999"
}
```

**Response:**
```json
{
  "success": true,
  "message": "Tạo mới phương tiện thành công",
  "data": {
    "id": 15,
    "code": "RV-015",
    "name": "Xe Cứu Thương Mới",
    "vehicleType": "rescue_person",
    "icon": "🚑",
    ...
  }
}
```

### 4. Cập Nhật Phương Tiện
```http
PUT /api/rescue-vehicles/{id}
Content-Type: application/json
Authorization: Bearer <token>

{
  "name": "Xe Cứu Thương Alpha 1 - Nâng Cấp",
  "status": "maintenance",
  "capacity": 8,
  "contactNumber": "0919999999"
}
```

### 5. Xóa Phương Tiện
```http
DELETE /api/rescue-vehicles/{id}
Authorization: Bearer <token>
```

### 6. Tìm Phương Tiện Theo Code
```http
GET /api/rescue-vehicles/search/by-code?code=RV-001
```

### 7. Tìm Phương Tiện Theo Loại
```http
GET /api/rescue-vehicles/search/by-type?type=rescue_person
```

**Loại hợp lệ:**
- `rescue_person` (🚑)
- `transport_supplies` (🚛)
- `technical_support` (🏗)
- `small_equipment` (📦)

### 8. Tìm Phương Tiện Có Sẵn Theo Loại
```http
GET /api/rescue-vehicles/search/available-by-type?type=rescue_person
```

### 9. Tìm Phương Tiện Theo Trạng Thái
```http
GET /api/rescue-vehicles/search/by-status?status=available
```

**Trạng thái hợp lệ:**
- `available` - Có sẵn
- `in_use` - Đang sử dụng
- `maintenance` - Bảo trì
- `retired` - Đã loại biên

### 10. Tìm Phương Tiện Của Đội
```http
GET /api/rescue-vehicles/search/by-team?teamId=1
```

### 11. Tìm Phương Tiện Của Điều Phối Viên
```http
GET /api/rescue-vehicles/search/by-dispatcher?dispatcherId=2
```

### 12. Cập Nhật Trạng Thái Phương Tiện
```http
PUT /api/rescue-vehicles/{id}/status
Content-Type: application/json
Authorization: Bearer <token>

{
  "status": "in_use"
}
```

### 13. Gán Phương Tiện Cho Đội
```http
POST /api/rescue-vehicles/{id}/assign-team
Content-Type: application/json
Authorization: Bearer <token>

{
  "teamId": 1
}
```

### 14. Gán Điều Phối Viên Cho Phương Tiện
```http
POST /api/rescue-vehicles/{id}/assign-dispatcher
Content-Type: application/json
Authorization: Bearer <token>

{
  "dispatcherId": 2
}
```

### 15. Lấy Thống Kê Phương Tiện
```http
GET /api/rescue-vehicles/stats
```

**Response:**
```json
{
  "success": true,
  "message": "Thống kê phương tiện",
  "data": {
    "totalVehicles": 14,
    "availableCount": 11,
    "inUseCount": 2,
    "maintenanceCount": 1
  }
}
```

### 16. Lấy Danh Sách Loại Phương Tiện
```http
GET /api/rescue-vehicles/types
```

**Response:**
```json
{
  "success": true,
  "message": "Danh sách loại phương tiện",
  "data": [
    {
      "value": "rescue_person",
      "label": "🚑 Phương tiện cứu người"
    },
    {
      "value": "transport_supplies",
      "label": "🚛 Phương tiện vận chuyển hàng cứu trợ"
    },
    {
      "value": "technical_support",
      "label": "🏗 Phương tiện hỗ trợ kỹ thuật"
    },
    {
      "value": "small_equipment",
      "label": "📦 Phương tiện nhỏ / thiết bị đi kèm"
    }
  ]
}
```

### 17. Lấy Danh Sách Trạng Thái
```http
GET /api/rescue-vehicles/statuses
```

**Response:**
```json
{
  "success": true,
  "message": "Danh sách trạng thái",
  "data": [
    { "value": "available", "label": "Có sẵn" },
    { "value": "in_use", "label": "Đang sử dụng" },
    { "value": "maintenance", "label": "Bảo trì" },
    { "value": "retired", "label": "Đã loại biên" }
  ]
}
```

---

## Ví Dụ Sử Dụng

### Ví Dụ 1: Lấy Tất Cả Phương Tiện Cứu Người Có Sẵn

```bash
curl -X GET "http://localhost:8080/api/rescue-vehicles/search/available-by-type?type=rescue_person"
```

### Ví Dụ 2: Tạo Phương Tiện Vận Chuyển Hàng Cứu Trợ Mới

```bash
curl -X POST "http://localhost:8080/api/rescue-vehicles" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -d '{
    "code": "RV-020",
    "name": "Xe Tải Mới - Hàng Cứu Trợ",
    "vehicleType": "transport_supplies",
    "icon": "🚛",
    "description": "Xe tải chuyên chở hàng cứu trợ",
    "status": "available",
    "capacity": 4000,
    "dispatcherId": 2,
    "assignedTeamId": 3,
    "licensePlate": "51B-555.55",
    "contactNumber": "0919888888"
  }'
```

### Ví Dụ 3: Cập Nhật Trạng Thái Phương Tiện Sang "Đang Sử Dụng"

```bash
curl -X PUT "http://localhost:8080/api/rescue-vehicles/1/status" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -d '{
    "status": "in_use"
  }'
```

### Ví Dụ 4: Gán Phương Tiện Cho Đội

```bash
curl -X POST "http://localhost:8080/api/rescue-vehicles/7/assign-team" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -d '{
    "teamId": 4
  }'
```

### Ví Dụ 5: Tìm Phương Tiện Hỗ Trợ Kỹ Thuật

```bash
curl -X GET "http://localhost:8080/api/rescue-vehicles/search/by-type?type=technical_support"
```

---

## Dữ Liệu Mẫu Đã Insert

### 1. Phương Tiện Cứu Người (🚑) - 3 chiếc
- **RV-001**: Xe Cứu Thương Alpha 1 (6 người)
- **RV-002**: Xe Cứu Hộ Nhanh Alpha 2 (4 người)
- **RV-003**: Xe Du Kích Beta 1 (8 người) - [MAINTENANCE]

### 2. Phương Tiện Vận Chuyển Hàng Cứu Trợ (🚛) - 3 chiếc
- **RV-004**: Xe Tải Gamma 1 (5000 kg)
- **RV-005**: Xe Tải Gamma 2 (4500 kg)
- **RV-006**: Xe Tải Gamma 3 (3000 kg) - [IN_USE]

### 3. Phương Tiện Hỗ Trợ Kỹ Thuật (🏗) - 3 chiếc
- **RV-007**: Xe Thang Cứu Hộ Delta 1 (200 m)
- **RV-008**: Xe Máy Bơm Delta 2 (500 m³/h)
- **RV-009**: Máy Phát Điện (50 kVA)

### 4. Phương Tiện Nhỏ / Thiết Bị Đi Kèm (📦) - 5 chiếc
- **RV-010**: Xuồng Cao Su 1 (8 người)
- **RV-011**: Xuồng Cao Su 2 (10 người)
- **RV-012**: Xe Máy Đặc Dụng (2 người)
- **RV-013**: Thiết Bị Máy Sục Khí (20 m³/h)
- **RV-014**: Đập Nước Di Động (100 m³) - [MAINTENANCE]

---

## Lỗi Phổ Biến & Giải Pháp

| Lỗi | Nguyên Nhân | Giải Pháp |
|-----|-----------|----------|
| `Code phương tiện đã tồn tại` | Code bị trùng | Sử dụng code khác |
| `Không tìm thấy user` | dispatcherId không tồn tại | Kiểm tra ID user trong bảng users |
| `Không tìm thấy team` | assignedTeamId không tồn tại | Kiểm tra ID team trong bảng teams |
| `Vehicle type không hợp lệ` | Loại phương tiện sai | Dùng: `rescue_person`, `transport_supplies`, `technical_support`, `small_equipment` |
| `Status không hợp lệ` | Trạng thái sai | Dùng: `available`, `in_use`, `maintenance`, `retired` |

---

## Tổng Kết

✅ **Hoàn tất cài đặt:**
1. ✅ Schema database (bảng `rescue_vehicles`)
2. ✅ Dữ liệu mẫu (14 phương tiện)
3. ✅ Entity JPA (`RescueVehicle`)
4. ✅ DTOs (Request/Response)
5. ✅ Repository (queries)
6. ✅ Service (business logic)
7. ✅ Controller (17 API endpoints)
8. ✅ Documentation (hướng dẫn này)

**Bạn có thể bắt đầu sử dụng API ngay!** 🚀
