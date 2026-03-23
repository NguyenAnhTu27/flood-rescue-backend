# Trieu_Demo_ADMIN

**Phạm vi role:** ADMIN (Quản trị hệ thống)

**Routes UI đang dùng**
1. `/admin` -> `AdminDashboard.jsx`
2. `/admin/nguoi-dung` -> `UserManagementPage.jsx`
3. `/admin/doi-cuu-ho` -> `TeamsManagementPage.jsx`
4. `/admin/tao-doi-cuu-ho` -> `TeamCreatePage.jsx`
5. `/admin/cau-hinh-he-thong` -> `SystemSettingsPage.jsx`
6. `/admin/nhat-ky-he-thong` -> `AuditLogsPage.jsx`
7. `/admin/phan-hoi-he-thong` -> `SystemFeedbacksPage.jsx`
8. `/admin/noi-dung-trang` -> `ContentPagesSettingsPage.jsx`

**Bảng DB liên quan thường gặp**
1. `users`
2. `roles`
3. `teams`
4. `audit_logs`
5. `system_settings`
6. `system_feedbacks`

---

**1) Dashboard Admin (`/admin`)**

**Nút/Thẻ điều hướng**
1. Click thẻ điều hướng sang quản lý người dùng, cấu hình, nhật ký.
2. API đọc thống kê: `GET /api/admin/stats`.
3. Không ghi DB.

---

**2) Quản lý người dùng (`/admin/nguoi-dung`)**

**Nút: `Tạo tài khoản`**
1. API: `POST /api/admin/create-user`.
2. DB: thêm `users` (email, phone, role_id, team_id nếu có, password_hash).
3. DB: thêm `audit_logs` (nếu service có ghi log hành động).

**Nút: `Cập nhật thông tin`**
1. API: `PUT /api/admin/users/{id}`.
2. DB: cập nhật `users.full_name`, `users.phone`, `users.role_id`, `users.team_id`, `users.status`.
3. DB: thêm `audit_logs` (nếu bật ghi log).

**Nút: `Đặt lại mật khẩu`**
1. API: `POST /api/admin/users/{id}/reset-password`.
2. DB: cập nhật `users.password_hash`.
3. DB: thêm `audit_logs` (nếu bật ghi log).

**Nút: `Khóa/Mở khóa tài khoản`**
1. API: `PUT /api/admin/users/{id}/status`.
2. DB: cập nhật `users.status`.
3. DB: thêm `audit_logs` (nếu bật ghi log).

**Nút: `Xóa tài khoản`**
1. API: `DELETE /api/admin/users/{id}`.
2. DB: xóa `users` (hoặc đánh dấu soft-delete tùy service).
3. DB: thêm `audit_logs` (nếu bật ghi log).

---

**3) Quản lý đội cứu hộ (`/admin/doi-cuu-ho`)**

**Nút: `Tạo đội`**
1. Điều hướng `/admin/tao-doi-cuu-ho`.
2. Không ghi DB.

**Nút: `Sửa đội`**
1. Điều hướng `/admin/tao-doi-cuu-ho?id=...`.
2. Không ghi DB.

**Nút: `Xóa đội`**
1. API: `DELETE /api/admin/teams/{id}`.
2. DB: xóa `teams` và cập nhật `users.team_id` liên quan (nếu service xử lý).
3. DB: thêm `audit_logs` (nếu bật ghi log).

---

**4) Tạo/Sửa đội (`/admin/tao-doi-cuu-ho`)**

**Nút: `Lưu đội`**
1. API: `POST /api/admin/teams` (tạo) hoặc `PUT /api/admin/teams/{id}` (cập nhật).
2. DB: thêm/cập nhật `teams`.
3. DB: thêm `audit_logs` (nếu bật ghi log).

---

**5) Cấu hình hệ thống (`/admin/cau-hinh-he-thong`)**

**Nút: `Lưu cấu hình`**
1. API: `PUT /api/admin/system-settings`.
2. DB: cập nhật `system_settings` theo từng key.
3. DB: thêm `audit_logs` (action `UPDATE_SYSTEM_SETTINGS`).

---

**6) Nội dung trang công khai (`/admin/noi-dung-trang`)**

**Nút: `Lưu nội dung`**
1. API: `PUT /api/admin/content-pages`.
2. DB: cập nhật `system_settings` (các key nội dung trang).
3. DB: thêm `audit_logs` (action `UPDATE_CONTENT_PAGES`).

---

**7) Nhật ký hệ thống (`/admin/nhat-ky-he-thong`)**

**Thao tác lọc/xem**
1. API: `GET /api/admin/audit-logs` (tùy filter).
2. Không ghi DB.

---

**8) Phản hồi hệ thống (`/admin/phan-hoi-he-thong`)**

**Thao tác lọc/xem**
1. API: `GET /api/feedback/admin` và `GET /api/feedback/admin/summary`.
2. Không ghi DB.
