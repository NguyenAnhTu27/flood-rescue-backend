# ADMIN DASHBOARD - TECHNICAL TESTING GUIDE (Trieu)

Cập nhật theo code hiện tại ngày 2026-03-06.

## 1. Mục tiêu tài liệu
Tài liệu này để dev mới vào dự án có thể hiểu nhanh:
1. Trang admin làm được gì.
2. Bấm nút nào gọi API nào.
3. API ghi/đọc bảng nào trong database.
4. Test Postman thế nào để verify end-to-end.

## 2. Phạm vi trang Admin đang active
Các route admin thực sự đang dùng:
1. `/admin` - Dashboard.
2. `/admin/nguoi-dung` - Quản lý người dùng.
3. `/admin/phan-quyen` - Vai trò & Phân quyền.
4. `/admin/danh-muc-he-thong` - Danh mục hệ thống.
5. `/admin/mau-thong-bao` - Mẫu thông báo.
6. `/admin/cau-hinh-he-thong` - Cấu hình hệ thống.
7. `/admin/nhat-ky-he-thong` - Nhật ký hệ thống.

Lưu ý: `AdminTeamAssetPage.jsx` là file cũ, không nằm trong route active.

## 3. Tổng quan kiến trúc Admin
1. Frontend admin gọi trực tiếp backend qua `fetch` với base `http://localhost:8080/api/admin`.
2. Backend admin nằm ở package `com.floodrescue.module.admin`.
3. Toàn bộ `/api/admin/**` yêu cầu role `ADMIN` (`@PreAuthorize("hasRole('ADMIN')")`).
4. Bảo trì hệ thống có filter runtime:
- Nếu `maintenanceMode=true`, non-admin bị chặn phần lớn API với HTTP `503`.
- `/api/auth/**` và `/api/public/**` vẫn cho qua.

## 4. Database role mapping chuẩn

| role_id | code | name |
|---:|---|---|
| 1 | CITIZEN | Công dân |
| 2 | COORDINATOR | Điều phối cứu hộ |
| 3 | RESCUER | Đội cứu hộ |
| 4 | MANAGER | Quản lý |
| 5 | ADMIN | Quản trị hệ thống |

## 5. Bản đồ chức năng theo từng trang Admin

## 5.1 Dashboard `/admin`
Mục đích:
1. Hiển thị tổng quan user (`totalUsers`, `activeUsers`, `lockedUsers`).
2. Điều hướng sang các module admin khác.

API gọi:
1. `GET /api/admin/stats`.

DB tác động:
1. Chỉ đọc bảng `users` (đếm theo `status`).

Không có thao tác ghi DB tại trang này.

## 5.2 Quản lý người dùng `/admin/nguoi-dung`
Mục đích:
1. CRUD user admin tạo.
2. Đổi trạng thái active/locked.
3. Reset mật khẩu.
4. Xem danh sách đội để gán (`/api/admin/teams`).

Chức năng chính:
1. Tạo user mới.
2. Tìm user theo keyword + role + page.
3. Mở popup chi tiết để sửa thông tin.
4. Đổi trạng thái `ACTIVE/LOCKED`.
5. Reset password.
6. Xóa user có confirm.

Validation tạo user:
1. Frontend validate trước khi gửi.
2. Backend validate cứng DTO (`@Valid`) với rule:
- fullName: 2-120 ký tự.
- email: đúng định dạng.
- phone: `0[3-9]xxxxxxxx` hoặc `+84[3-9]xxxxxxxx`.
- password: 8-72 ký tự, có hoa/thường/số/ký tự đặc biệt.

Rule đăng nhập liên quan trạng thái user:
1. `status=0` là khóa vĩnh viễn (chỉ admin mở mới đăng nhập lại).
2. Khóa tạm do nhập sai dùng cột `temp_locked_until`.
3. Hết thời gian khóa tạm được thử lại; sai tiếp sẽ khóa tạm lại theo config.

