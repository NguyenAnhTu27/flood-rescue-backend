# 🚑 POSTMAN TESTING GUIDE - RESCUE VEHICLES API

Hướng dẫn test API Phương Tiện Cứu Hộ bằng Postman

---

## 📦 Import Collection

Thêm các request này vào Postman collection của bạn, hoặc copy-paste các ví dụ dưới đây.

---

## 🔐 Xác Thực (Authentication)

Tất cả request `POST`, `PUT`, `DELETE` cần header:
```
Authorization: Bearer <access_token>
```

**Để lấy token:**
1. Login bằng endpoint `/api/auth/login`
2. Copy `accessToken` từ response
3. Paste vào **Authorization** tab → **Bearer Token**

---

## ✅ REQUEST COLLECTIONS

### 1️⃣ GET ALL VEHICLES
**Lấy danh sách tất cả phương tiện**

```http
GET http://localhost:8080/api/rescue-vehicles
```

**Expected Response (200 OK):**
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
      "status": "available",
      "capacity": 6,
      "dispatcherName": "Trần Điều Phối",
      "assignedTeamName": "Đội Cứu Hộ Alpha",
      "licensePlate": "51A-123.45",
      "contactNumber": "0919999001"
    }
  ]
}
```

---

### 2️⃣ GET VEHICLE BY ID
**Lấy chi tiết một phương tiện**

```http
GET http://localhost:8080/api/rescue-vehicles/1
```

---

### 3️⃣ GET BY CODE
**Tìm phương tiện theo code**

```http
GET http://localhost:8080/api/rescue-vehicles/search/by-code?code=RV-001
```

---

### 4️⃣ GET BY TYPE
**Tìm phương tiện theo loại**

```http
GET http://localhost:8080/api/rescue-vehicles/search/by-type?type=rescue_person
```

**Query Parameters:**
- `type=rescue_person` → Phương tiện cứu người 🚑
- `type=transport_supplies` → Phương tiện vận chuyển hàng 🚛
- `type=technical_support` → Phương tiện hỗ trợ kỹ thuật 🏗
- `type=small_equipment` → Phương tiện nhỏ 📦

---

### 5️⃣ GET AVAILABLE BY TYPE
**Tìm phương tiện có sẵn theo loại**

```http
GET http://localhost:8080/api/rescue-vehicles/search/available-by-type?type=rescue_person
```

---

### 6️⃣ GET BY STATUS
**Tìm phương tiện theo trạng thái**

```http
GET http://localhost:8080/api/rescue-vehicles/search/by-status?status=available
```

**Query Parameters:**
- `status=available` → Có sẵn
- `status=in_use` → Đang sử dụng
- `status=maintenance` → Bảo trì
- `status=retired` → Đã loại biên

---

### 7️⃣ GET BY TEAM
**Tìm phương tiện của đội**

```http
GET http://localhost:8080/api/rescue-vehicles/search/by-team?teamId=1
```

---

### 8️⃣ GET BY DISPATCHER
**Tìm phương tiện của điều phối viên**

```http
GET http://localhost:8080/api/rescue-vehicles/search/by-dispatcher?dispatcherId=2
```

---

### 9️⃣ CREATE NEW VEHICLE
**Tạo mới phương tiện cứu hộ**

```http
POST http://localhost:8080/api/rescue-vehicles
Content-Type: application/json
Authorization: Bearer <token>

{
  "code": "RV-101",
  "name": "Xe Cứu Thương Mới - Test",
  "vehicleType": "rescue_person",
  "icon": "🚑",
  "description": "Xe cứu thương mới trang bị đầy đủ",
  "status": "available",
  "capacity": 8,
  "dispatcherId": 2,
  "assignedTeamId": 1,
  "licensePlate": "51A-001.01",
  "contactNumber": "0919999001"
}
```

**Expected Response (200 OK):**
```json
{
  "success": true,
  "message": "Tạo mới phương tiện thành công",
  "data": {
    "id": 15,
    "code": "RV-101",
    "name": "Xe Cứu Thương Mới - Test",
    ...
  }
}
```

---

### 🔟 CREATE - TRANSPORT SUPPLIES VEHICLE
**Tạo phương tiện vận chuyển hàng cứu trợ**

```http
POST http://localhost:8080/api/rescue-vehicles
Content-Type: application/json
Authorization: Bearer <token>

