# Vehicle Management API Documentation
# Tài Liệu API Quản Lý Phương Tiện Cứu Hộ

## Overview / Tổng Quan
This API manages rescue and relief vehicles for the Flood Rescue Coordination and Relief Management System.
API này quản lý các phương tiện cứu hộ và cứu trợ cho Hệ Thống Điều Phối Cứu Hộ Lũ Lụt.

## Base URL
```
http://localhost:8080/api/v1/vehicles
```

## Authentication / Xác Thực
All endpoints require JWT token in Authorization header:
```
Authorization: Bearer <JWT_TOKEN>
```

## Roles & Permissions / Vai Trò & Quyền Hạn
- **MANAGER** (Quản lý): Full access to all operations
- **RESCUE_COORDINATOR** (Điều phối cứu hộ): Can create, update, assign vehicles
- **TEAM_LEAD** (Trưởng đội): Read-only access
- **ADMIN** (Quản trị viên): Full access

---

## Vehicle Types / Loại Phương Tiện

### Category 1: Rescue Vehicles (Phương tiện cứu người)
- `MOTORBOAT` - Xuồng máy / Cano cứu hộ
- `INFLATABLE_BOAT` - Thuyền cao su
- `AMBULANCE` - Xe cứu thương
- `RESCUE_TRUCK` - Xe cứu hộ đa năng
- `RESCUE_HELICOPTER` - Trực thăng cứu hộ

### Category 2: Supply Transport (Phương tiện vận chuyển hàng cứu trợ)
- `CARGO_TRUCK` - Xe tải cứu trợ
- `PICKUP_TRUCK` - Xe bán tải
- `MOBILE_CONTAINER` - Container di động

### Category 3: Technical Support (Phương tiện hỗ trợ kỹ thuật)
- `EXCAVATOR` - Xe múc / Xe xúc
- `WATER_PUMP` - Máy bơm nước
- `GENERATOR` - Máy phát điện
- `COMMAND_CENTER` - Xe chỉ huy lưu động

### Category 4: Equipment (Phương tiện nhỏ / thiết bị)
- `LIFE_VEST` - Áo phao
- `LIFE_BUOY` - Phao cứu sinh
- `RADIO` - Bộ đàm
- `DRONE` - Drone

## Vehicle Status / Trạng Thái Phương Tiện
- `AVAILABLE` - Có sẵn
- `IN_USE` - Đang sử dụng
- `MAINTENANCE` - Bảo dưỡng
- `BROKEN` - Hỏng hóc
- `INACTIVE` - Không hoạt động

---

## API Endpoints / Các Endpoint

### 1. Create Vehicle / Tạo Phương Tiện
**POST** `/`
```
Authorization: MANAGER, RESCUE_COORDINATOR

Request:
{
  "code": "VH001",
  "name": "Xuồng máy số 1",
  "vehicleType": "MOTORBOAT",
  "capacity": 6,
  "location": "Trạm Quận 1",
  "licensePlate": "51-A-123456",
  "vinNumber": "JBLF35EL542234567",
  "lastMaintenanceDate": "2025-01-15T10:00:00",
  "nextMaintenanceDate": "2025-06-15T10:00:00",
  "description": "Xuồng máy cứu hộ chuyên dụng",
  "contactNumber": "0901234567",
  "assignedTeamId": 1
}

Response (201 Created):
{
  "id": 1,
  "code": "VH001",
  "name": "Xuồng máy số 1",
  "vehicleType": "MOTORBOAT",
  "vehicleTypeDisplay": "Xuồng máy / Cano cứu hộ",
  "status": "AVAILABLE",
  "capacity": 6,
  "location": "Trạm Quận 1",
  "licensePlate": "51-A-123456",
  "vinNumber": "JBLF35EL542234567",
  "lastMaintenanceDate": "2025-01-15T10:00:00",
  "nextMaintenanceDate": "2025-06-15T10:00:00",
  "description": "Xuồng máy cứu hộ chuyên dụng",
  "contactNumber": "0901234567",
  "assignedTeamId": 1,
  "assignedTeamName": "Đội cứu hộ Quận 1",
  "createdAt": "2025-03-05T10:30:00",
  "updatedAt": "2025-03-05T10:30:00"
}
```

