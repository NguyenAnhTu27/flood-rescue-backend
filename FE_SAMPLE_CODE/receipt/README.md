# Code mẫu giao diện duyệt phiếu nhập kho

## Files:
- `receiptApi.ts` - API functions để gọi BE
- `ReceiptListPage.tsx` - Component React hiển thị danh sách và duyệt phiếu nhập

## Cách sử dụng:

### 1. Copy file `receiptApi.ts` vào project FE của bạn
- Điều chỉnh import path của `httpClient` theo cấu trúc project
- Đảm bảo `httpClient` đã được config với JWT token

### 2. Copy file `ReceiptListPage.tsx` vào project FE của bạn
- Component này sử dụng Ant Design (antd)
- Nếu bạn dùng UI library khác, có thể điều chỉnh tương ứng

### 3. Tích hợp vào routing:
```tsx
import ReceiptListPage from '@/pages/receipt/ReceiptListPage';

// Trong router config
{
  path: '/inventory/receipts',
  element: <ReceiptListPage />,
}
```

## Tính năng:
- ✅ Hiển thị danh sách phiếu nhập kho
- ✅ Lọc theo trạng thái (DRAFT, APPROVED, DONE, CANCELLED)
- ✅ Duyệt phiếu nhập (chỉ phiếu DRAFT)
- ✅ Hủy phiếu nhập (chỉ phiếu DRAFT)
- ✅ Xem chi tiết phiếu nhập
- ✅ Phân trang

## API Endpoints sử dụng:
- `GET /api/inventory/receipts?status=DRAFT` - Lấy danh sách
- `PUT /api/inventory/receipts/{id}/approve` - Duyệt phiếu
- `PUT /api/inventory/receipts/{id}/cancel` - Hủy phiếu
- `GET /api/inventory/receipts/{id}` - Chi tiết phiếu

## Lưu ý:
- Cần quyền `ADMIN` hoặc `MANAGER` để duyệt phiếu
- Sau khi duyệt, tồn kho sẽ tự động tăng lên
- Chỉ có thể duyệt/hủy phiếu ở trạng thái `DRAFT`
