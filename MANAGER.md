# Trieu_Demo_MANAGER

**Phạm vi role:** MANAGER (Quản lý cứu trợ & kho)

**Routes UI đang dùng**
1. `/quan-ly` -> `ManagerDashboard.jsx`
2. `/quan-ly/yeu-cau-cuu-tro` -> `ReliefRequestsPage.jsx`
3. `/quan-ly/kho-hang` -> `InventoryOverviewPage.jsx`
4. `/quan-ly/danh-muc-hang` -> `ItemCategoriesPage.jsx`
5. `/quan-ly/phan-loai-hang` -> `ItemClassificationsPage.jsx`
6. `/quan-ly/don-vi` -> `ItemUnitsPage.jsx`
7. `/quan-ly/phieu-nhap` -> `ReceiptCreatePage.jsx`
8. `/quan-ly/phieu-xuat` -> `IssueCreatePage.jsx`
9. `/quan-ly/doi-cuu-ho-cuu-tro` -> `ReliefTeamManagementPage.jsx`
10. `/quan-ly/yeu-cau-cuu-tro-da-duyet-phieu-xuat` -> `ApprovedReliefIssueRequestsPage.jsx`
11. `/quan-ly/tao-yeu-cau-cuu-tro` -> `ReliefRequestCreatePage.jsx`
12. `/quan-ly/phuong-tien` -> `AssetsManagementPage.jsx`
13. `/quan-ly/tao-phuong-tien` -> `AssetCreatePage.jsx`

**Bảng DB liên quan thường gặp**
1. `relief_requests`
2. `relief_request_lines`
3. `inventory_issues`
4. `inventory_issue_lines`
5. `inventory_receipts`
6. `inventory_receipt_lines`
7. `stock_balances`
8. `item_categories`
9. `item_classifications`
10. `item_units`
11. `assets`
12. `teams`
13. `notifications`

**Trạng thái liên quan**
1. `InventoryDocumentStatus`: `DRAFT`, `APPROVED`, `DONE`, `CANCELLED`
2. `ReliefDeliveryStatus`: `REQUESTED`, `MANAGER_APPROVED`, `RESCUER_RECEIVED`, `ARRIVED_WAREHOUSE`, `ARRIVED_RELIEF_POINT`, `RETURNED_TO_WAREHOUSE`, `COMPLETED`, `REJECTED`

---

**1) Dashboard quản lý (`/quan-ly`)**

**Nút/Thẻ điều hướng**
1. Các thẻ “Yêu cầu cứu trợ”, “Kho hàng”, “Phương tiện”… chỉ điều hướng, không ghi DB.
2. API đọc dữ liệu tổng quan: `GET /api/relief/dashboard` và `GET /api/inventory/issues`.

---

**2) Tạo yêu cầu cứu trợ (`/quan-ly/tao-yeu-cau-cuu-tro`)**

**Nút: `Gửi yêu cầu cứu trợ`**
1. API: `POST /api/relief/requests`.
2. DB: thêm `relief_requests` (status `DRAFT`), các trường địa chỉ, tọa độ, mô tả, note.
3. DB: thêm `relief_request_lines` nếu có danh sách hàng.

---

**3) Danh sách yêu cầu cứu trợ (`/quan-ly/yeu-cau-cuu-tro`)**

**Nút: `Duyệt & điều phối`**
1. API: `PUT /api/relief/requests/{id}/approve-dispatch`.
2. DB: tạo `inventory_issues` và `inventory_issue_lines` từ yêu cầu cứu trợ.
3. DB: cập nhật `relief_requests.status=APPROVED`, `delivery_status=MANAGER_APPROVED`, `assigned_team_id`, `assigned_issue_id`.
4. DB: tạo `notifications` cho citizen và đội cứu hộ được giao.

**Nút: `Từ chối yêu cầu`**
1. API: `PUT /api/relief/requests/{id}/reject`.
2. DB: cập nhật `relief_requests.status=CANCELLED`, `delivery_status=REJECTED`, `delivery_note=reason`.
3. DB: tạo `notifications` cho citizen.

**Nút: `Tạo phiếu xuất từ yêu cầu`**
1. Điều hướng sang `/quan-ly/phieu-xuat` với dữ liệu prefill.
2. Không ghi DB.