{
  "code": "RV-102",
  "name": "Xe Tải Cứu Trợ Gamma Mới",
  "vehicleType": "transport_supplies",
  "icon": "🚛",
  "description": "Xe tải chuyên chở hàng cứu trợ, nước sạch, lương thực khô",
  "status": "available",
  "capacity": 3000,
  "dispatcherId": 2,
  "assignedTeamId": 3,
  "licensePlate": "51B-001.01",
  "contactNumber": "0919888888"
}
```

---

### 1️⃣1️⃣ CREATE - TECHNICAL SUPPORT VEHICLE
**Tạo phương tiện hỗ trợ kỹ thuật**

```http
POST http://localhost:8080/api/rescue-vehicles
Content-Type: application/json
Authorization: Bearer <token>

{
  "code": "RV-103",
  "name": "Máy Phát Điện Dự Phòng",
  "vehicleType": "technical_support",
  "icon": "🏗",
  "description": "Máy phát điện 100kVA hỗ trợ khu vực mất điện",
  "status": "available",
  "capacity": 100,
  "dispatcherId": 2,
  "assignedTeamId": 4,
  "licensePlate": "51C-001.01",
  "contactNumber": "0919777777"
}
```

---

### 1️⃣2️⃣ CREATE - SMALL EQUIPMENT VEHICLE
**Tạo phương tiện nhỏ / thiết bị đi kèm**

```http
POST http://localhost:8080/api/rescue-vehicles
Content-Type: application/json
Authorization: Bearer <token>

{
  "code": "RV-104",
  "name": "Xuồng Cao Su Cứu Hộ Số 3",
  "vehicleType": "small_equipment",
  "icon": "📦",
  "description": "Xuồng cao su động cơ 25HP, cứu hộ lũ lụt, địa hình khó",
  "status": "available",
  "capacity": 12,
  "dispatcherId": 2,
  "assignedTeamId": 1,
  "licensePlate": "XC-003",
  "contactNumber": "0919666666"
}
```

---

### 1️⃣3️⃣ UPDATE VEHICLE
**Cập nhật thông tin phương tiện**

```http
PUT http://localhost:8080/api/rescue-vehicles/1
Content-Type: application/json
Authorization: Bearer <token>

{
  "name": "Xe Cứu Thương Alpha 1 - Nâng Cấp 2026",
  "description": "Xe cứu thương nâng cấp với thiết bị y tế hiện đại nhất",
  "capacity": 10,
  "contactNumber": "0919999999"
}
```

---

### 1️⃣4️⃣ UPDATE STATUS
**Cập nhật trạng thái phương tiện**

```http
PUT http://localhost:8080/api/rescue-vehicles/1/status
Content-Type: application/json
Authorization: Bearer <token>

{
  "status": "in_use"
}
```

---

### 1️⃣5️⃣ UPDATE STATUS - MAINTENANCE
**Chuyển phương tiện vào bảo trì**

```http
PUT http://localhost:8080/api/rescue-vehicles/3/status
Content-Type: application/json
Authorization: Bearer <token>

{
  "status": "maintenance"
}
```

---

### 1️⃣6️⃣ ASSIGN TO TEAM
**Gán phương tiện cho đội**

```http
POST http://localhost:8080/api/rescue-vehicles/1/assign-team
Content-Type: application/json
Authorization: Bearer <token>

{
  "teamId": 2
}
```

---

### 1️⃣7️⃣ ASSIGN DISPATCHER
**Gán người điều hành cho phương tiện**

```http
POST http://localhost:8080/api/rescue-vehicles/1/assign-dispatcher
Content-Type: application/json
Authorization: Bearer <token>

{
  "dispatcherId": 3
}
```

---

### 1️⃣8️⃣ DELETE VEHICLE
**Xóa phương tiện**

```http
DELETE http://localhost:8080/api/rescue-vehicles/15
Authorization: Bearer <token>
```

**Expected Response (200 OK):**
```json
{
  "success": true,
  "message": "Xóa phương tiện thành công",
  "data": "OK"
}
```

---

### 1️⃣9️⃣ GET STATISTICS
**Lấy thống kê phương tiện**

```http
GET http://localhost:8080/api/rescue-vehicles/stats
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

---

### 2️⃣0️⃣ GET VEHICLE TYPES
**Lấy danh sách loại phương tiện**

```http
GET http://localhost:8080/api/rescue-vehicles/types
```

**Response:**
```json
{
  "success": true,
  "message": "Danh sách loại phương tiện",
  "data": [
    { "value": "rescue_person", "label": "🚑 Phương tiện cứu người" },
    { "value": "transport_supplies", "label": "🚛 Phương tiện vận chuyển hàng cứu trợ" },
    { "value": "technical_support", "label": "🏗 Phương tiện hỗ trợ kỹ thuật" },
    { "value": "small_equipment", "label": "📦 Phương tiện nhỏ / thiết bị đi kèm" }
  ]
}
```

