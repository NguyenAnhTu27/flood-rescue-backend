# API Connection Guide

## Base URL
```
http://localhost:8080
```

## ⚠️ Lưu ý Session
- Hệ thống dùng **HTTP Session Cookie** (`RESCUE_SESSION`).
- Sau khi login, cookie được tự động gửi kèm mọi request.
- Khi dùng Postman: bật **Cookie Manager** hoặc dùng tab Cookies.
- Khi dùng Frontend (React/Vue): cấu hình `credentials: 'include'`.

---

## 1. Đăng nhập

**POST** `/api/auth/login`

```json
// Request
{ "email": "admin@rescue.vn", "password": "123456" }

// 200 OK – Thành công
{
  "success": true,
  "message": "Đăng nhập thành công",
  "data": { "id": 1, "fullName": "Nguyễn Quản Trị", "email": "admin@rescue.vn", "role": "admin" }
}

// 401 – Sai thông tin
{ "success": false, "message": "Email hoặc mật khẩu không đúng.", "data": null }

// 409 – Đã đăng nhập (duplicate session)
{ "success": false, "message": "Tài khoản đã đăng nhập. Vui lòng đăng xuất trước.", "data": null }
```

---

## 2. Tạo Rescue Request

**POST** `/api/rescue-requests` *(cần đăng nhập)*

```json
// Request
{
  "description": "Lũ lụt, cần hỗ trợ khẩn cấp",
  "addressText": "123 Nguyễn Văn Linh, Quận 3, HCM",
  "priority": "high",
  "affectedPeopleCount": 10
}
// priority: low | medium | high | critical (default: medium)

// 201 – Tạo thành công
{ "success": true, "message": "Tạo yêu cầu cứu hộ thành công",
  "data": { "id": 2, "code": "RR-2026-0002", "status": "pending", ... } }

// 409 – Đã có pending request
{ "success": false, "message": "Bạn đang có một yêu cầu cứu hộ chờ xử lý...", "data": null }
```

---

## 3. Lấy danh sách Rescue Request

**GET** `/api/rescue-requests` → Tất cả

**GET** `/api/rescue-requests?status=pending` → Filter theo status

**GET** `/api/rescue-requests/{id}` → Chi tiết

---

## 4. Team Rảnh / Bận

**GET** `/api/teams/available`
```json
{ "success": true, "message": "Danh sách team đang rảnh",
  "data": [{ "id": 1, "code": "T-001", "name": "Đội Cứu Hộ Alpha", "status": "active" }] }
```

**GET** `/api/teams/busy`

**GET** `/api/teams/{id}/availability`
```json
{ "success": true, "data": { "teamId": 1, "availability": "available" } }
// availability: "available" | "busy" | "inactive"
```

---

## 5. Gán Team vào Rescue Request

**POST** `/api/assignments`

```json
// Request
{ "rescueRequestId": 2, "taskGroupId": 1, "assetId": 1 }
// assetId là optional

// 201 – Gán thành công
{ "success": true, "message": "Gán team thành công",
  "data": { "id": 1, "taskGroupCode": "TG-2026-0001", "teamName": "Đội Cứu Hộ Alpha", ... } }

// 409 – Team đang bận
{ "success": false, "message": "Team [...] đang thực hiện nhiệm vụ khác, không thể gán.", "data": null }

// 400 – Request không 'pending' hoặc TaskGroup không 'idle'
{ "success": false, "message": "Chỉ có thể gán team khi request đang 'pending'...", "data": null }
```

---

## 6. Update Status — In Progress / Completed

**PUT** `/api/assignments/{id}/status`

```json
// Chuyển sang In Progress (team bận, rescue_request cũng in_progress)
{ "status": "in_progress" }

// Chuyển sang Completed (team được GIẢI PHÓNG, rescue_request completed)
{ "status": "completed" }

// Hủy (team về idle)
{ "status": "cancelled" }
```

**Luồng hợp lệ:**
```
assigned → in_progress → completed
                       → cancelled
```

Ngoài ra còn có:
**PUT** `/api/rescue-requests/{id}/status`
```json
{ "status": "in_progress" }   // pending → in_progress
{ "status": "completed" }     // in_progress → completed
{ "status": "cancelled" }     // any → cancelled
```

---

## 7. Đăng xuất

**POST** `/api/auth/logout`
```json
{ "success": true, "message": "Đăng xuất thành công", "data": null }
```

---

## Response Format chuẩn

```json
{
  "success": true | false,
  "message": "...",
  "data": { ... } | null
}
```

| HTTP Code | Ý nghĩa |
|-----------|---------|
| 200 | OK |
| 201 | Created |
| 400 | Bad Request (validation / sai luồng) |
| 401 | Unauthorized (chưa login) |
| 404 | Not Found |
| 409 | Conflict (duplicate session / duplicate request / team bận) |
| 500 | Server Error |
