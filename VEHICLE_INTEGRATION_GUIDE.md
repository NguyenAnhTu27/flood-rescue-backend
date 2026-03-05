# Quick Integration Guide
# Hướng Dẫn Tích Hợp Nhanh Chóng

## 1. Database Setup / Cài Đặt Cơ Sở Dữ Liệu

### Option A: Flyway (Automatic - Tự động)
The database tables will be created automatically when the application starts.
Các bảng dữ liệu sẽ được tạo tự động khi ứng dụng khởi động.

**File**: `src/main/resources/db/migration/V1__create_vehicle_tables.sql`

### Option B: Manual SQL (Manual - Thủ công)
```bash
# Connect to MySQL
mysql -u root -p flood_rescue_db < setup_vehicles_table.sql
```

**File**: `setup_vehicles_table.sql` (in project root)

## 2. Code Structure / Cấu Trúc Mã

The vehicle module follows the existing architecture pattern:

```
module/vehicle/
├── controller/    → REST API endpoints
├── service/      → Business logic
├── repository/   → Data access (JPA)
├── entity/       → Database entity
├── mapper/       → DTO conversion
└── dto/          → Request/Response objects
```

## 3. Module Dependencies / Phụ Thuộc Mô-đun

**Team Module Reference**
The vehicle module references the Team module. Ensure it exists:
```bash
src/main/java/com/floodrescue/module/team/entity/TeamEntity.java
```

## 4. Key Classes / Các Lớp Quan Trọng

### Entity / Thực thể
- **VehicleEntity** - Represents a vehicle in the database
  - `/module/vehicle/entity/VehicleEntity.java`
  - Fields: id, code, name, vehicleType, status, capacity, location, etc.
  - Relations: ManyToOne with TeamEntity

### Service / Dịch vụ
- **VehicleService** - Interface defining business operations
  - `/module/vehicle/service/VehicleService.java`
- **VehicleServiceImpl** - Service implementation
  - `/module/vehicle/service/VehicleServiceImpl.java`

### Controller / Bộ Điều Khiển
- **VehicleController** - REST API endpoints
  - `/module/vehicle/controller/VehicleController.java`
  - Base path: `/api/v1/vehicles`

### Enums / Liệt Kê
- **VehicleType** - 17 vehicle types in 4 categories
  - `/shared/enums/VehicleType.java`
- **VehicleStatus** - 5 status values (AVAILABLE, IN_USE, MAINTENANCE, BROKEN, INACTIVE)
  - `/shared/enums/VehicleStatus.java`

## 5. API Endpoints / Các Endpoint API

**Base URL**: `http://localhost:8080/api/v1/vehicles`

### Main Operations / Các Phép Toán Chính

| Operation | Method | Path | Auth |
|-----------|--------|------|------|
| Create | POST | / | MANAGER, COORDINATOR |
| List All | GET | / | All |
| Get by ID | GET | /{id} | All |
| Get by Code | GET | /code/{code} | All |
| Available | GET | /available | All |
| by Status | GET | /status/{status} | All |
| by Type | GET | /type/{type} | All |
| by Team | GET | /team/{teamId} | All |
| by Location | GET | /location/{location} | All |
| Update | PUT | /{id} | MANAGER, COORDINATOR |
| Assign Team | POST | /{id}/assign-team/{teamId} | MANAGER, COORDINATOR |
| Update Status | PATCH | /{id}/status/{status} | MANAGER, COORDINATOR |
| Update Location | PATCH | /{id}/location | MANAGER, COORDINATOR |
| Delete | DELETE | /{id} | MANAGER |
| Statistics | GET | /statistics | All |
| Count Available | GET | /count/available | All |

## 6. Sample Requests / Yêu Cầu Mẫu

### Create a Vehicle / Tạo Phương Tiện
```json
POST /api/v1/vehicles

{
  "code": "VH001",
  "name": "Xuồng máy số 1",
  "vehicleType": "MOTORBOAT",
  "capacity": 6,
  "location": "Trạm Quận 1",
  "licensePlate": "51-A-123456",
  "description": "Xuồng máy cứu hộ"
}
```

