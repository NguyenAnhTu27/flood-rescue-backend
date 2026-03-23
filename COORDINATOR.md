# Trieu_Demo_COORDINATOR

**Phạm vi role:** COORDINATOR (Điều phối)

**Routes UI đang dùng**
1. `/dieu-phoi` -> `CoordinatorDashboardPage.jsx`
2. `/dieu-phoi/xac-minh` -> `RescueVerifyPage.jsx`
3. `/dieu-phoi/phan-cong` -> `RescueAssignPage.jsx`
4. `/dieu-phoi/giam-sat-nhiem-vu` -> `RescueRequestHandle.jsx`
5. `/dieu-phoi/lich-su-cuu-ho` -> `RescueHistoryPage.jsx`
6. `/dieu-phoi/theo-doi-doi-cuu-ho` -> `TeamWorkloadPage.jsx`
7. `/dieu-phoi/phan-loai` -> `RescuePrioritizePage.jsx`
8. `/dieu-phoi/trung-lap` -> `DuplicateManagementPage.jsx`
9. `/dieu-phoi/da-khoa` -> `BlockedCitizensPage.jsx`

**Bảng DB liên quan thường gặp**
1. `rescue_requests`
2. `rescue_request_timeline`
3. `rescue_request_attachments`
4. `task_groups`
5. `task_group_requests`
6. `task_group_timeline`
7. `rescue_assignments`
8. `users`
9. `teams`
10. `assets`
11. `notifications`

**Trạng thái liên quan**
1. `RescueRequestStatus`: `PENDING`, `VERIFIED`, `ASSIGNED`, `IN_PROGRESS`, `COMPLETED`, `CANCELLED`, `DUPLICATE`
2. `TaskGroupStatus`: `NEW`, `ASSIGNED`, `IN_PROGRESS`, `DONE`, `CANCELLED`

---

**1) Dashboard điều phối (`/dieu-phoi`)**

**Nút/Thẻ điều hướng**
1. Click vào thẻ yêu cầu hoặc nút nhanh sẽ điều hướng sang `Xác minh`, `Phân công`, `Theo dõi đội`, `Danh sách đã khóa`.
2. Không ghi DB, chỉ điều hướng.
3. API đọc dữ liệu tổng quan: `GET /api/rescue/coordinator/dashboard`.

---

**2) Xác minh yêu cầu (`/dieu-phoi/xac-minh`)**

**Nút: `Xác minh yêu cầu`**
1. API: `POST /api/rescue/coordinator/requests/{id}/verify` với `locationVerified`, `note`, hoặc `cancelRequest`.
2. DB: cập nhật `rescue_requests.status` từ `PENDING` -> `VERIFIED` (nếu xác minh thành công).
3. DB: cập nhật `rescue_requests.location_verified` và ghi chú nếu có.
4. DB: thêm `rescue_request_timeline` (VERIFY/NOTE/STATUS_CHANGE).
5. Nếu `cancelRequest=true`, DB cập nhật `rescue_requests.status=CANCELLED` hoặc tạo trạng thái chờ tuỳ `cancelAction`.

**Nút: `Đổi mức ưu tiên`**
1. API: `PUT /api/rescue/coordinator/requests/{id}/priority`.
2. DB: cập nhật `rescue_requests.priority`.
3. DB: thêm `rescue_request_timeline` (NOTE).

**Nút: `Đánh dấu trùng lặp`**
1. API: `POST /api/rescue/coordinator/requests/{id}/duplicate` với `masterRequestId`.
2. DB: cập nhật `rescue_requests.status=DUPLICATE`, gán `master_request_id`.
3. DB: thêm `rescue_request_timeline` (STATUS_CHANGE/NOTE).

**Nút: `Khoá/Gỡ khoá citizen`**
1. API: `POST /api/rescue/coordinator/requests/{id}/citizen-block`.
2. DB: cập nhật `users.rescue_request_blocked` và `users.rescue_request_blocked_reason`.
3. DB: thêm `rescue_request_timeline` (NOTE) ghi lại hành động.
4. DB: tạo `notifications` cho citizen (`CITIZEN_REQUEST_BLOCKED` hoặc `CITIZEN_REQUEST_UNBLOCKED`).

