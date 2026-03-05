# Postman Testing Guide

## Setup

1. Tải Postman: https://www.postman.com/downloads/
2. Tạo **Environment** với biến: `base_url = http://localhost:8080`
3. Vào **Settings → General**: bật **"Automatically follow redirects"**
4. Trong Collection: bật **Cookie Jar** để lưu session cookie

---

## Luồng test đầy đủ (Happy Path)

```
1. Login                          → 200 (lấy session cookie)
2. Tạo rescue request             → 201
3. Xem team rảnh                  → thấy 3 team
4. Gán team vào rescue request    → 201 (team bận)
5. Xem team rảnh                  → thấy 2 team (1 đã bận)
6. Update → in_progress           → 200
7. Xem team busy                  → thấy team đó
8. Update → completed             → 200 (team giải phóng)
9. Xem team rảnh                  → thấy lại 3 team
10. Logout                        → 200
```

---

## Chi tiết từng Test Case

### TC-01: Đăng nhập thành công
- **POST** `{{base_url}}/api/auth/login`
- Body: `{ "email": "admin@rescue.vn", "password": "123456" }`
- ✅ Expected: `200`, `success: true`, có `data.email`

### TC-02: Đăng nhập khi đã có session (Duplicate)
- Giữ nguyên session, gửi lại request login
- ✅ Expected: `409 Conflict`, `success: false`

### TC-03: Sai mật khẩu
- Body: `{ "email": "admin@rescue.vn", "password": "wrong" }`
- ✅ Expected: `401 Unauthorized`

### TC-04: Tạo Rescue Request
- **POST** `{{base_url}}/api/rescue-requests`
- Body:
```json
{
  "description": "Cần hỗ trợ khẩn cấp tại khu vực Quận 3",
  "addressText": "456 Đường Lê Lợi, Quận 3, TP.HCM",
  "priority": "high",
  "affectedPeopleCount": 8
}
```
- ✅ Expected: `201 Created`, `data.code = "RR-2026-XXXX"`, `data.status = "pending"`

### TC-05: Tạo Rescue Request lần 2 (Duplicate)
- Gửi lại cùng POST (không logout, cùng caller)
- ✅ Expected: `409 Conflict`, message chứa "đang có một yêu cầu cứu hộ chờ xử lý"

### TC-06: Lấy danh sách team rảnh
- **GET** `{{base_url}}/api/teams/available`
- ✅ Expected: `200`, `data` là array, có 3 teams (T-001, T-002, T-004)

### TC-07: Gán team vào rescue request
- **POST** `{{base_url}}/api/assignments`
- Body: `{ "rescueRequestId": 2, "taskGroupId": 1, "assetId": 1 }`
- ✅ Expected: `201 Created`, `data.taskGroupCode = "TG-2026-0001"`, `data.teamName = "Đội Cứu Hộ Alpha"`

### TC-08: Gán team đang bận (lần 2)
- Gán lại cùng taskGroupId=1 vào request mới
- ✅ Expected: `409 Conflict`, message chứa "đang thực hiện nhiệm vụ khác"

### TC-09: Cập nhật → In Progress
- **PUT** `{{base_url}}/api/assignments/1/status`
- Body: `{ "status": "in_progress" }`
- ✅ Expected: `200 OK`
- Kiểm tra thêm: **GET** `/api/teams/busy` → thấy Team Alpha trong danh sách bận

### TC-10: Cập nhật → Completed (giải phóng team)
- **PUT** `{{base_url}}/api/assignments/1/status`
- Body: `{ "status": "completed" }`
- ✅ Expected: `200 OK`
- Kiểm tra thêm: **GET** `/api/teams/available` → Team Alpha xuất hiện lại trong danh sách rảnh

### TC-11: Chuyển status không hợp lệ
- **PUT** `{{base_url}}/api/assignments/1/status`
- Body: `{ "status": "assigned" }` ← không hợp lệ theo luồng
- ✅ Expected: `400 Bad Request`, message chứa "Không thể chuyển"

### TC-12: Gọi API khi chưa đăng nhập
- **GET** `{{base_url}}/api/rescue-requests` (không có cookie)
- ✅ Expected: `401 Unauthorized`, `success: false`

### TC-13: Đăng xuất
- **POST** `{{base_url}}/api/auth/logout`
- ✅ Expected: `200 OK`, `message: "Đăng xuất thành công"`
- Kiểm tra: **GET** `/api/auth/me` → `401 Unauthorized`

---

## Tài khoản mẫu

| Email | Password | Role |
|-------|----------|------|
| admin@rescue.vn | 123456 | admin |
| dispatcher@rescue.vn | 123456 | dispatcher |
| alpha@rescue.vn | 123456 | team_leader (Team Alpha) |
| citizen1@gmail.com | 123456 | citizen |
| citizen2@gmail.com | 123456 | citizen |
