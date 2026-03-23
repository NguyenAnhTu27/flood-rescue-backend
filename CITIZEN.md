# Trieu_Demo_CITIZEN

**Phạm vi role:** CITIZEN (Công dân)

**Routes UI đang dùng**
1. `/cong-dan` -> `CItizenDashboard.jsx`
2. `/cong-dan/tao-yeu-cau-cuu-ho` -> `RescueRequestCreatePage.jsx`
3. `/cong-dan/yeu-cau-cuu-ho` -> `MyRescueRequestsPage.jsx`
4. `/cong-dan/trang-thai-cuu-ho` -> `RescueRequestStatusPage.jsx`
5. `/cong-dan/cap-nhat-yeu-cau` -> `RescueRequestUpdatePage.jsx`
6. `/cong-dan/phan-hoi` -> `FeedbackPage.jsx`

**Bảng DB liên quan thường gặp**
1. `users`
2. `rescue_requests`
3. `rescue_request_timeline`
4. `rescue_request_attachments`
5. `notifications`
6. `system_feedbacks`

**Trạng thái liên quan**
1. `RescueRequestStatus`: `PENDING`, `VERIFIED`, `ASSIGNED`, `IN_PROGRESS`, `COMPLETED`, `CANCELLED`, `DUPLICATE`

---

**1) Dashboard Công dân (`/cong-dan`)**

**Nút: `Đã được cứu hộ`**
1. API: `POST /api/rescue/citizen/requests/{id}/confirm-result` với `rescued=true`.
2. DB: cập nhật `rescue_requests.rescue_result_confirmation_status='RESCUED'`, `rescue_result_confirmation_note`, `rescue_result_confirmed_at`.
3. DB: thêm dòng `rescue_request_timeline` (NOTE) nội dung “Citizen xác nhận: đã được cứu hộ an toàn.”
4. UI điều hướng sang trang đánh giá (`/cong-dan/phan-hoi`).

**Nút: `Chưa được cứu hộ`**
1. API: `POST /api/rescue/citizen/requests/{id}/confirm-result` với `rescued=false`, kèm `reason`.
2. DB: cập nhật `rescue_requests.rescue_result_confirmation_status='NOT_RESCUED'`, `rescue_result_confirmation_note`, `rescue_result_confirmed_at`.
3. DB: thêm dòng `rescue_request_timeline` (NOTE) “Citizen phản hồi cứu hộ chưa thành công: ...”.
4. DB: tạo bản ghi mới `rescue_requests` (status `PENDING`, priority `HIGH`), sao chép dữ liệu địa chỉ, mô tả, tọa độ.
5. DB: sao chép `rescue_request_attachments` sang yêu cầu mới (nếu có).
6. DB: thêm `rescue_request_timeline` (STATUS_CHANGE) cho yêu cầu mới.
7. DB: tạo `notifications` cho role `COORDINATOR` (loại `CITIZEN_REOPEN_AFTER_FAILED_RESCUE`).

**Nút: `Gửi lại yêu cầu`** (khi yêu cầu bị hủy)
1. API: `POST /api/rescue/citizen/requests/{id}/reopen`.
2. DB: cập nhật `rescue_requests.status` về `PENDING` (tùy điều kiện) hoặc tạo lại theo logic reopen.
3. DB: thêm dòng `rescue_request_timeline` với nội dung yêu cầu được gửi lại.
4. DB: tạo `notifications` cho điều phối viên nếu service có phát thông báo reopen.

**Nút: `Tạo yêu cầu cứu hộ`**
1. Điều hướng sang `/cong-dan/tao-yeu-cau-cuu-ho`.
2. Không ghi DB.

**Nút: `Xem yêu cầu đã tạo`**
1. Điều hướng sang `/cong-dan/yeu-cau-cuu-ho`.
2. Không ghi DB.

---

**2) Tạo yêu cầu cứu hộ (`/cong-dan/tao-yeu-cau-cuu-ho`)**

**Nút: `Lấy vị trí GPS của tôi`**
1. Lấy GPS từ trình duyệt và reverse-geocode.
2. Không ghi DB.