### Get Available Vehicles / Lấy Phương Tiện Có Sẵn
```
GET /api/v1/vehicles/available
```

### Update Vehicle Status / Cập Nhật Trạng Thái
```
PATCH /api/v1/vehicles/1/status/IN_USE
```

### Get Statistics / Lấy Thống Kê
```
GET /api/v1/vehicles/statistics
```

## 7. Database Tables / Bảng Dữ Liệu

### Main Table: vehicles
```sql
CREATE TABLE vehicles (
    id BIGINT PRIMARY KEY,
    code VARCHAR(30) UNIQUE,
    name VARCHAR(120),
    vehicle_type VARCHAR(50),
    status VARCHAR(20),
    capacity INT,
    location VARCHAR(255),
    license_plate VARCHAR(20),
    vin_number VARCHAR(50),
    last_maintenance_date DATETIME,
    next_maintenance_date DATETIME,
    description TEXT,
    contact_number VARCHAR(20),
    assigned_team_id BIGINT,
    created_at DATETIME,
    updated_at DATETIME,
    is_deleted BOOLEAN,
    
    FOREIGN KEY (assigned_team_id) REFERENCES teams(id)
);
```

### Indexes (Created for Performance)
- `idx_vehicles_status` - For filtering by status
- `idx_vehicles_type` - For filtering by type
- `idx_vehicles_team` - For team assignments
- `idx_vehicles_location` - For location-based queries

## 8. Security & Permissions / Bảo Mật & Quyền Hạn

**Roles Required / Vai Trò Yêu Cầu**:
- **MANAGER** - Full access
- **RESCUE_COORDINATOR** - Can create, update, assign
- **TEAM_LEAD** - Read-only
- **ADMIN** - Full access

**Authorization Annotations**:
```java
@PreAuthorize("hasAnyRole('MANAGER', 'RESCUE_COORDINATOR')")  // For write operations
@PreAuthorize("hasRole('MANAGER')")  // For delete operations
```

## 9. Vehicle Types Reference / Tham Chiếu Loại Phương Tiện

### Category 1: Rescue (Cứu người)
- MOTORBOAT - Xuồng máy
- INFLATABLE_BOAT - Thuyền cao su
- AMBULANCE - Xe cứu thương
- RESCUE_TRUCK - Xe cứu hộ
- RESCUE_HELICOPTER - Trực thăng

### Category 2: Supply (Vận chuyển cứu trợ)
- CARGO_TRUCK - Xe tải
- PICKUP_TRUCK - Xe bán tải
- MOBILE_CONTAINER - Container

### Category 3: Technical (Hỗ trợ kỹ thuật)
- EXCAVATOR - Xe múc
- WATER_PUMP - Máy bơm
- GENERATOR - Máy phát điện
- COMMAND_CENTER - Xe chỉ huy

### Category 4: Equipment (Thiết bị)
- LIFE_VEST - Áo phao
- LIFE_BUOY - Phao cứu sinh
- RADIO - Bộ đàm
- DRONE - Drone

## 10. Pagination / Phân Trang

All list endpoints support pagination:
```
GET /api/v1/vehicles?page=0&size=10&sort=createdAt,desc
```

**Query Parameters**:
- `page` - Page number (0-indexed)
- `size` - Items per page (default: 10)
- `sort` - Sort field and direction (field,asc|desc)

## 11. Error Responses / Phản Hồi Lỗi

```json
{
  "timestamp": "2025-03-05T10:30:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed",
  "errors": [
    {
      "field": "code",
      "message": "Mã phương tiện không được để trống"
    }
  ]
}
```

## 12. Testing / Kiểm Thử

### With cURL / Bằng cURL
```bash
# Create
curl -X POST http://localhost:8080/api/v1/vehicles \
  -H "Authorization: Bearer TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"code":"VH001","name":"Boat","vehicleType":"MOTORBOAT"}'

# Get all
curl http://localhost:8080/api/v1/vehicles \
  -H "Authorization: Bearer TOKEN"

# Get stats
curl http://localhost:8080/api/v1/vehicles/statistics \
  -H "Authorization: Bearer TOKEN"
```

