package com.floodrescue.module.admin.service;

import com.floodrescue.module.admin.dto.AdminUserResponse;
import com.floodrescue.module.admin.dto.TeamOptionResponse;
import com.floodrescue.module.admin.dto.UpdateStatusRequest;
import com.floodrescue.module.admin.dto.UpdateUserInfoRequest;
import com.floodrescue.module.admin.repository.AuditLogRepository;
import com.floodrescue.module.team.repository.TeamRepository;
import com.floodrescue.module.user.dto.response.request.CreateUserAdminRequest;
import com.floodrescue.module.user.entity.RoleEntity;
import com.floodrescue.module.user.entity.UserEntity;
import com.floodrescue.module.user.repository.RoleRepository;
import com.floodrescue.module.user.repository.UserRepository;
import com.floodrescue.shared.exception.BusinessException;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AdminUserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final TeamRepository teamRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuditLogService auditLogService;
    private final AuditLogRepository auditLogRepository;

    // ===============================
    // 1️⃣ CREATE USER
    // ===============================
    public String createUser(CreateUserAdminRequest request){

        if(userRepository.existsByEmail(request.getEmail()))
            throw new BusinessException("Email đã tồn tại");

        if(request.getPhone()!=null && userRepository.existsByPhone(request.getPhone()))
            throw new BusinessException("Số điện thoại đã tồn tại");

        RoleEntity role = roleRepository.findById(request.getRoleId())
                .orElseThrow(() -> new BusinessException("RoleId không tồn tại"));

        if("ADMIN".equals(role.getCode()))
            throw new BusinessException("Không được tạo ADMIN mới");

        UserEntity user = UserEntity.builder()
                .fullName(request.getFullName())
                .email(request.getEmail())
                .phone(request.getPhone())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .role(role)
                .status((byte)1)
                .failedLoginAttempts(0)
                .lockedAt(null)
                .tempLockedUntil(null)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        userRepository.save(user);
        auditLogService.log("CREATE_USER", "user:" + user.getEmail(), "SUCCESS", "Role = " + role.getCode());

        return "Tạo tài khoản thành công";
    }


    // ===============================
    // 2️⃣ SEARCH USER
    // ===============================
    @Transactional(readOnly = true)
    public Map<String,Object> searchUsers(String keyword, Long roleId, int page){

        Pageable pageable = PageRequest.of(page,20);

        Page<UserEntity> users =
                userRepository.searchUsers(keyword,roleId,pageable);

        var result = users.stream()
                .map(user -> AdminUserResponse.builder()
                        .id(user.getId())
                        .fullName(user.getFullName())
                        .email(user.getEmail())
                        .phone(user.getPhone())
                        .role(user.getRole().getCode())
                        .roleId(user.getRole().getId())
                        .teamId(user.getTeamId())
                        .status(user.getStatus()==1 ? "ACTIVE":"LOCKED")
                        .createdAt(user.getCreatedAt())
                        .build())
                .toList();

        return Map.of(
                "totalUsers",users.getTotalElements(),
                "totalPages",users.getTotalPages(),
                "users",result
        );
    }

    @Transactional
    public String updateUser(Long id, UpdateUserInfoRequest request) {
        UserEntity user = userRepository.findById(id)
                .orElseThrow(() -> new BusinessException("User không tồn tại"));

        if (request.getRoleId() == null) {
            throw new BusinessException("RoleId không được để trống");
        }

        RoleEntity role = roleRepository.findById(request.getRoleId())
                .orElseThrow(() -> new BusinessException("RoleId không tồn tại"));

        String email = request.getEmail() == null ? null : request.getEmail().trim().toLowerCase();
        String phone = request.getPhone() == null ? null : request.getPhone().trim();

        if (email != null && !email.isBlank() && userRepository.existsByEmailAndIdNot(email, id)) {
            throw new BusinessException("Email đã tồn tại");
        }
        if (phone != null && !phone.isBlank() && userRepository.existsByPhoneAndIdNot(phone, id)) {
            throw new BusinessException("Số điện thoại đã tồn tại");
        }

        if (request.getFullName() == null || request.getFullName().isBlank()) {
            throw new BusinessException("Họ tên không được để trống");
        }

        user.setFullName(request.getFullName().trim());
        user.setEmail(email);
        user.setPhone(phone);
        user.setRole(role);
        user.setTeamId(request.getTeamId());
        if ("LOCKED".equalsIgnoreCase(request.getStatus())) {
            user.setStatus((byte) 0);
            user.setTempLockedUntil(null);
        } else {
            user.setStatus((byte) 1);
            user.setFailedLoginAttempts(0);
            user.setLockedAt(null);
            user.setTempLockedUntil(null);
        }
        user.setUpdatedAt(LocalDateTime.now());

        userRepository.save(user);
        auditLogService.log("UPDATE_USER", "user:" + id, "SUCCESS", "Updated profile");
        return "Cập nhật người dùng thành công";
    }


    // ===============================
    // 3️⃣ DELETE USER
    // ===============================
    @Transactional
    public String deleteUser(Long id){

        UserEntity user = userRepository.findById(id)
                .orElseThrow(() -> new BusinessException("User không tồn tại"));

        if(user.getRole()!=null && "ADMIN".equals(user.getRole().getCode()))
            throw new BusinessException("Không được xoá ADMIN");

        try {
            // Remove dependent audit logs first to satisfy FK fk_audit_actor.
            auditLogRepository.deleteByActorId(id);
            userRepository.deleteById(id);
            auditLogService.log("DELETE_USER", "user:" + id, "WARN", "Deleted by admin");
        } catch (DataIntegrityViolationException ex) {
            throw new BusinessException("Không thể xoá user vì còn dữ liệu liên quan trong hệ thống");
        }

        return "Đã xoá user thành công";
    }


    // ===============================
    // 4️⃣ RESET PASSWORD
    // ===============================
    public String resetPassword(Long id,String password){

        UserEntity user = userRepository.findById(id)
                .orElseThrow(() -> new BusinessException("User không tồn tại"));

        if("ADMIN".equals(user.getRole().getCode()))
            throw new BusinessException("Không được reset ADMIN");

        user.setPasswordHash(passwordEncoder.encode(password));
        user.setUpdatedAt(LocalDateTime.now());

        userRepository.save(user);
        auditLogService.log("RESET_PASSWORD", "user:" + id, "WARN", "Password reset by admin");

        return "Reset password thành công";
    }


    // ===============================
    // 5️⃣ UPDATE STATUS
    // ===============================
    public String updateStatus(Long id, UpdateStatusRequest request){

        UserEntity user = userRepository.findById(id)
                .orElseThrow(() -> new BusinessException("User không tồn tại"));

        if(request.getStatus().equals("ACTIVE"))
            user.setStatus((byte)1);
        else
            user.setStatus((byte)0);

        if (user.getStatus() == 1) {
            user.setFailedLoginAttempts(0);
            user.setLockedAt(null);
            user.setTempLockedUntil(null);
        } else {
            user.setTempLockedUntil(null);
        }

        user.setUpdatedAt(LocalDateTime.now());

        userRepository.save(user);
        auditLogService.log("UPDATE_STATUS", "user:" + id, "SUCCESS", "New status = " + request.getStatus());

        return "Status updated";
    }


    // ===============================
    // 6️⃣ SYSTEM STATS
    // ===============================
    public Map<String,Object> getStats(){

        long total = userRepository.count();
        long active = userRepository.countByStatus((byte)1);
        long locked = userRepository.countByStatus((byte)0);

        return Map.of(
                "totalUsers",total,
                "activeUsers",active,
                "lockedUsers",locked
        );
    }

    @Transactional(readOnly = true)
    public List<TeamOptionResponse> getTeams() {
        return teamRepository.findAll().stream()
                .map(team -> TeamOptionResponse.builder()
                        .id(team.getId())
                        .name(team.getName())
                        .build())
                .toList();
    }
}
