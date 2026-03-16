package com.floodrescue.module.auth.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UserProfileResponse {
    private Long id;
    private String fullName;
    private String phone;
    private String email;
    private String role;
    private boolean rescueRequestBlocked;
    private String rescueRequestBlockedReason;
}