API gọi và ghi DB:
1. `POST /api/admin/create-user`
- Ghi `users`: `full_name,email,phone,password_hash,role_id,status,failed_login_attempts,locked_at,temp_locked_until,created_at,updated_at`.
- Ghi `audit_logs`: action `CREATE_USER`.
2. `GET /api/admin/users`
- Đọc `users` join `roles`.
3. `PUT /api/admin/users/{id}`
- Update `users`: profile + role + team + status.
- Nếu status chuyển active thì reset lock fields.
- Ghi `audit_logs`: `UPDATE_USER`.
4. `PUT /api/admin/users/{id}/status`
- Update `users.status`.
- Nếu active thì reset `failed_login_attempts`, `locked_at`, `temp_locked_until`.
- Ghi `audit_logs`: `UPDATE_STATUS`.
5. `PUT /api/admin/users/{id}/reset-password`
- Update `users.password_hash`, `updated_at`.
- Ghi `audit_logs`: `RESET_PASSWORD`.
6. `DELETE /api/admin/users/{id}`
- Xóa phụ thuộc `audit_logs` theo `actor_id` trước.
- Xóa dòng `users`.
- Ghi `audit_logs`: `DELETE_USER`.
7. `GET /api/admin/stats`
- Đếm từ `users`.
8. `GET /api/admin/teams`
- Đọc bảng `teams`.

## 5.3 Vai trò & Phân quyền `/admin/phan-quyen`
Mục đích:
1. Quản lý quyền thao tác theo role.
2. Xem số user mỗi role.

Chức năng chính:
1. Load ma trận roles + permissions + mapping.
2. Tick/bỏ tick permission theo role.
3. Lưu mapping mới.
4. Xem tab người dùng theo role.

API gọi và ghi DB:
1. `GET /api/admin/permissions`
- Đọc `roles`, `permissions`, `role_permissions`, `users` (đếm theo role).
2. `GET /api/admin/roles/{roleCode}/permissions`
- Đọc `role_permissions` theo role.
3. `PUT /api/admin/roles/{roleCode}/permissions`
- Xóa toàn bộ mapping cũ role đó trong `role_permissions`.
- Insert mapping mới vào `role_permissions`.
- Ghi `audit_logs`: `UPDATE_ROLE_PERMISSIONS`.
4. `GET /api/admin/users?roleId=...`
- Đọc user theo role cho tab Người dùng.

## 5.4 Danh mục hệ thống `/admin/danh-muc-he-thong`
Mục đích:
1. Quản lý danh mục dạng group/status dùng chung toàn hệ thống.
2. Có tab theo từng group.
3. Có thao tác thêm/sửa/xóa status, đổi tên/xóa group.

DB model:
1. Bảng chính: `admin_catalogs`.
2. Group metadata dùng mã đặc biệt `code='__GROUP__'` trong cùng bảng.

Chức năng chính:
1. Tải toàn bộ catalog + group.
2. Thêm trạng thái vào group.
3. Sửa trạng thái.
4. Bật/tắt active trạng thái.
5. Xóa trạng thái.
6. Thêm danh mục mới (tạo row metadata group).
7. Đổi tên danh mục.
8. Xóa cả danh mục (xóa theo `group_code`).

