# Vehicle Management Module
# Mô-đun Quản Lý Phương Tiện Cứu Hộ

## Overview / Tổng Quan

This module provides comprehensive vehicle management for the Flood Rescue Coordination and Relief Management System. It allows Managers and Rescue Coordinators to register, track, distribute, and coordinate rescue and relief vehicles.

Mô-đun này cung cấp quản lý toàn diện các phương tiện cứu hộ cho Hệ Thống Điều Phối Cứu Hộ Lũ Lụt. Cho phép các Quản lý viên và Điều phối viên cứu hộ đăng ký, theo dõi, phân phối và điều phối các phương tiện cứu hộ và cứu trợ.

## Module Structure / Cấu Trúc Mô-đun

```
src/main/java/com/floodrescue/module/vehicle/
├── controller/
│   └── VehicleController.java       # REST API endpoints
├── service/
│   ├── VehicleService.java          # Service interface
│   └── VehicleServiceImpl.java       # Service implementation
├── repository/
│   └── VehicleRepository.java       # JPA repository
├── entity/
│   └── VehicleEntity.java           # JPA entity
├── mapper/
│   └── VehicleMapper.java           # DTO mapper
└── dto/
    ├── request/
    │   ├── CreateVehicleRequest.java
    │   └── UpdateVehicleRequest.java
    ├── response/
    │   ├── VehicleResponse.java
    │   └── VehicleStatisticsResponse.java
    └── VehicleDTO.java              # Combined DTO

src/main/java/com/floodrescue/shared/enums/
├── VehicleType.java                 # Vehicle type enum
└── VehicleStatus.java               # Vehicle status enum

Database/
├── src/main/resources/db/migration/
│   └── V1__create_vehicle_tables.sql # Flyway migration
└── setup_vehicles_table.sql         # Direct SQL setup (root)
```

## Features / Tính Năng

### Vehicle Management
- ✅ Create new vehicles (Tạo phương tiện mới)
- ✅ Update vehicle information (Cập nhật thông tin)
- ✅ Delete vehicles - soft delete (Xóa phương tiện - xóa mềm)
- ✅ Track vehicle status (Theo dõi trạng thái)
- ✅ Manage vehicle locations (Quản lý vị trí)
- ✅ Track maintenance schedules (Theo dõi lịch bảo dưỡng)
- ✅ Assign vehicles to teams (Gán phương tiện cho đội)

### Vehicle Types / Loại Phương Tiện
Supports 4 categories with 17 vehicle types:

1. **Rescue Vehicles (Phương tiện cứu người)**
   - Motorboats (Xuồng máy)
   - Inflatable boats (Thuyền cao su)
   - Ambulances (Xe cứu thương)
   - Rescue trucks (Xe cứu hộ)
   - Rescue helicopters (Trực thăng)

2. **Supply Transport (Phương tiện vận chuyển cứu trợ)**
   - Cargo trucks (Xe tải)
   - Pickup trucks (Xe bán tải)
   - Mobile containers (Container)

3. **Technical Support (Phương tiện hỗ trợ kỹ thuật)**
   - Excavators (Xe múc)
   - Water pumps (Máy bơm)
   - Generators (Máy phát điện)
   - Command centers (Xe chỉ huy)

4. **Equipment (Thiết bị)**
   - Life vests (Áo phao)
   - Life buoys (Phao cứu sinh)
   - Radios (Bộ đàm)
   - Drones (Drone)

### Search & Filter / Tìm Kiếm & Lọc
- By ID or code (Theo ID hoặc mã)
- By status (Có sẵn, Đang dùng, Bảo dưỡng)
- By type (Theo loại phương tiện)
- By team assignment (Theo đội được gán)
- By location (Theo địa điểm)
- By type + status combination (Loại + Trạng thái)

### Statistics / Thống Kê
- Total vehicles count
- Available vehicles count
- In-use vehicles count
- Maintenance vehicles count
- Group by type with counts
- Group by status with counts

### Pagination / Phân Trang
- Configurable page size
- Default: 10 items per page
- Sortable by any field

## Installation & Setup / Cài Đặt & Thiết Lập

### 1. Database Setup / Cài Đặt Cơ Sở Dữ Liệu

**Option A: Using Flyway (Automated)**
```bash
# Just run the application - Flyway will create tables
mvn spring-boot:run
# Or
java -jar flood-rescue-backend.jar
```