### 2. Get All Vehicles / Lấy Danh Sách Phương Tiện
**GET** `/`
```
Query Parameters:
- page: 0
- size: 10
- sort: createdAt,desc

Response (200 OK):
{
  "content": [
    { ... vehicle objects ... }
  ],
  "pageable": {
    "pageNumber": 0,
    "pageSize": 10,
    "sort": { ... }
  },
  "totalElements": 25,
  "totalPages": 3
}
```

### 3. Get Vehicle by ID / Lấy Chi Tiết Phương Tiện
**GET** `/{id}`
```
Path: /1

Response (200 OK):
{
  "id": 1,
  "code": "VH001",
  "name": "Xuồng máy số 1",
  ...
}
```

### 4. Get Vehicle by Code / Lấy Phương Tiện Theo Mã
**GET** `/code/{code}`
```
Path: /code/VH001

Response (200 OK):
{
  "id": 1,
  "code": "VH001",
  ...
}
```

### 5. Get Available Vehicles / Lấy Phương Tiện Có Sẵn
**GET** `/available`
```
Response (200 OK):
[
  { ... vehicle object ... },
  { ... vehicle object ... }
]
```

### 6. Get Vehicles by Status / Lấy Phương Tiện Theo Trạng Thái
**GET** `/status/{status}`
```
Path: /status/AVAILABLE
or   /status/IN_USE
or   /status/MAINTENANCE

Response (200 OK):
[
  { ... vehicle objects ... }
]
```

### 7. Get Vehicles by Type / Lấy Phương Tiện Theo Loại
**GET** `/type/{type}`
```
Path: /type/MOTORBOAT
or   /type/AMBULANCE
or   /type/CARGO_TRUCK

Response (200 OK):
[
  { ... vehicle objects ... }
]
```

### 8. Get Vehicles by Type and Status
**GET** `/type/{type}/status/{status}`
```
Path: /type/MOTORBOAT/status/AVAILABLE

Response (200 OK):
[ ... available motorboats ... ]
```

### 9. Get Vehicles by Team / Lấy Phương Tiện Của Đội
**GET** `/team/{teamId}`
```
Path: /team/1

Response (200 OK):
[
  { ... vehicles assigned to team 1 ... }
]
```

### 10. Get Vehicles by Location / Lấy Phương Tiện Theo Địa Điểm
**GET** `/location/{location}`
```
Path: /location/Trạm%20Quận%201

Response (200 OK):
[
  { ... vehicles at location ... }
]
```

### 11. Update Vehicle / Cập Nhật Thông Tin Phương Tiện
**PUT** `/{id}`
```
Authorization: MANAGER, RESCUE_COORDINATOR

Path: /1

Request:
{
  "name": "Xuồng máy số 1 (Cải tiến)",
  "vehicleType": "MOTORBOAT",
  "status": "AVAILABLE",
  "capacity": 8,
  "location": "Trạm Quận 2",
  "licensePlate": "51-A-123456",
  "vinNumber": "JBLF35EL542234567",
  "lastMaintenanceDate": "2025-02-15T10:00:00",
  "nextMaintenanceDate": "2025-08-15T10:00:00",
  "description": "Xuồng máy cải tiến",
  "contactNumber": "0901234567",
  "assignedTeamId": 2
}

Response (200 OK):
{
  "id": 1,
  "name": "Xuồng máy số 1 (Cải tiến)",
  ...
}
```

### 12. Assign Vehicle to Team / Gán Phương Tiện Cho Đội
**POST** `/{vehicleId}/assign-team/{teamId}`
```
Authorization: MANAGER, RESCUE_COORDINATOR

Path: /1/assign-team/2

Response (200 OK):
{
  "id": 1,
  "code": "VH001",
  "assignedTeamId": 2,
  "assignedTeamName": "Đội cứu hộ Quận 2",
  ...
}
```

### 13. Update Vehicle Status / Cập Nhật Trạng Thái
**PATCH** `/{vehicleId}/status/{status}`
```
Authorization: MANAGER, RESCUE_COORDINATOR

Path: /1/status/IN_USE

Response (200 OK):
{
  "id": 1,
  "status": "IN_USE",
  ...
}
```

### 14. Update Vehicle Location / Cập Nhật Vị Trí
**PATCH** `/{vehicleId}/location`
```
Authorization: MANAGER, RESCUE_COORDINATOR

Path: /1/location
Query: ?location=Khu%20vực%20ngập%20nước%20Quận%204

Response (200 OK):
{
  "id": 1,
  "location": "Khu vực ngập nước Quận 4",
  ...
}
```