API gọi và ghi DB:
1. `GET /api/admin/catalogs`
- Đọc `admin_catalogs` theo filter.
2. `GET /api/admin/catalog-groups`
- Tổng hợp từ `admin_catalogs`.
3. `POST /api/admin/catalogs`
- Insert `admin_catalogs`.
- Ghi `audit_logs`: `CREATE_CATALOG`.
4. `PUT /api/admin/catalogs/{id}`
- Update `admin_catalogs`.
- Ghi `audit_logs`: `UPDATE_CATALOG`.
5. `PATCH /api/admin/catalogs/{id}/active`
- Toggle `admin_catalogs.active`.
- Ghi `audit_logs`: `TOGGLE_CATALOG_ACTIVE`.
6. `DELETE /api/admin/catalogs/{id}`
- Delete 1 row `admin_catalogs`.
- Ghi `audit_logs`: `DELETE_CATALOG`.
7. `PUT /api/admin/catalog-groups/{groupCode}`
- Upsert metadata row `__GROUP__` trong `admin_catalogs`.
- Ghi `audit_logs`: `RENAME_CATALOG_GROUP`.
8. `DELETE /api/admin/catalog-groups/{groupCode}`
- Delete all rows theo `group_code` trong `admin_catalogs`.
- Ghi `audit_logs`: `DELETE_CATALOG_GROUP`.

## 5.5 Mẫu thông báo `/admin/mau-thong-bao`
Mục đích:
1. Quản lý template thông báo theo sự kiện.
2. Search/pagination/stats.
3. Tạo/sửa/bật-tắt/xóa template.

Chức năng chính:
1. Search theo code/templateKey/title/content.
2. Tạo template mới.
3. Sửa title/channel/content/active.
4. Toggle active nhanh trên bảng.
5. Xóa template trong popup edit.

API gọi và ghi DB:
1. `GET /api/admin/notification-templates?keyword=&page=&size=`
- Đọc `notification_templates`.
- Trả `items`, phân trang và `stats`.
2. `POST /api/admin/notification-templates`
- Insert `notification_templates`.
- Ghi `audit_logs`: `CREATE_NOTIFICATION_TEMPLATE`.
3. `PUT /api/admin/notification-templates/{id}`
- Update `notification_templates`.
- Đồng bộ `active` và `is_active`.
- Ghi `audit_logs`: `UPDATE_NOTIFICATION_TEMPLATE`.
4. `PATCH /api/admin/notification-templates/{id}/active`
- Toggle active + is_active.
- Ghi `audit_logs`: `TOGGLE_NOTIFICATION_TEMPLATE`.
5. `DELETE /api/admin/notification-templates/{id}`
- Nếu có ràng buộc, xóa `notification_rules` theo `template_id` trước.
- Xóa `notification_templates`.
- Ghi `audit_logs`: `DELETE_NOTIFICATION_TEMPLATE`.

## 5.6 Cấu hình hệ thống `/admin/cau-hinh-he-thong`
Mục đích:
1. Quản lý tham số runtime của hệ thống.
2. Cấu hình này hiện đã có tác dụng thật vào auth/rescue/dashboard.

Key cấu hình đang dùng:
1. `rescueSlaMinutes`.
2. `maxOpenRequestPerCitizen`.
3. `autoLockAfterFailedLogin`.
4. `failedLoginLockMinutes`.
5. `maintenanceMode`.
6. `mapRefreshSeconds`.
7. `hotline`.
8. `footerBrandName`.
9. `footerDescription`.
10. `footerTermsLabel`.
11. `footerTermsUrl`.
12. `footerPrivacyLabel`.
13. `footerPrivacyUrl`.
14. `footerSupportLabel`.
15. `footerSupportUrl`.
16. `footerSupportEmail`.
17. `footerFacebookUrl`.
18. `footerTwitterUrl`.
19. `footerYoutubeUrl`.
20. `footerCopyright`.

Lưu ý quan trọng:
1. Hệ thống chỉ dùng **1 số điện thoại duy nhất** là `hotline`.
2. Không dùng key phone riêng cho footer để tránh lệch dữ liệu.

API gọi và ghi DB:
1. `GET /api/admin/system-settings`
- Đọc `system_settings`.
2. `PUT /api/admin/system-settings`
- Upsert từng key vào `system_settings` (`setting_key`, `setting_value`, `key_name`, `value_text`, `updated_at`).
- Ghi `audit_logs`: `UPDATE_SYSTEM_SETTINGS`.