**Option B: Manual SQL Setup**
```bash
# Connect to MySQL
mysql -u root -p flood_rescue_db

# Run the setup script
source setup_vehicles_table.sql;

# Verify
SHOW TABLES;
SELECT * FROM vehicles;
```

### 2. Verify Dependencies / Kiểm Tra Phụ Thuộc

Check that your `pom.xml` has:
```xml
<!-- Spring Boot Web -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-web</artifactId>
</dependency>

<!-- Spring Data JPA -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-jpa</artifactId>
</dependency>

<!-- MySQL Driver -->
<dependency>
    <groupId>com.mysql</groupId>
    <artifactId>mysql-connector-java</artifactId>
    <version>8.0.33</version>
</dependency>

<!-- Lombok -->
<dependency>
    <groupId>org.projectlombok</groupId>
    <artifactId>lombok</artifactId>
    <optional>true</optional>
</dependency>

<!-- Spring Security -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-security</artifactId>
</dependency>

<!-- Spring Validation -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-validation</artifactId>
</dependency>
```

### 3. Configuration / Cấu Hình

Add to `application.properties`:
```properties
# Vehicle module configuration
vehicle.default-status=AVAILABLE
vehicle.pagination-page-size=10
vehicle.enable-soft-delete=true
```

### 4. Verify Teams Table / Kiểm Tra Bảng Teams

The vehicle module references the `teams` table. Ensure it exists:
```sql
SELECT * FROM teams LIMIT 1;
```

## API Usage / Sử Dụng API

### Get All Vehicles / Lấy Tất Cả Phương Tiện
```bash
curl -X GET http://localhost:8080/api/v1/vehicles \
  -H "Authorization: Bearer YOUR_TOKEN"
```

### Create New Vehicle / Tạo Phương Tiện Mới
```bash
curl -X POST http://localhost:8080/api/v1/vehicles \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "code": "VH001",
    "name": "Xuồng máy số 1",
    "vehicleType": "MOTORBOAT",
    "capacity": 6,
    "location": "Trạm Quận 1"
  }'
```

### Get Available Vehicles / Lấy Phương Tiện Có Sẵn
```bash
curl -X GET http://localhost:8080/api/v1/vehicles/available \
  -H "Authorization: Bearer YOUR_TOKEN"
```

### Get Vehicles by Type / Lấy Phương Tiện Theo Loại
```bash
curl -X GET http://localhost:8080/api/v1/vehicles/type/MOTORBOAT \
  -H "Authorization: Bearer YOUR_TOKEN"
```

### Update Vehicle Status / Cập Nhật Trạng Thái
```bash
curl -X PATCH http://localhost:8080/api/v1/vehicles/1/status/IN_USE \
  -H "Authorization: Bearer YOUR_TOKEN"
```

### Assign Vehicle to Team / Gán Phương Tiện Cho Đội
```bash
curl -X POST http://localhost:8080/api/v1/vehicles/1/assign-team/1 \
  -H "Authorization: Bearer YOUR_TOKEN"
```

### Get Statistics / Lấy Thống Kê
```bash
curl -X GET http://localhost:8080/api/v1/vehicles/statistics \
  -H "Authorization: Bearer YOUR_TOKEN"
```

## Roles & Permissions / Vai Trò & Quyền Hạn

| Operation | MANAGER | RESCUE_COORDINATOR | TEAM_LEAD | ADMIN |
|-----------|---------|-------------------|-----------|-------|
| Create | ✅ | ✅ | ❌ | ✅ |
| Read | ✅ | ✅ | ✅ | ✅ |
| Update | ✅ | ✅ | ❌ | ✅ |
| Assign to Team | ✅ | ✅ | ❌ | ✅ |
| Delete | ✅ | ❌ | ❌ | ✅ |
| View Stats | ✅ | ✅ | ✅ | ✅ |

## Integration with Other Modules / Tích Hợp Với Các Mô-đun Khác

### Team Module / Mô-đun Đội
- Vehicles can be assigned to teams
- Delete team cascades to clear vehicle assignments
- View team vehicles via `/team/{teamId}`

### Admin Module / Mô-đun Quản Trị
- Admins can send notifications about vehicle status
- Track vehicle usage in audit logs

### Rescue Module / Mô-đun Cứu Hộ
- Rescue operations can request available vehicles by type
- Track which vehicles are assigned to rescue tasks

### Relief Module / Mô-đun Cứu Trợ
- Relief operations can request cargo/supply vehicles
- Track supply distribution by vehicle

## Queries / Truy Vấn

