package com.swp391.rescuemanagement.controller;

import com.swp391.rescuemanagement.dto.request.LoginRequest;
import com.swp391.rescuemanagement.dto.response.ApiResponse;
import com.swp391.rescuemanagement.dto.response.UserResponse;
import com.swp391.rescuemanagement.model.User;
import com.swp391.rescuemanagement.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /**
     * POST /api/auth/login
     * Body: LoginRequest { email, password }
     *
     * 200 → Đăng nhập thành công
     * 401 → Sai thông tin
     * 409 → Session đã tồn tại (đã đăng nhập)
     */
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<UserResponse>> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletRequest httpRequest) {

        User user = authService.login(request.email(), request.password(), httpRequest);
        return ResponseEntity.ok(
                ApiResponse.ok(UserResponse.from(user), "Đăng nhập thành công"));
    }

    /**
     * POST /api/auth/logout
     * 200 → Đăng xuất thành công
     */
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(HttpServletRequest request) {
        authService.logout(request);
        return ResponseEntity.ok(ApiResponse.ok(null, "Đăng xuất thành công"));
    }

    /**
     * GET /api/auth/me
     * 200 → Thông tin user hiện tại
     * 401 → Chưa đăng nhập
     */
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserResponse>> me(HttpServletRequest request) {
        User user = authService.getCurrentUser(request);
        return ResponseEntity.ok(ApiResponse.ok(UserResponse.from(user)));
    }
}