Tác dụng runtime thật:
1. `maintenanceMode`
- Non-admin bị chặn API bằng HTTP `503`.
2. `autoLockAfterFailedLogin` + `failedLoginLockMinutes`
- Dùng trong login để khóa tạm account sau số lần sai.
3. `maxOpenRequestPerCitizen`
- Chặn công dân tạo request mới nếu đang có quá nhiều request mở.
4. `rescueSlaMinutes`
- Gán `sla_minutes` + `sla_due_at` khi tạo `rescue_requests` mới.
5. `hotline`
- Frontend citizen dashboard và footer toàn hệ thống lấy từ API public để hiển thị số hỗ trợ.
6. `mapRefreshSeconds`
- Frontend coordinator dashboard dùng làm chu kỳ tự refresh dashboard.
7. Nhóm `footer*`
- Footer ở các trang lấy từ `/api/public/runtime-settings`, nên khi admin lưu cấu hình thì giao diện cuối trang đổi theo DB thật, không hardcode frontend.

API public runtime để frontend dùng:
1. `GET /api/public/runtime-settings`.
- Trả về cả tham số vận hành (`maintenanceMode`, `mapRefreshSeconds`, `hotline`, ...)
- Và toàn bộ cấu hình footer (`footerBrandName`, `footerDescription`, link/email/social/copyright).

## 5.7 Nhật ký hệ thống `/admin/nhat-ky-he-thong`
Mục đích:
1. Xem log hành động hệ thống.
2. Filter theo action + keyword.
3. Phân trang.

API gọi:
1. `GET /api/admin/audit-logs?action=&keyword=&page=&size=`.

DB tác động:
1. Trang chỉ đọc `audit_logs`.
2. Tuy nhiên các trang admin khác đều ghi vào `audit_logs` qua `AuditLogService.log(...)`.

## 6. API Catalog chuẩn dùng cho Postman

## 6.1 Auth
1. `POST /api/auth/login`
2. `POST /api/auth/register`

Lưu ý đặc biệt:
1. Có tài khoản admin hardcode không phụ thuộc DB:
- `identifier: admin@gmail.com`
- `password: admin123`

## 6.2 Admin APIs
1. `GET /api/admin/stats`
2. `POST /api/admin/create-user`
3. `GET /api/admin/users`
4. `PUT /api/admin/users/{id}`
5. `PUT /api/admin/users/{id}/status`
6. `PUT /api/admin/users/{id}/reset-password`
7. `DELETE /api/admin/users/{id}`
8. `GET /api/admin/teams`
9. `GET /api/admin/permissions`
10. `GET /api/admin/roles/{roleCode}/permissions`
11. `PUT /api/admin/roles/{roleCode}/permissions`
12. `GET /api/admin/catalogs`
13. `GET /api/admin/catalog-groups`
14. `POST /api/admin/catalogs`
15. `PUT /api/admin/catalogs/{id}`
16. `PATCH /api/admin/catalogs/{id}/active`
17. `DELETE /api/admin/catalogs/{id}`
18. `PUT /api/admin/catalog-groups/{groupCode}`
19. `DELETE /api/admin/catalog-groups/{groupCode}`
20. `GET /api/admin/notification-templates`
21. `POST /api/admin/notification-templates`
22. `PUT /api/admin/notification-templates/{id}`
23. `PATCH /api/admin/notification-templates/{id}/active`
24. `DELETE /api/admin/notification-templates/{id}`
25. `GET /api/admin/system-settings`
26. `PUT /api/admin/system-settings`
27. `GET /api/admin/audit-logs`

## 6.3 Public runtime
1. `GET /api/public/runtime-settings`

## 7. Postman Environment khuyến nghị

| Key | Value |
|---|---|
| `baseUrl` | `http://localhost:8080` |
| `token` | để trống ban đầu |
| `userId` | để trống |
| `templateId` | để trống |
| `catalogId` | để trống |
| `roleCode` | `COORDINATOR` |