### 15. Delete Vehicle (Soft Delete) / Xóa Phương Tiện
**DELETE** `/{id}`
```
Authorization: MANAGER

Path: /1

Response (204 No Content):
(Empty body)
```

### 16. Get Statistics / Lấy Thống Kê
**GET** `/statistics`
```
Response (200 OK):
{
  "totalVehicles": 25,
  "availableVehicles": 18,
  "inUseVehicles": 4,
  "maintenanceVehicles": 2,
  "byType": [
    {
      "type": "MOTORBOAT",
      "typeName": "Xuồng máy / Cano cứu hộ",
      "count": 5,
      "availableCount": 4
    },
    {
      "type": "AMBULANCE",
      "typeName": "Xe cứu thương",
      "count": 3,
      "availableCount": 2
    },
    ...
  ],
  "byStatus": [
    {
      "status": "AVAILABLE",
      "count": 18
    },
    {
      "status": "IN_USE",
      "count": 4
    },
    {
      "status": "MAINTENANCE",
      "count": 2
    },
    {
      "status": "BROKEN",
      "count": 1
    }
  ]
}
```

### 17. Count Available Vehicles by Type
**GET** `/count/available`
```
Query: ?type=MOTORBOAT

Response (200 OK):
4

Returns: Long (number of available vehicles of specified type)
```

---

## HTTP Status Codes / Mã Trạng Thái HTTP

| Code | Meaning | Ngĩa |
|------|---------|------|
| 200 | OK | Thành công |
| 201 | Created | Tạo thành công |
| 204 | No Content | Thành công (xóa) |
| 400 | Bad Request | Yêu cầu không hợp lệ |
| 401 | Unauthorized | Chưa xác thực |
| 403 | Forbidden | Bị cấm truy cập |
| 404 | Not Found | Không tìm thấy |
| 500 | Server Error | Lỗi máy chủ |

---

## Error Response Format / Định Dạng Lỗi

```json
{
  "timestamp": "2025-03-05T10:30:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed",
  "path": "/api/v1/vehicles",
  "errors": [
    {
      "field": "code",
      "message": "Mã phương tiện không được để trống"
    },
    {
      "field": "name",
      "message": "Tên phương tiện phải từ 5-120 ký tự"
    }
  ]
}
```

---

## Example Workflows / Ví Dụ Quy Trình

### Workflow 1: Register New Rescue Boat
1. Create new boat vehicle (POST /)
2. Assign to team (POST /assign-team)
3. Set location (PATCH /location)
4. Get statistics (GET /statistics)

### Workflow 2: Track Vehicle Status
1. Get vehicles by type (GET /type/MOTORBOAT)
2. Update status to IN_USE (PATCH /status)
3. Update location (PATCH /location)
4. Get team's vehicles (GET /team/{teamId})

### Workflow 3: Maintenance Management
1. Get all vehicles (GET /)
2. Check next maintenance dates
3. Update to MAINTENANCE status when due
4. View statistics (GET /statistics)

---

## Database Schema / Cơ Sở Dữ Liệu

See `setup_vehicles_table.sql` for complete schema.

Key tables:
- `vehicles` - Main vehicle information
- References to `teams` table for team assignments

---

## Notes / Ghi Chú

1. All dates use format: `yyyy-MM-ddTHH:mm:ss`
2. Soft delete is enabled - deleted vehicles aren't removed but marked as deleted
3. Pagination defaults to 10 items per page
4. All queries include only non-deleted vehicles (is_deleted = false)
5. Vietnamese display names are included for vehicle types

---

## Testing with cURL / Kiểm Tra Bằng cURL

```bash
# Create vehicle
curl -X POST http://localhost:8080/api/v1/vehicles \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"code":"VH001","name":"Xuồng máy","vehicleType":"MOTORBOAT"}'

# Get all vehicles
curl -X GET http://localhost:8080/api/v1/vehicles \
  -H "Authorization: Bearer YOUR_TOKEN"

# Get available vehicles
curl -X GET http://localhost:8080/api/v1/vehicles/available \
  -H "Authorization: Bearer YOUR_TOKEN"

# Get statistics
curl -X GET http://localhost:8080/api/v1/vehicles/statistics \
  -H "Authorization: Bearer YOUR_TOKEN"

# Update status
curl -X PATCH http://localhost:8080/api/v1/vehicles/1/status/IN_USE \
  -H "Authorization: Bearer YOUR_TOKEN"
```

---

## Support / Hỗ Trợ
For issues or questions, contact the development team.
