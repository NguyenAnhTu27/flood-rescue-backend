package com.floodrescue.module.admin.controller;


import com.floodrescue.module.user.dto.response.request.CreateUserByAdminRequest;
import com.floodrescue.module.user.entity.RoleEntity;
import com.floodrescue.module.user.entity.UserEntity;
import com.floodrescue.module.user.repository.RoleRepository;
import com.floodrescue.module.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

        import java.time.LocalDateTime;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminUserController {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    // ===============================
    // 1️⃣ TẠO USER
    // ===============================
    @PostMapping("/create-user")
    public ResponseEntity<?> createUser(@RequestBody CreateUserByAdminRequest request) {

        if (userRepository.existsByEmail(request.getEmail())) {
            return ResponseEntity.badRequest().body("Email đã tồn tại");
        }

        if (request.getPhone() != null && userRepository.existsByPhone(request.getPhone())) {
            return ResponseEntity.badRequest().body("Số điện thoại đã tồn tại");
        }
        RoleEntity role = roleRepository.findById(request.getRoleId()).orElse(null);

        if (role == null) {
            return ResponseEntity.badRequest().body("RoleId không tồn tại");
        }
        if ("ADMIN".equals(role.getCode())) {
            return ResponseEntity.badRequest().body("Không được tạo ADMIN mới");
        }

        UserEntity user = UserEntity.builder()
                .fullName(request.getFullName())
                .email(request.getEmail())
                .phone(request.getPhone())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .role(role)
                .teamId(request.getTeamId())
                .status((byte) 1)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        userRepository.save(user);

        return ResponseEntity.ok("Tạo tài khoản thành công");
    }

    // ===============================
    // 2️⃣ XEM USER CHIA THEO ROLE
    // ===============================
    @GetMapping("/users-by-role")
    public ResponseEntity<?> getUsersByRole() {

        var roles = roleRepository.findAll();

        var result = roles.stream()
                .collect(Collectors.toMap(
                        RoleEntity::getCode,
                        role -> userRepository.findByRole(role)
                ));

        return ResponseEntity.ok(result);
    }

    // ===============================
// 3️⃣ XOÁ USER
// ===============================
    @DeleteMapping("/delete-user/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable Long id) {

        var userOptional = userRepository.findById(id);

        if (userOptional.isEmpty()) {
            return ResponseEntity.badRequest().body("User không tồn tại");
        }

        UserEntity user = userOptional.get();

        if (user.getRole() != null && "ADMIN".equals(user.getRole().getCode())) {
            return ResponseEntity.badRequest().body("Không được xoá ADMIN");
        }

        userRepository.deleteById(id);

        return ResponseEntity.ok("Đã xoá user thành công");
    }

    // ===============================
// 4️⃣ RESET PASSWORD
// ===============================
    @PutMapping("/reset-password/{id}")
    public ResponseEntity<?> resetPassword(
            @PathVariable Long id,
            @RequestBody Map<String, String> body
    ) {

        var userOptional = userRepository.findById(id);

        if (userOptional.isEmpty()) {
            return ResponseEntity.badRequest().body("User không tồn tại");
        }

        UserEntity user = userOptional.get();

        if ("ADMIN".equals(user.getRole().getCode())) {
            return ResponseEntity.badRequest().body("Không được reset ADMIN");
        }

        String newPassword = body.get("password");

        user.setPasswordHash(passwordEncoder.encode(newPassword));
        user.setUpdatedAt(LocalDateTime.now());

        userRepository.save(user);

        return ResponseEntity.ok("Reset password thành công");
    }
}