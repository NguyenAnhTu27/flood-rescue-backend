package com.floodrescue.module.user.dto.response.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateUserByAdminRequest {
    private Long roleId;
    private Long teamId; // nullable
    private String fullName;
    private String phone;
    private String email;
    private String password;
}