---

**4) Quản lý đội cứu trợ (`/quan-ly/doi-cuu-ho-cuu-tro`)**

**Nút: `Từ chối yêu cầu`**
1. API: `PUT /api/relief/requests/{id}/reject`.
2. DB: cập nhật `relief_requests.status=CANCELLED`, `delivery_status=REJECTED`.

**Nút: `Xem chi tiết phiếu xuất`**
1. API: `GET /api/inventory/issues/{id}`.
2. Không ghi DB.

---

**5) Yêu cầu đã duyệt phiếu xuất (`/quan-ly/yeu-cau-cuu-tro-da-duyet-phieu-xuat`)**

**Thao tác lọc/xem**
1. API: `GET /api/inventory/issues` và `GET /api/relief/requests`.
2. Không ghi DB.

---

**6) Kho hàng tổng quan (`/quan-ly/kho-hang`)**

**Nút: `Tạo phiếu nhập`**
1. Điều hướng `/quan-ly/phieu-nhap`.
2. Không ghi DB.

**Nút: `Tạo phiếu xuất`**
1. Điều hướng `/quan-ly/phieu-xuat`.
2. Không ghi DB.

**Các thống kê kho**
1. API đọc: `GET /api/inventory/stock`, `GET /api/inventory/receipts`, `GET /api/inventory/issues`.
2. Không ghi DB.

---

**7) Danh mục hàng (`/quan-ly/danh-muc-hang`)**

**Nút: `Tạo danh mục`**
1. API: `POST /api/inventory/items`.
2. DB: thêm `item_categories`.

**Nút: `Xóa danh mục`**
1. API: `DELETE /api/inventory/items/{id}`.
2. DB: xóa `item_categories` (nếu không có ràng buộc chứng từ).

---

**8) Phân loại hàng (`/quan-ly/phan-loai-hang`)**

**Nút: `Tạo phân loại`**
1. API: `POST /api/inventory/item-classifications`.
2. DB: thêm `item_classifications`.

**Nút: `Xóa phân loại`**
1. API: `DELETE /api/inventory/item-classifications/{id}`.
2. DB: xóa `item_classifications` (nếu không còn danh mục dùng).

---

**9) Đơn vị tính (`/quan-ly/don-vi`)**

**Nút: `Tạo đơn vị`**
1. API: `POST /api/inventory/item-units`.
2. DB: thêm `item_units`.

**Nút: `Xóa đơn vị`**
1. API: `DELETE /api/inventory/item-units/{id}`.
2. DB: xóa `item_units` (nếu không còn danh mục dùng).

---

**10) Phiếu nhập kho (`/quan-ly/phieu-nhap`)**

**Nút: `Xác nhận nhập kho`**
1. API: `POST /api/inventory/receipts` (tạo nháp) và `PUT /api/inventory/receipts/{id}/approve` (duyệt).
2. DB: thêm `inventory_receipts` và `inventory_receipt_lines`.
3. DB: cập nhật `inventory_receipts.status=APPROVED`.
4. DB: cập nhật `stock_balances` tăng tồn kho.

---

**11) Phiếu xuất kho (`/quan-ly/phieu-xuat`)**

**Nút: `Tạo phiếu xuất`**
1. API: `POST /api/inventory/issues`.
2. DB: thêm `inventory_issues` và `inventory_issue_lines`.

**Nút: `Duyệt phiếu xuất`**
1. API: `PUT /api/inventory/issues/{id}/approve`.
2. DB: cập nhật `inventory_issues.status=APPROVED`.
3. DB: cập nhật `stock_balances` giảm tồn kho theo dòng xuất.

---

**12) Quản lý phương tiện (`/quan-ly/phuong-tien`)**

**Nút: `Tạo phương tiện`**
1. Điều hướng `/quan-ly/tao-phuong-tien`.
2. Không ghi DB.

**Nút: `Cập nhật trạng thái`**
1. API: `PUT /api/assets/{id}/status`.
2. DB: cập nhật `assets.status`.

---

**13) Tạo phương tiện (`/quan-ly/tao-phuong-tien`)**

**Nút: `Lưu phương tiện`**
1. API: `POST /api/assets`.
2. DB: thêm `assets`.
