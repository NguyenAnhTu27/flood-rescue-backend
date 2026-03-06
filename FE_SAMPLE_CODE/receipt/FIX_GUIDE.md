# Hướng dẫn sửa lỗi trang duyệt phiếu nhập

## Các vấn đề đã được sửa:

### 1. **Filter mặc định = 'DRAFT'**
   - **Trước:** Filter mặc định = `''` (Tất cả) → khó thấy phiếu cần duyệt
   - **Sau:** Filter mặc định = `'DRAFT'` → tự động hiển thị phiếu cần duyệt

### 2. **Cải thiện error handling**
   - Parse error message từ nhiều format khác nhau
   - Hiển thị error message rõ ràng hơn
   - Thêm console.log để debug

### 3. **Loading state cho từng action**
   - Thêm `actionLoading` state để track từng action riêng biệt
   - Hiển thị "Đang duyệt..." / "Đang hủy..." khi đang xử lý
   - Disable buttons khi đang xử lý

### 4. **Cải thiện feedback sau khi duyệt**
   - Kiểm tra `result.status === 'DONE'` để confirm
   - Hiển thị thông báo rõ ràng hơn
   - Tự động refresh danh sách sau khi duyệt thành công

### 5. **Thêm thông báo khi không có phiếu DRAFT**
   - Hiển thị message hướng dẫn khi không có phiếu cần duyệt

## Cách áp dụng:

### Bước 1: Kiểm tra API functions
Đảm bảo file `features/relief/api.js` (hoặc `features/inventory/api.js`) có các functions:

```javascript
export async function listInventoryReceipts(params) { ... }
export async function approveInventoryReceipt(id) { ... }
export async function cancelInventoryReceipt(id) { ... }
export async function getInventoryReceipt(id) { ... }
```

Nếu chưa có, copy từ file `inventoryReceiptApi.js` đã tạo.

### Bước 2: Cập nhật component
Thay thế code trong file `ReceiptApprovalPage.jsx` bằng code từ `ReceiptApprovalPage_FIXED.jsx`.

**Hoặc chỉ cần sửa các điểm sau:**

1. **Dòng 33:** Đổi filter mặc định
```javascript
// Trước:
const [statusFilter, setStatusFilter] = useState('');

// Sau:
const [statusFilter, setStatusFilter] = useState('DRAFT');
```

2. **Thêm state actionLoading:**
```javascript
const [actionLoading, setActionLoading] = useState({});
```

3. **Cập nhật hàm handleApprove:**
   - Thêm `setActionLoading` trước và sau khi gọi API
   - Cải thiện error handling
   - Kiểm tra `result.status === 'DONE'`

4. **Cập nhật hàm handleCancel:**
   - Tương tự như `handleApprove`

5. **Cập nhật render buttons:**
   - Thêm loading state cho từng button
   - Disable khi đang xử lý

### Bước 3: Kiểm tra API endpoint
Đảm bảo BE đang chạy và API endpoints đúng:
- `GET /api/inventory/receipts?status=DRAFT`
- `PUT /api/inventory/receipts/{id}/approve`
- `PUT /api/inventory/receipts/{id}/cancel`

### Bước 4: Test
1. Mở trang duyệt phiếu nhập
2. Filter mặc định sẽ là "Nháp" (DRAFT)
3. Click nút "Duyệt" trên một phiếu DRAFT
4. Xác nhận → Nên thấy loading → Thành công → Danh sách tự động refresh
5. Kiểm tra tồn kho đã tăng chưa

## Debug nếu vẫn không hoạt động:

1. **Mở Console (F12) và kiểm tra:**
   - Có log `[ReceiptApprovalPage] Fetching receipts...` không?
   - Có log `[ReceiptApprovalPage] Approving receipt...` không?
   - Có error nào không?

2. **Kiểm tra Network tab:**
   - Request `PUT /api/inventory/receipts/{id}/approve` có được gửi không?
   - Response status code là gì? (200 = OK, 401 = Unauthorized, 403 = Forbidden, 500 = Server Error)
   - Response body có gì?

3. **Kiểm tra JWT token:**
   - Token có hợp lệ không?
   - Token có quyền ADMIN hoặc MANAGER không?

4. **Kiểm tra BE logs:**
   - Có log gì khi gọi API approve không?
   - Có exception nào không?

## Lưu ý:
- Chỉ có thể duyệt phiếu ở trạng thái `DRAFT`
- Cần quyền `ADMIN` hoặc `MANAGER` để duyệt
- Sau khi duyệt, `status` sẽ chuyển từ `DRAFT` → `DONE`
- Tồn kho sẽ tự động tăng sau khi duyệt thành công
