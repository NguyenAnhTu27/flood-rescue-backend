# Hướng dẫn duyệt phiếu nhập kho

## Cách 1: Duyệt qua Postman/cURL (Nhanh nhất)

### Bước 1: Lấy danh sách phiếu nhập chưa duyệt (DRAFT)

**Endpoint:**
```
GET http://localhost:8080/api/inventory/receipts?status=DRAFT
```

**Headers:**
```
Authorization: Bearer <your-jwt-token>
```

**cURL:**
```bash
curl -X GET "http://localhost:8080/api/inventory/receipts?status=DRAFT" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

**Response:**
```json
{
  "content": [
    {
      "id": 1,
      "code": "IR202603047060",
      "status": "DRAFT",
      "sourceType": "DONATION",
      "createdById": 4,
      "note": "...",
      "createdAt": "2026-03-04T16:35:04",
      "lines": [...]
    }
  ],
  "totalElements": 5,
  "totalPages": 1
}
```

### Bước 2: Duyệt từng phiếu nhập

**Endpoint:**
```
PUT http://localhost:8080/api/inventory/receipts/{id}/approve
```

**Ví dụ:**
```
PUT http://localhost:8080/api/inventory/receipts/1/approve
```

**Headers:**
```
Authorization: Bearer <your-jwt-token>
```

**cURL:**
```bash
curl -X PUT http://localhost:8080/api/inventory/receipts/1/approve \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

**Response:**
```json
{
  "id": 1,
  "code": "IR202603047060",
  "status": "DONE",
  "sourceType": "DONATION",
  ...
}
```

**Lưu ý:** Sau khi duyệt, `status` sẽ chuyển từ `DRAFT` → `DONE`, và tồn kho sẽ tự động tăng lên.

### Bước 3: Duyệt tất cả phiếu nhập (Script)

Nếu có nhiều phiếu, bạn có thể dùng script này:

**PowerShell:**
```powershell
$token = "YOUR_JWT_TOKEN"
$baseUrl = "http://localhost:8080/api/inventory/receipts"

# Lấy danh sách phiếu DRAFT
$response = Invoke-RestMethod -Uri "$baseUrl?status=DRAFT" -Headers @{
    "Authorization" = "Bearer $token"
}

# Duyệt từng phiếu
foreach ($receipt in $response.content) {
    Write-Host "Đang duyệt phiếu: $($receipt.code) (ID: $($receipt.id))"
    Invoke-RestMethod -Uri "$baseUrl/$($receipt.id)/approve" -Method PUT -Headers @{
        "Authorization" = "Bearer $token"
    }
    Write-Host "✓ Đã duyệt phiếu: $($receipt.code)"
}
```

## Cách 2: Duyệt qua SQL (Không khuyến khích)

**⚠️ Cảnh báo:** Cách này bỏ qua business logic, có thể gây lỗi dữ liệu.

```sql
-- Chỉ cập nhật status, KHÔNG tự động tăng tồn kho
UPDATE inventory_receipts 
SET status = 'DONE' 
WHERE status = 'DRAFT';
```

**Sau đó phải tự động tăng tồn kho thủ công (phức tạp, không khuyến khích).**
