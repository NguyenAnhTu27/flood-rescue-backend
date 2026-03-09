package com.floodrescue.module.admin.controller;

import com.floodrescue.module.user.dto.response.request.CreateUserAdminRequest;
import com.floodrescue.module.admin.dto.UpdateStatusRequest;
import com.floodrescue.module.admin.dto.UpdateUserInfoRequest;
import com.floodrescue.module.admin.service.AdminUserService;
import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminUserController {

    private final AdminUserService adminUserService;

    // ===============================
    // 1️⃣ CREATE USER
    // ===============================
    @PostMapping("/create-user")
    public ResponseEntity<?> createUser(
            @Valid @RequestBody CreateUserAdminRequest request){

        return ResponseEntity.ok(
                adminUserService.createUser(request)
        );
    }

    // ===============================
    // 2️⃣ SEARCH USER
    // ===============================
    @GetMapping("/users")
    public ResponseEntity<?> searchUsers(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long roleId,
            @RequestParam(defaultValue = "0") int page){

        return ResponseEntity.ok(
                adminUserService.searchUsers(keyword, roleId, page)
        );
    }

    // ===============================
    // 3️⃣ DELETE USER
    // ===============================
    @DeleteMapping("/users/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable Long id){

        return ResponseEntity.ok(
                adminUserService.deleteUser(id)
        );
    }

    // ===============================
    // 4️⃣ RESET PASSWORD
    // ===============================
    @PutMapping("/users/{id}/reset-password")
    public ResponseEntity<?> resetPassword(
            @PathVariable Long id,
            @RequestBody Map<String,String> body){

        String password = body.get("password");

        return ResponseEntity.ok(
                adminUserService.resetPassword(id, password)
        );
    }

    // ===============================
    // 5️⃣ UPDATE STATUS
    // ===============================
    @PutMapping("/users/{id}/status")
    public ResponseEntity<?> updateStatus(
            @PathVariable Long id,
            @RequestBody UpdateStatusRequest request){

        return ResponseEntity.ok(
                adminUserService.updateStatus(id, request)
        );
    }

    @PutMapping("/users/{id}")
    public ResponseEntity<?> updateUser(
            @PathVariable Long id,
            @RequestBody UpdateUserInfoRequest request) {
        return ResponseEntity.ok(adminUserService.updateUser(id, request));
    }

    // ===============================
    // 6️⃣ SYSTEM STATS
    // ===============================
    @GetMapping("/stats")
    public ResponseEntity<?> stats(){

        return ResponseEntity.ok(
                adminUserService.getStats()
        );
    }

    @GetMapping("/team-options")
    public ResponseEntity<?> teams() {
        return ResponseEntity.ok(adminUserService.getTeams());
    }
}