### With Postman / Bằng Postman
1. Import OpenAPI spec (if available)
2. Set Authorization header with Bearer token
3. Test endpoints one by one
4. Use pre-defined requests

## 13. Sample Data / Dữ Liệu Mẫu

The `setup_vehicles_table.sql` includes:
- **7 rescue vehicles** (Phương tiện cứu người)
- **5 supply vehicles** (Phương tiện vận chuyển)
- **5 technical vehicles** (Phương tiện kỹ thuật)
- **4 equipment items** (Thiết bị)

Total: **25 vehicles** for testing

## 14. Integration Checklist / Danh Sách Kiểm Tra Tích Hợp

- [ ] Copy vehicle module code to `/src/main/java/com/floodrescue/module/vehicle/`
- [ ] Copy enums to `/src/main/java/com/floodrescue/shared/enums/`
- [ ] Run database setup (Flyway or manual SQL)
- [ ] Verify dependencies in pom.xml
- [ ] Test API endpoints
- [ ] Configure authorization roles
- [ ] Add vehicle module to API documentation
- [ ] Test with sample data
- [ ] Verify soft delete functionality
- [ ] Test pagination
- [ ] Test search/filter endpoints
- [ ] Verify team assignments
- [ ] Test statistics endpoint
- [ ] Review security constraints

## 15. Common Issues & Solutions / Vấn Đề Thường Gặp & Giải Pháp

**Issue**: "Table 'vehicles' doesn't exist"
**Solution**: Run the SQL setup script or restart the application to trigger Flyway migration

**Issue**: "Could not find the main entity class" 
**Solution**: Ensure VehicleEntity.java is in the correct package

**Issue**: "Foreign key constraint fails"
**Solution**: Ensure the team exists in the teams table before assigning a vehicle

**Issue**: "No vehicles appear"
**Solution**: Check that is_deleted = false in the database

**Issue**: "Unauthorized" error
**Solution**: Ensure JWT token is valid and user has MANAGER or RESCUE_COORDINATOR role

## 16. Files to Copy / Tệp Tin Cần Sao Chép

**Source Code**:
```
src/main/java/com/floodrescue/module/vehicle/
├── controller/VehicleController.java
├── service/VehicleService.java
├── service/VehicleServiceImpl.java
├── repository/VehicleRepository.java
├── entity/VehicleEntity.java
├── mapper/VehicleMapper.java
└── dto/
    ├── request/CreateVehicleRequest.java
    ├── request/UpdateVehicleRequest.java
    ├── response/VehicleResponse.java
    ├── response/VehicleStatisticsResponse.java
    └── VehicleDTO.java
```

**Enums**:
```
src/main/java/com/floodrescue/shared/enums/
├── VehicleType.java
└── VehicleStatus.java
```

**Database**:
```
setup_vehicles_table.sql  (or use Flyway migration)
src/main/resources/db/migration/V1__create_vehicle_tables.sql
```

**Documentation**:
```
VEHICLE_API_GUIDE.md
VEHICLE_MODULE_README.md
VEHICLE_INTEGRATION_GUIDE.md (this file)
```

## 17. Next Steps / Bước Tiếp Theo

1. **Test the API** - Use cURL or Postman to test endpoints
2. **Review Logs** - Check application logs for any issues
3. **Verify Data** - Query the database to verify data is saved
4. **Integrate with UI** - Build frontend to consume the API
5. **Add Notifications** - Implement notifications for status changes
6. **Monitor Usage** - Track which vehicles are used most frequently
7. **Plan Maintenance** - Use statistics to plan maintenance schedules

## 18. Support Resources / Tài Nguyên Hỗ Trợ

- **API Documentation**: See `VEHICLE_API_GUIDE.md`
- **Module README**: See `VEHICLE_MODULE_README.md`
- **Database Setup**: See `setup_vehicles_table.sql`
- **Code Comments**: All classes have JavaDoc comments

## 19. Contact / Liên Hệ

For questions or issues:
1. Check documentation files
2. Review code comments
3. Check database logs
4. Contact development team

---

**Ready to use!** The vehicle management module is complete and ready to integrate.

**Sẵn sàng sử dụng!** Mô-đun quản lý phương tiện hoàn thành và sẵn sàng tích hợp.
