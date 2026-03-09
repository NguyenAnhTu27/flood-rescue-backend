package com.floodrescue.module.admin.dto.response;

import lombok.*;

import java.util.List;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PermissionMatrixResponse {
    private List<RoleItem> roles;
    private List<PermissionItem> permissions;
    private Map<String, List<String>> rolePermissions;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class RoleItem {
        private Long id;
        private String code;
        private String name;
        private long userCount;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class PermissionItem {
        private String code;
        private String name;
        private String module;
        private String description;
    }
}
