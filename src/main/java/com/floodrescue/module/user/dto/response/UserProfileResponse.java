package com.floodrescue.module.user.dto.response;

import lombok.*;

import java.time.LocalDateTime;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class UserProfileResponse {
    private Long id;
    private String fullName;
    private String phone;
    private String email;
    private String role;
    private String roleName;
    private String status;
    private Long teamId;
    private Boolean isLeader;
    private Boolean rescueRequestBlocked;
    private String rescueRequestBlockedReason;
    private LocalDateTime lastLoginAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
