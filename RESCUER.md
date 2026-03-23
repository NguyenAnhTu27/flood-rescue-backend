# Trieu_Demo_RESCUER

**Phạm vi role:** RESCUER (Đội cứu hộ)

**Routes UI đang dùng**
1. `/doi-cuu-ho` -> `RescuerDashboard.jsx`
2. `/doi-cuu-ho/nhiem-vu` -> `MyAssignmentsPage.jsx`
3. `/doi-cuu-ho/nhiem-vu/:id` -> `AssignmentDetailPage.jsx`
4. `/doi-cuu-ho/sap-xep-yeu-cau-cuu-tro` -> `ReliefPrioritizePage.jsx`
5. `/doi-cuu-ho/sap-xep-yeu-cau-cuu-tro/:id` -> `ReliefPrioritizeDetailPage.jsx`

**Bảng DB liên quan thường gặp**
1. `rescue_requests`
2. `rescue_request_timeline`
3. `task_groups`
4. `task_group_timeline`
5. `rescue_assignments`
6. `teams`
7. `assets`
8. `relief_requests`
9. `inventory_issues`
10. `notifications`

**Trạng thái liên quan**
1. `RescueRequestStatus`: `ASSIGNED`, `IN_PROGRESS`, `COMPLETED`, `CANCELLED`
2. `TaskGroupStatus`: `ASSIGNED`, `IN_PROGRESS`, `DONE`, `CANCELLED`
3. `ReliefDeliveryStatus`: `RESCUER_RECEIVED`, `ARRIVED_WAREHOUSE`, `ARRIVED_RELIEF_POINT`, `RETURNED_TO_WAREHOUSE`, `COMPLETED`
4. `InventoryDocumentStatus`: `DRAFT`, `APPROVED`, `DONE`, `CANCELLED`

---

**1) Dashboard đội cứu hộ (`/doi-cuu-ho`)**

**Nút: `Mở chi tiết nhiệm vụ`**
1. Điều hướng `/doi-cuu-ho/nhiem-vu/:id`.
2. Không ghi DB.

**Nút: `Cập nhật vị trí đội`**
1. API: `POST /api/rescue/rescuer/team-location`.
2. DB: cập nhật `teams.current_latitude`, `teams.current_longitude`, `teams.current_location_text`.

**Nút: `Xem yêu cầu cứu trợ`**
1. Điều hướng `/doi-cuu-ho/sap-xep-yeu-cau-cuu-tro`.
2. Không ghi DB.

---

**2) Danh sách nhiệm vụ (`/doi-cuu-ho/nhiem-vu`)**

**Click vào nhiệm vụ**
1. Điều hướng `/doi-cuu-ho/nhiem-vu/:id`.
2. Không ghi DB.

---

**3) Chi tiết nhiệm vụ (`/doi-cuu-ho/nhiem-vu/:id`)**

**Nút: `Cập nhật trạng thái nhiệm vụ (Rescue Request)`**
1. API: `PUT /api/rescue/rescuer/tasks/{id}/status?status=...&note=...`.
2. DB: cập nhật `rescue_requests.status` theo trạng thái mới.
3. DB: thêm `rescue_request_timeline` (STATUS_CHANGE/NOTE).
4. Nếu trạng thái `COMPLETED`, hệ thống chờ citizen xác nhận (cập nhật các cột xác nhận trong `rescue_requests`).

**Nút: `Cập nhật trạng thái nhóm (Task Group)`**
1. API: `PUT /api/rescue/rescuer/task-groups/{id}/status?status=...&note=...`.
2. DB: cập nhật `task_groups.status`.
3. DB: thêm `task_group_timeline`.
4. DB: đồng bộ `rescue_requests.status` theo nhóm (ASSIGNED/IN_PROGRESS/COMPLETED/CANCELLED).
5. DB: thêm `rescue_request_timeline` cho từng request trong nhóm.

**Nút: `Thêm ghi chú`**
1. API: `POST /api/rescue/rescuer/tasks/{id}/notes`.
2. DB: thêm `rescue_request_timeline` (NOTE).

**Nút: `Báo leo thang`**
1. API: `POST /api/rescue/rescuer/task-groups/{id}/escalate`.
2. DB: ghi nhận vào `task_group_timeline` hoặc log nội bộ (tùy service).
3. DB: tạo `notifications` cho điều phối nếu hệ thống cấu hình thông báo leo thang.

**Nút: `Trả phương tiện`**
1. API: `POST /api/rescue/rescuer/assets/return`.
2. DB: cập nhật `assets.status` về `AVAILABLE` (hoặc trạng thái rảnh tương ứng).

---

**4) Sắp xếp yêu cầu cứu trợ (`/doi-cuu-ho/sap-xep-yeu-cau-cuu-tro`)**

**Nút: `Xem chi tiết yêu cầu cứu trợ`**
1. Điều hướng `/doi-cuu-ho/sap-xep-yeu-cau-cuu-tro/:id`.
2. Không ghi DB.

---

**5) Chi tiết yêu cầu cứu trợ (`/doi-cuu-ho/sap-xep-yeu-cau-cuu-tro/:id`)**

**Nút: `Cập nhật trạng thái giao hàng`**
1. API: `PUT /api/relief/rescuer/requests/{id}/delivery-status`.
2. DB: cập nhật `relief_requests.delivery_status`.
3. Nếu status `COMPLETED`, DB cập nhật `relief_requests.status=DONE`.
4. DB: có thể cập nhật `inventory_issues` liên quan (nếu ràng buộc tồn kho hoặc tiến độ).
5. DB: tạo `notifications` cho citizen (nếu hệ thống bật thông báo).
