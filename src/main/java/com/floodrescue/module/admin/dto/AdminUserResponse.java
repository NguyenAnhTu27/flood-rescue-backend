package com.floodrescue.module.admin.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class AdminUserResponse {

    private Long id;

    private String fullName;

    private String email;

    private String phone;

    private String role;

    private Long roleId;

    private Long teamId;

    private String status;

    private LocalDateTime createdAt;

}