**Nút: `Thêm ghi chú`**
1. API: `POST /api/rescue/coordinator/requests/{id}/notes`.
2. DB: thêm `rescue_request_timeline` (NOTE).

---

**3) Phân công cứu hộ (`/dieu-phoi/phan-cong`)**

**Nút: `Tạo nhóm nhiệm vụ`**
1. API: `POST /api/rescue/coordinator/task-groups` với danh sách `rescueRequestIds`.
2. DB: tạo `task_groups` (status `NEW`).
3. DB: tạo các dòng `task_group_requests` liên kết rescue_request với task_group.
4. DB: thêm `task_group_timeline` và `rescue_request_timeline` (STATUS_CHANGE/NOTE).

**Nút: `Phân công đội & phương tiện`**
1. API: `POST /api/rescue/coordinator/task-groups/assign` với `teamId`, `assetId` (nếu có).
2. DB: cập nhật `task_groups.assigned_team_id`, `task_groups.status=ASSIGNED`.
3. DB: tạo `rescue_assignments` cho team.
4. DB: cập nhật `rescue_requests.status=ASSIGNED` theo nhóm.
5. DB: thêm `task_group_timeline` và `rescue_request_timeline`.
6. DB: tạo `notifications` cho đội cứu hộ được giao nhiệm vụ.

**Nút: `Đổi trạng thái nhóm`** (nếu có trên UI)
1. API: `PUT /api/rescue/coordinator/task-groups/{id}/status`.
2. DB: cập nhật `task_groups.status`.
3. DB: đồng bộ `rescue_requests.status` tương ứng (ASSIGNED/IN_PROGRESS/COMPLETED/CANCELLED).
4. DB: thêm `task_group_timeline` và `rescue_request_timeline`.

---

**4) Giám sát nhiệm vụ (`/dieu-phoi/giam-sat-nhiem-vu`)**

**Nút/Thao tác lọc, xem chi tiết**
1. API: `GET /api/rescue/coordinator/task-groups` và `GET /api/rescue/coordinator/task-groups/{id}`.
2. Không ghi DB.

---

**5) Lịch sử cứu hộ (`/dieu-phoi/lich-su-cuu-ho`)**

**Nút/Thao tác lọc, xem chi tiết**
1. API: đọc `GET /api/rescue/coordinator/requests` với filter status.
2. Không ghi DB.

---

**6) Theo dõi đội (`/dieu-phoi/theo-doi-doi-cuu-ho`)**

**Nút: `Xem nhiệm vụ đội`**
1. Điều hướng sang `Giám sát nhiệm vụ` với `teamId`.
2. Không ghi DB.

---

**7) Phân loại ưu tiên (`/dieu-phoi/phan-loai`)**

**Nút: `Cập nhật ưu tiên`**
1. API: `PUT /api/rescue/coordinator/requests/{id}/priority`.
2. DB: cập nhật `rescue_requests.priority`.
3. DB: thêm `rescue_request_timeline` (NOTE).

---

**8) Quản lý trùng lặp (`/dieu-phoi/trung-lap`)**

**Nút: `Đánh dấu trùng lặp`**
1. API: `POST /api/rescue/coordinator/requests/{id}/duplicate`.
2. DB: cập nhật `rescue_requests.status=DUPLICATE`, `master_request_id`.
3. DB: thêm `rescue_request_timeline`.

---

**9) Citizen đã khóa (`/dieu-phoi/da-khoa`)**

**Nút: `Gỡ khóa`**
1. API: `POST /api/rescue/coordinator/citizens/{citizenId}/unblock`.
2. DB: cập nhật `users.rescue_request_blocked=false`, `users.rescue_request_blocked_reason=NULL`.
3. DB: thêm `rescue_request_timeline` (NOTE) nếu gỡ khóa dựa trên request.
4. DB: tạo `notifications` cho citizen và coordinator (type `CITIZEN_REQUEST_UNBLOCKED`, `COORDINATOR_UNBLOCKED_CITIZEN`).
