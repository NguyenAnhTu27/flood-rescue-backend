package com.floodrescue.module.admin.service;

import com.floodrescue.module.admin.dto.request.UpdateRolePermissionsRequest;
import com.floodrescue.module.admin.dto.response.PermissionMatrixResponse;
import com.floodrescue.module.admin.entity.PermissionEntity;
import com.floodrescue.module.admin.entity.RolePermissionEntity;
import com.floodrescue.module.admin.repository.PermissionRepository;
import com.floodrescue.module.admin.repository.RolePermissionRepository;
import com.floodrescue.module.user.entity.RoleEntity;
import com.floodrescue.module.user.repository.RoleRepository;
import com.floodrescue.module.user.repository.UserRepository;
import com.floodrescue.shared.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PermissionService {

    private final PermissionRepository permissionRepository;
    private final RolePermissionRepository rolePermissionRepository;
    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final AuditLogService auditLogService;

    @Transactional(readOnly = true)
    public PermissionMatrixResponse getPermissionMatrix() {
        List<PermissionEntity> permissions = permissionRepository.findAllByOrderByIdAsc();
        List<RoleEntity> roles = roleRepository.findAll().stream()
                .sorted(Comparator.comparing(RoleEntity::getId))
                .toList();

        List<PermissionMatrixResponse.RoleItem> roleItems = roles.stream()
                .map(role -> PermissionMatrixResponse.RoleItem.builder()
                        .id(role.getId())
                        .code(role.getCode())
                        .name(role.getName())
                        .userCount(userRepository.countByRole_Id(role.getId()))
                        .build())
                .toList();

        Map<String, List<String>> rolePermissions = new LinkedHashMap<>();
        for (RoleEntity role : roles) {
            List<String> codes = rolePermissionRepository.findByRole_Code(role.getCode()).stream()
                    .map(rp -> rp.getPermission().getCode())
                    .sorted()
                    .toList();
            rolePermissions.put(role.getCode(), codes);
        }

        List<PermissionMatrixResponse.PermissionItem> items = permissions.stream()
                .map(p -> PermissionMatrixResponse.PermissionItem.builder()
                        .code(p.getCode())
                        .name(p.getName())
                        .module(p.getModule())
                        .description(p.getDescription())
                        .build())
                .toList();

        return PermissionMatrixResponse.builder()
                .roles(roleItems)
                .permissions(items)
                .rolePermissions(rolePermissions)
                .build();
    }

    @Transactional(readOnly = true)
    public List<String> getPermissionsByRole(String roleCode) {
        return rolePermissionRepository.findByRole_Code(roleCode.toUpperCase()).stream()
                .map(rp -> rp.getPermission().getCode())
                .sorted()
                .toList();
    }

    @Transactional
    public String updateRolePermissions(String roleCode, UpdateRolePermissionsRequest request) {
        if (request == null || request.getPermissions() == null) {
            throw new BusinessException("Danh sách quyền không được để trống");
        }

        RoleEntity role = roleRepository.findByCode(roleCode.toUpperCase())
                .orElseThrow(() -> new BusinessException("Role không tồn tại"));

        List<PermissionEntity> permissions = permissionRepository.findAllByOrderByIdAsc();
        Map<String, PermissionEntity> permissionMap = permissions.stream()
                .collect(Collectors.toMap(PermissionEntity::getCode, Function.identity()));

        Set<String> requestedCodes = request.getPermissions().stream()
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(s -> !s.isBlank())
                .collect(Collectors.toCollection(LinkedHashSet::new));

        for (String code : requestedCodes) {
            if (!permissionMap.containsKey(code)) {
                throw new BusinessException("Permission không tồn tại: " + code);
            }
        }

        rolePermissionRepository.deleteByRole_Id(role.getId());

        LocalDateTime now = LocalDateTime.now();
        List<RolePermissionEntity> newMappings = requestedCodes.stream()
                .map(code -> RolePermissionEntity.builder()
                        .roleId(role.getId())
                        .permissionId(permissionMap.get(code).getId())
                        .role(role)
                        .permission(permissionMap.get(code))
                        .createdAt(now)
                        .build())
                .toList();

        rolePermissionRepository.saveAll(newMappings);
        auditLogService.log("UPDATE_ROLE_PERMISSIONS", "role:" + role.getCode(), "SUCCESS",
                "Updated permissions count = " + newMappings.size());

        return "Cập nhật phân quyền thành công";
    }

    @Transactional
    public void ensureDefaultPermissions() {
        if (permissionRepository.count() > 0) {
            return;
        }

        LocalDateTime now = LocalDateTime.now();
        List<PermissionEntity> defaults = List.of(
                PermissionEntity.builder().code("user.read").name("Xem danh sách người dùng").module("USER").description("Read users").createdAt(now).build(),
                PermissionEntity.builder().code("user.create").name("Tạo người dùng").module("USER").description("Create users").createdAt(now).build(),
                PermissionEntity.builder().code("user.update").name("Cập nhật người dùng").module("USER").description("Update users").createdAt(now).build(),
                PermissionEntity.builder().code("user.delete").name("Xóa người dùng").module("USER").description("Delete users").createdAt(now).build(),
                PermissionEntity.builder().code("rescue.assign").name("Phân công cứu hộ").module("RESCUE").description("Assign rescue").createdAt(now).build(),
                PermissionEntity.builder().code("inventory.manage").name("Quản lý kho").module("INVENTORY").description("Manage inventory").createdAt(now).build(),
                PermissionEntity.builder().code("system.config").name("Cấu hình hệ thống").module("SYSTEM").description("Manage system settings").createdAt(now).build(),
                PermissionEntity.builder().code("audit.read").name("Xem nhật ký").module("SYSTEM").description("Read audit logs").createdAt(now).build()
        );

        permissionRepository.saveAll(defaults);
    }
}
