# Rescue Management System

## Giới thiệu
Backend REST API cho hệ thống quản lý cứu hộ, xây dựng bằng **Java 21 + Spring Boot 3.4.3** theo mô hình **MVC + DTO**.

## Công nghệ
| | |
|--|--|
| Java | 21 (Virtual Threads) |
| Spring Boot | 3.4.3 |
| ORM | Spring Data JPA + Hibernate |
| Database | MySQL 8 |
| Build | Maven (mvnw) |
| DTOs | Java 21 Records |

## Cấu trúc dự án
```
src/main/java/com/swp391/rescuemanagement/
├── controller/     ← REST endpoints
├── service/        ← Business logic
├── repository/     ← JPA queries
├── model/          ← JPA Entities
├── dto/
│   ├── request/    ← LoginRequest, CreateRescueRequestRequest ...
│   └── response/   ← ApiResponse, UserResponse, TeamResponse ...
├── exception/      ← BusinessException, GlobalExceptionHandler
└── config/         ← WebConfig (CORS)

src/main/resources/db/
├── schema.sql      ← 17 bảng MySQL
└── data.sql        ← Dữ liệu mẫu
```

## Cài đặt & Chạy

### Bước 1 — Tạo Database
```sql
-- Chạy trong MySQL Workbench hoặc CLI
source src/main/resources/db/schema.sql
source src/main/resources/db/data.sql
```

### Bước 2 — Cấu hình
Sửa `src/main/resources/application.properties`:
```properties
spring.datasource.password=YOUR_MYSQL_PASSWORD
```

### Bước 3 — Chạy ứng dụng
```cmd
mvnw.cmd spring-boot:run
```
Server: `http://localhost:8080`

## API Endpoints

| Method | Endpoint | Mô tả |
|--------|----------|-------|
| POST | `/api/auth/login` | Đăng nhập (Session) |
| POST | `/api/auth/logout` | Đăng xuất |
| GET  | `/api/auth/me` | User hiện tại |
| POST | `/api/rescue-requests` | Tạo yêu cầu cứu hộ |
| GET  | `/api/rescue-requests` | Danh sách (filter: `?status=pending`) |
| GET  | `/api/rescue-requests/{id}` | Chi tiết |
| PUT  | `/api/rescue-requests/{id}/status` | Cập nhật status |
| GET  | `/api/teams/available` | Team đang rảnh |
| GET  | `/api/teams/busy` | Team đang bận |
| GET  | `/api/teams/{id}/availability` | Kiểm tra 1 team |
| POST | `/api/assignments` | Gán team → rescue request |
| PUT  | `/api/assignments/{id}/status` | In Progress / Completed |

## Tài khoản mẫu
| Email | Password | Role |
|-------|----------|------|
| admin@rescue.vn | 123456 | admin |
| dispatcher@rescue.vn | 123456 | dispatcher |
| alpha@rescue.vn | 123456 | team_leader |
| citizen1@gmail.com | 123456 | citizen |