**Nút: `Gửi yêu cầu cứu hộ`**
1. API upload ảnh: `POST /api/rescue/citizen/attachments` (nếu có ảnh).
2. API tạo yêu cầu: `POST /api/rescue/citizen/requests`.
3. DB: thêm dòng `rescue_requests` với `status=PENDING`, `priority`, `affected_people_count`, `address_text`, `latitude/longitude`, `location_description`.
4. DB: thêm các dòng `rescue_request_attachments` theo file đã upload.
5. DB: thêm `rescue_request_timeline` (STATUS_CHANGE) “Yêu cầu cứu hộ được tạo”.
6. DB: thêm `notifications` tới `COORDINATOR` (type `CITIZEN_NEW_REQUEST`).
7. UI điều hướng về `/cong-dan` và hiển thị thông báo thành công.

---

**3) Danh sách yêu cầu cứu hộ (`/cong-dan/yeu-cau-cuu-ho`)**

**Nút: `Thử lại`**
1. API: `GET /api/rescue/citizen/requests`.
2. Không ghi DB.

**Nút: `Tạo yêu cầu mới`**
1. Điều hướng `/cong-dan/tao-yeu-cau-cuu-ho`.
2. Không ghi DB.

**Click vào một thẻ yêu cầu**
1. Điều hướng `/cong-dan/trang-thai-cuu-ho` với `requestId`.
2. Không ghi DB.

---

**4) Trạng thái yêu cầu cứu hộ (`/cong-dan/trang-thai-cuu-ho`)**

**Nút: `Cập nhật yêu cầu`**
1. Điều hướng `/cong-dan/cap-nhat-yeu-cau`.
2. Không ghi DB.

**Nút: `Hủy yêu cầu`**
1. API: `DELETE /api/rescue/citizen/requests/{id}` (fallback `POST /api/rescue/requests/{id}/cancel`).
2. DB: cập nhật `rescue_requests.status=CANCELLED`.
3. DB: thêm `rescue_request_timeline` (CANCEL) “Yêu cầu cứu hộ được hủy bởi người tạo”.

**Nút: `Đã được cứu hộ` / `Chưa được cứu hộ`**
1. Giống luồng xác nhận ở Dashboard.

**Nút: `Gửi lại yêu cầu`** (khi bị hủy)
1. API: `POST /api/rescue/citizen/requests/{id}/reopen`.
2. DB: cập nhật hoặc tạo lại `rescue_requests` theo logic reopen.
3. DB: thêm `rescue_request_timeline`.

**Nút: `Đánh giá hệ thống`**
1. Điều hướng `/cong-dan/phan-hoi`.
2. Không ghi DB.

---

**5) Cập nhật yêu cầu cứu hộ (`/cong-dan/cap-nhat-yeu-cau`)**

**Nút: `Cập nhật yêu cầu`**
1. API upload ảnh (nếu có): `POST /api/rescue/citizen/attachments`.
2. API cập nhật: `PUT /api/rescue/citizen/requests/{id}`.
3. DB: cập nhật `rescue_requests` (mô tả, địa chỉ, tọa độ, ưu tiên, số người).
4. DB: nếu có ảnh mới thì thêm `rescue_request_attachments`.
5. DB: thêm `rescue_request_timeline` (NOTE) “Yêu cầu cứu hộ được cập nhật”.

**Nút: `Hủy` / `Quay lại`**
1. Điều hướng về danh sách hoặc trạng thái.
2. Không ghi DB.

---

**6) Đánh giá hệ thống (`/cong-dan/phan-hoi`)**

**Nút: `Gửi đánh giá`**
1. API: `POST /api/feedback/citizen`.
2. DB: thêm `system_feedbacks` (citizen_id, request_id nếu có, rating, rescued_confirmed/relief_confirmed, comment).
3. UI điều hướng về `/cong-dan`.

**Nút: `Quay lại trang chủ`**
1. Điều hướng `/cong-dan`.
2. Không ghi DB.