## 8. Postman test flow đề xuất cho dev mới

## 8.1 Login và set token
1. Request: `POST {{baseUrl}}/api/auth/login`
2. Body:
```json
{
  "identifier": "admin@gmail.com",
  "password": "admin123"
}
```
3. Tests script:
```javascript
pm.test("login ok", () => pm.response.to.have.status(200));
const data = pm.response.json();
pm.environment.set("token", data.token);
```

## 8.2 Smoke test nhanh toàn bộ admin
1. `GET /api/admin/stats`
2. `GET /api/admin/users?page=0`
3. `GET /api/admin/permissions`
4. `GET /api/admin/catalog-groups`
5. `GET /api/admin/notification-templates?page=0&size=10`
6. `GET /api/admin/system-settings`
7. `GET /api/admin/audit-logs?page=0&size=20`

## 8.3 Kịch bản đầy đủ cho User management
1. Tạo user.
2. Search user theo email vừa tạo.
3. Update user.
4. Lock user bằng `PUT /users/{id}/status`.
5. Verify login user bị chặn.
6. Active lại user.
7. Reset password.
8. Delete user.

## 8.4 Kịch bản đầy đủ cho Notification templates
1. Tạo template.
2. Sửa template.
3. Toggle active.
4. Xóa template.
5. Verify `audit_logs` có đủ action.

## 8.5 Kịch bản đầy đủ cho System settings runtime
1. Set `maintenanceMode=true`.
2. Verify non-admin gọi API nghiệp vụ bị 503.
3. Set `maintenanceMode=false`.
4. Set `autoLockAfterFailedLogin=2`, `failedLoginLockMinutes=1`.
5. Login user sai 2 lần -> bị khóa tạm.
6. Chờ hết 1 phút, login sai tiếp -> bị khóa tạm lại.
7. Admin set status LOCKED -> user không login được cho tới khi admin mở ACTIVE.

## 9. SQL đối chiếu nhanh theo module

## 9.1 Users + role
```sql
SELECT u.id, u.full_name, u.email, u.phone, u.role_id, r.code AS role_code,
       u.status, u.failed_login_attempts, u.locked_at, u.temp_locked_until,
       u.last_login_at, u.created_at, u.updated_at
FROM users u
JOIN roles r ON r.id = u.role_id
ORDER BY u.id DESC;
```

## 9.2 Permissions mapping
```sql
SELECT rp.role_id, r.code AS role_code, rp.permission_id, p.code AS permission_code, rp.created_at
FROM role_permissions rp
JOIN roles r ON r.id = rp.role_id
JOIN permissions p ON p.id = rp.permission_id
ORDER BY rp.role_id, rp.permission_id;
```

## 9.3 System catalog
```sql
SELECT id, group_code, code, name, active, created_at, updated_at
FROM admin_catalogs
ORDER BY group_code, code;
```

## 9.4 Notification templates
```sql
SELECT id, code, template_key, title, channel, active, is_active, created_at, updated_at
FROM notification_templates
ORDER BY id DESC;
```

## 9.5 Notification rules liên quan template
```sql
SELECT id, event_code, template_id, target_role_id, is_active
FROM notification_rules
ORDER BY id DESC;
```

## 9.6 System settings
```sql
SELECT id, setting_key, setting_value, key_name, value_text, value_type, updated_by, description, updated_at
FROM system_settings
ORDER BY setting_key;
```

```sql
SELECT setting_key, setting_value, updated_at
FROM system_settings
WHERE setting_key IN (
  'hotline',
  'footerBrandName',
  'footerDescription',
  'footerTermsLabel',
  'footerTermsUrl',
  'footerPrivacyLabel',
  'footerPrivacyUrl',
  'footerSupportLabel',
  'footerSupportUrl',
  'footerSupportEmail',
  'footerFacebookUrl',
  'footerTwitterUrl',
  'footerYoutubeUrl',
  'footerCopyright'
)
ORDER BY setting_key;
```