### View: Available Vehicles
```sql
SELECT * FROM v_available_vehicles;
```

### View: Vehicle Count by Type
```sql
SELECT * FROM v_vehicle_count_by_type;
```

### View: Vehicle Count by Status
```sql
SELECT * FROM v_vehicle_count_by_status;
```

### Check Vehicles Needing Maintenance
```sql
SELECT * FROM vehicles 
WHERE next_maintenance_date <= NOW() 
  AND is_deleted = FALSE;
```

## Stored Procedures / Thủ Tục Lưu Trữ

### Check Maintenance Due
```sql
CALL sp_check_maintenance_due();
```
Automatically updates vehicle status to MAINTENANCE if next_maintenance_date is reached.

## Error Handling / Xử Lý Lỗi

All endpoints return proper HTTP status codes and error messages:

- **400 Bad Request** - Invalid input data
- **401 Unauthorized** - Missing or invalid token
- **403 Forbidden** - Insufficient permissions
- **404 Not Found** - Resource not found
- **500 Server Error** - Internal server error

## Testing / Kiểm Thử

### Sample Test Data / Dữ Liệu Kiểm Thử
The `setup_vehicles_table.sql` includes 25 sample vehicles:
- 7 rescue vehicles (Phương tiện cứu người)
- 5 supply vehicles (Phương tiện vận chuyển)
- 5 technical support vehicles (Phương tiện kỹ thuật)
- 4 equipment items (Thiết bị)

### Test with Postman / Kiểm Thử Với Postman
Import the endpoints into Postman using the OpenAPI integration, or manually test each endpoint.

## Performance Considerations / Cân Nhắc Hiệu Năng

- Indexes on: status, vehicle_type, assigned_team_id, location
- Pagination enabled to prevent loading all vehicles
- Soft delete to maintain data integrity
- Lazy loading on team associations

## Troubleshooting / Khắc Phục Sự Cố

**Issue: No vehicles appear**
- Check that `is_deleted = false` in database
- Verify team associations exist
- Check pagination parameters

**Issue: Cannot assign vehicle to team**
- Ensure team exists in database
- Check role permissions (MANAGER, RESCUE_COORDINATOR)
- Verify team ID is correct

**Issue: Status update fails**
- Check that vehicle exists
- Verify valid status value
- Check role permissions

## Future Enhancements / Cải Tiến Tương Lai

- [ ] GPS tracking integration (Tích hợp theo dõi GPS)
- [ ] Fuel/battery level monitoring (Giám sát nhiên liệu/pin)
- [ ] Maintenance reminder notifications (Thông báo nhắc bảo dưỡng)
- [ ] Usage history & reports (Lịch sử sử dụng & báo cáo)
- [ ] Cost tracking & budget management (Theo dõi chi phí)
- [ ] Vehicle condition assessment form (Biểu mẫu đánh giá tình trạng)
- [ ] Scheduling & reservation system (Hệ thống lên lịch & đặt chỗ)

## Files Reference / Tham Chiếu Tệp Tin

**Source Code** (`src/main/java/com/floodrescue/module/vehicle/`)
- `VehicleController.java` - REST endpoints
- `VehicleService.java` / `VehicleServiceImpl.java` - Business logic
- `VehicleRepository.java` - Data access
- `VehicleEntity.java` - Database entity
- `VehicleMapper.java` - DTO mapping

**Enums** (`src/main/java/com/floodrescue/shared/enums/`)
- `VehicleType.java` - 17 vehicle types in 4 categories
- `VehicleStatus.java` - 5 status values

**DTOs** (`src/main/java/com/floodrescue/module/vehicle/dto/`)
- Request: CreateVehicleRequest, UpdateVehicleRequest
- Response: VehicleResponse, VehicleStatisticsResponse

**Database** (Root directory)
- `setup_vehicles_table.sql` - Quick start SQL script
- `src/main/resources/db/migration/V1__create_vehicle_tables.sql` - Flyway migration

**Documentation**
- `VEHICLE_API_GUIDE.md` - Complete API documentation
- `VEHICLE_MODULE_README.md` - This file

## Support & Contact / Hỗ Trợ & Liên Hệ

For questions or issues:
1. Check the API guide: `VEHICLE_API_GUIDE.md`
2. Review the sample database: `setup_vehicles_table.sql`
3. Check the logs for error messages
4. Contact the development team

---

**Last Updated**: March 5, 2025
**Module Version**: 1.0.0
**Database Version**: 1.0
