# Postman Testing Guide

Base URL

http://localhost:8080/api

---

# 1. Admin Login

### Endpoint
POST /auth/login

### Description
Admin đăng nhập để lấy JWT token.
 Admin là mặc định theo ví dụ dưới
### Request Body
```json
{
  "email": "admin@gmail.com",
  "password": "admin123"
}
```

### Response
```json
{
  "token": "JWT_TOKEN"
}
```

### Postman Setup
Authorization → Bearer Token

```
JWT_TOKEN
```

---

# 2. Create User

### Endpoint
POST /admin/create-user

### Description
Admin tạo tài khoản mới (không cho tạo ADMIN).

### Headers
```
Authorization: Bearer JWT_TOKEN
Content-Type: application/json
```

### Request Body
```json
{
  "fullName": "Nguyen Van A",
  "email": "user@gmail.com",
  "phone": "0900000000",
  "password": "123456",
  "roleId": 2,
  "teamId": null
}
```

### Response
```
Tạo tài khoản thành công
```

---

# 3. Get Users By Role

### Endpoint
GET /admin/users-by-role

### Description
Lấy danh sách user và nhóm theo role.

### Headers
```
Authorization: Bearer JWT_TOKEN
```

### Response
```json
{
  "CITIZEN": [
    {
      "id": 3,
      "fullName": "Nguyen Van A",
      "email": "user@gmail.com"
    }
  ],
  "RESCUER": [],
  "COORDINATOR": [],
  "MANAGER": []
}
```

---

# 4. Delete User

### Endpoint
DELETE /admin/delete-user/{id}

### Description
Admin xoá user (không xoá ADMIN).

### Example
```
DELETE /admin/delete-user/5
```

### Headers
```
Authorization: Bearer JWT_TOKEN
```

### Response
```
Đã xoá user thành công
```

---

# 5. Reset Password

### Endpoint
PUT /admin/reset-password/{id}

### Description
Admin reset password cho user (không reset ADMIN).

### Example
```
PUT /admin/reset-password/5
```

### Headers
```
Authorization: Bearer JWT_TOKEN
Content-Type: application/json
```

### Request Body
```json
{
  "password": "newpassword123"
}
```

### Response
```
Reset password thành công
```

---

# Role Mapping

| Role Name | Role ID |
|----------|--------|
| CITIZEN | 2 |
| RESCUER | 3 |
| COORDINATOR | 4 |
| MANAGER | 5 |

---

# Postman Test Flow

1. Login → lấy JWT Token
2. Set Bearer Token trong Authorization
3. Test các API:

```
Create User
Get Users
Delete User
Reset Password
```