## 9.7 Audit logs
```sql
SELECT id, actor_id, action, entity_type, entity_id, actor, target, level, detail, created_at
FROM audit_logs
ORDER BY id DESC
LIMIT 200;
```

## 9.8 Rescue requests (để kiểm tra config runtime có tác dụng)
```sql
SELECT id, code, citizen_id, status, sla_minutes, sla_due_at, created_at, updated_at
FROM rescue_requests
ORDER BY id DESC
LIMIT 100;
```

## 10. Mapping nút UI -> API -> DB (tóm tắt nhanh)

| Trang | Nút/Thao tác | API | DB bị ảnh hưởng |
|---|---|---|---|
| Dashboard | Load thống kê | `GET /api/admin/stats` | Read `users` |
| User | Tạo user | `POST /api/admin/create-user` | Write `users`, `audit_logs` |
| User | Lưu chi tiết user | `PUT /api/admin/users/{id}` | Update `users`, write `audit_logs` |
| User | Đổi trạng thái | `PUT /api/admin/users/{id}/status` | Update `users`, write `audit_logs` |
| User | Reset password | `PUT /api/admin/users/{id}/reset-password` | Update `users`, write `audit_logs` |
| User | Xóa user | `DELETE /api/admin/users/{id}` | Delete `audit_logs(actor_id)` + `users` |
| Permissions | Lưu phân quyền | `PUT /api/admin/roles/{roleCode}/permissions` | Rewrite `role_permissions`, write `audit_logs` |
| Catalog | Thêm/Sửa/Xóa trạng thái | `POST/PUT/DELETE /api/admin/catalogs...` | Write `admin_catalogs`, write `audit_logs` |
| Catalog | Bật/tắt trạng thái | `PATCH /api/admin/catalogs/{id}/active` | Update `admin_catalogs` |
| Catalog | Đổi/Xóa nhóm | `PUT/DELETE /api/admin/catalog-groups/{groupCode}` | Write/Delete `admin_catalogs` |
| Template | Tạo/Sửa | `POST/PUT /api/admin/notification-templates...` | Write `notification_templates` |
| Template | Bật/tắt | `PATCH /api/admin/notification-templates/{id}/active` | Update `notification_templates` |
| Template | Xóa | `DELETE /api/admin/notification-templates/{id}` | Delete `notification_rules` -> `notification_templates` |
| Settings | Lưu cấu hình | `PUT /api/admin/system-settings` | Upsert `system_settings`, write `audit_logs` |
| Audit | Tìm log | `GET /api/admin/audit-logs` | Read `audit_logs` |

## 11. Các lỗi thường gặp
1. `401 Chưa đăng nhập hoặc token không hợp lệ`:
- Thiếu `Authorization: Bearer <token>` hoặc token hết hạn/sai.
2. `403 Access Denied`:
- User không phải ADMIN gọi `/api/admin/**`.
3. `503 Hệ thống đang bảo trì`:
- `maintenanceMode=true` và user không phải ADMIN.
4. `400 ... đã tồn tại`:
- Vi phạm unique (`email`, `phone`, `template_key`, `group_code+code`).
5. `400 Permission không tồn tại`:
- Gửi permission code không có trong bảng `permissions`.
6. `400 Không thể xoá user vì còn dữ liệu liên quan`:
- Còn FK phụ thuộc chưa xử lý hết.

## 12. Kết luận cho dev mới
1. Admin module đã kết nối DB thật, không còn mock cho các trang chính.
2. Tất cả thao tác quan trọng đều có audit log.
3. System settings đã ảnh hưởng runtime thật vào auth/rescue/dashboard.
4. Khi thêm tính năng mới, luôn cập nhật theo chuỗi:
- UI button action.
- API controller/service.
- Repository + DB migration/schema.
- Audit log.
- Test Postman + SQL verify.