---

### 2️⃣1️⃣ GET STATUSES
**Lấy danh sách trạng thái**

```http
GET http://localhost:8080/api/rescue-vehicles/statuses
```

---

## 🧪 Test Cases - Senario Thực Tế

### Test 1: Tìm Phương Tiện Cứu Người Có Sẵn

**Bước 1:** GET `/api/rescue-vehicles/search/available-by-type?type=rescue_person`

**Kỳ vọng:** Danh sách chỉ gồm phương tiện:
- vehicle_type = rescue_person
- status = available

---

### Test 2: Gán Phương Tiện Cho Đội Mới

**Bước 1:** POST `/api/rescue-vehicles` tạo phương tiện mới
- Không gán team (assignedTeamId = null)

**Bước 2:** GET `/api/rescue-vehicles/{id}` kiểm tra assignedTeamName = null

**Bước 3:** POST `/api/rescue-vehicles/{id}/assign-team` gán team=1

**Bước 4:** GET `/api/rescue-vehicles/{id}` kiểm tra assignedTeamName = "Đội Cứu Hộ Alpha"

---

### Test 3: Cập Nhật Trạng Thái Phương Tiện

**Bước 1:** GET `/api/rescue-vehicles/search/by-status?status=available` - đếm số lượng

**Bước 2:** PUT `/api/rescue-vehicles/1/status` sang maintenance

**Bước 3:** GET `/api/rescue-vehicles/search/by-status?status=available` - kiểm tra giảm 1

**Bước 4:** GET `/api/rescue-vehicles/search/by-status?status=maintenance` - kiểm tra tăng 1

---

### Test 4: Tìm Phương Tiện Của Điều Phối Viên

**Bước 1:** GET `/api/rescue-vehicles/search/by-dispatcher?dispatcherId=2`

**Kỳ vọng:** Danh sách gồm các phương tiện có `dispatcherId=2`

---

### Test 5: Thống Kê Phương Tiện

**Bước 1:** GET `/api/rescue-vehicles/stats`

**Bước 2:** GET `/api/rescue-vehicles` - đếm từng loại status

**Bước 3:** So sánh kết quả từ stats endpoint

---

## ⚠️ Error Handling Tests

### Test 1: Code Trùng
```http
POST http://localhost:8080/api/rescue-vehicles
{
  "code": "RV-001",  ← Code đã tồn tại
  ...
}
```

**Expected:** 400 Bad Request - "Code phương tiện 'RV-001' đã tồn tại"

---

### Test 2: Invalid Vehicle Type
```http
POST http://localhost:8080/api/rescue-vehicles
{
  "vehicleType": "invalid_type",  ← Loại không hợp lệ
  ...
}
```

**Expected:** 400 Bad Request - Validation error

---

### Test 3: Non-existent ID
```http
GET http://localhost:8080/api/rescue-vehicles/9999
```

**Expected:** 404 Not Found - "Không tìm thấy rescue_vehicle với ID: 9999"

---

### Test 4: Missing Authorization
```http
POST http://localhost:8080/api/rescue-vehicles
(Không có Authorization header)
```

**Expected:** 401 Unauthorized

---

## 📊 Performance Testing

### Load Test: Lấy Danh Sách To

```http
GET http://localhost:8080/api/rescue-vehicles
```

**Metrics:**
- Response time: < 500ms (với 14 phương tiện)
- Payload size: ~ 5KB

---

## 💾 Sample Data Reference

| Code | Name | Type | Icon | Status | Team | Dispatcher |
|------|------|------|------|--------|------|------------|
| RV-001 | Xe Cứu Thương Alpha 1 | rescue_person | 🚑 | available | T-001 | Trần Điều Phối |
| RV-004 | Xe Tải Gamma 1 | transport_supplies | 🚛 | available | T-003 | Trần Điều Phối |
| RV-007 | Xe Thang Delta 1 | technical_support | 🏗 | available | T-004 | Trần Điều Phối |
| RV-010 | Xuồng Cao Su 1 | small_equipment | 📦 | available | T-001 | Trần Điều Phối |

---

## 🚀 Next Steps

1. ✅ Import các request này vào Postman
2. ✅ Cấu hình base URL: `http://localhost:8080`
3. ✅ Set Authorization token
4. ✅ Chạy các test cases
5. ✅ Kiểm tra response status codes
6. ✅ Xác minh dữ liệu trong MySQL

---

**Happy Testing!** 🎉
