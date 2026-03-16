package com.floodrescue.module.auth.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ForgotPasswordResponse {
    private String message;
    private String resetToken;
    private Integer expiresInMinutes;
}
