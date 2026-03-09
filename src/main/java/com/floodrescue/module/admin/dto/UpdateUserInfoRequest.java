package com.floodrescue.module.admin.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateUserInfoRequest {
    private String fullName;
    private String email;
    private String phone;
    private Long roleId;
    private Long teamId;
    private String status;
}
