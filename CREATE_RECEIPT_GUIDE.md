# Hướng dẫn tạo phiếu nhập kho

## Bước 1: Lấy ID của mặt hàng "Lương Thực"

```sql
SELECT id, code, name FROM item_categories WHERE name LIKE '%Lương Thực%';
```

Hoặc gọi API:
```bash
GET http://localhost:8080/api/inventory/items
Authorization: Bearer <your-jwt-token>
```

## Bước 2: Tạo phiếu nhập kho

### Endpoint:
```
POST http://localhost:8080/api/inventory/receipts
```

### Headers:
```
Content-Type: application/json
Authorization: Bearer <your-jwt-token>
```

### Request Body (JSON):
```json
{
  "sourceType": "DONATION",
  "note": "Nhập kho để tăng tồn kho cho mặt hàng Lương Thực",
  "lines": [
    {
      "itemCategoryId": 1,
      "qty": 100.0,
      "unit": "kg"
    }
  ]
}
```

**Lưu ý:**
- `sourceType`: Chỉ có 2 giá trị hợp lệ: `"DONATION"` hoặc `"PURCHASE"`
- `itemCategoryId`: ID của mặt hàng (lấy từ Bước 1)
- `qty`: Số lượng (phải > 0)
- `unit`: Đơn vị tính (ví dụ: "kg", "thùng", "bao", ...)

### Ví dụ với cURL:
```bash
curl -X POST http://localhost:8080/api/inventory/receipts \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -d '{
    "sourceType": "DONATION",
    "note": "Nhập kho Lương Thực",
    "lines": [
      {
        "itemCategoryId": 1,
        "qty": 100.0,
        "unit": "kg"
      }
    ]
  }'
```

### Response:
```json
{
  "id": 6,
  "code": "IR202603060001",
  "status": "DRAFT",
  "sourceType": "DONATION",
  "createdById": 1,
  "note": "Nhập kho Lương Thực",
  "createdAt": "2026-03-06T01:10:00",
  "lines": [
    {
      "itemCategoryId": 1,
      "itemCategoryName": "Lương Thực",
      "qty": 100.0,
      "unit": "kg"
    }
  ]
}
```

**Lưu ý:** Sau khi tạo, phiếu sẽ ở trạng thái `DRAFT`. Tồn kho CHƯA tăng lên.

## Bước 3: Duyệt phiếu nhập kho (APPROVE)

Sau khi duyệt, tồn kho mới được tăng lên.

### Endpoint:
```
PUT http://localhost:8080/api/inventory/receipts/{id}/approve
```

### Ví dụ:
```bash
PUT http://localhost:8080/api/inventory/receipts/6/approve
Authorization: Bearer <your-jwt-token>
```

### Ví dụ với cURL:
```bash
curl -X PUT http://localhost:8080/api/inventory/receipts/6/approve \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

### Response:
```json
{
  "id": 6,
  "code": "IR202603060001",
  "status": "APPROVED",
  "sourceType": "DONATION",
  ...
}
```

**Sau khi duyệt:** Tồn kho sẽ tự động tăng lên trong bảng `stock_balances`.

## Bước 4: Kiểm tra tồn kho đã tăng chưa

```sql
SELECT 
    ic.name AS item_name,
    sb.source_type,
    sb.qty AS stock_qty,
    sb.unit
FROM stock_balances sb
INNER JOIN item_categories ic ON sb.item_category_id = ic.id
WHERE ic.name LIKE '%Lương Thực%';
```

## Ví dụ đầy đủ với nhiều mặt hàng:

```json
{
  "sourceType": "PURCHASE",
  "note": "Nhập kho nhiều mặt hàng",
  "lines": [
    {
      "itemCategoryId": 1,
      "qty": 100.0,
      "unit": "kg"
    },
    {
      "itemCategoryId": 2,
      "qty": 50.0,
      "unit": "thùng"
    },
    {
      "itemCategoryId": 3,
      "qty": 200.0,
      "unit": "chai"
    }
  ]
}
```
