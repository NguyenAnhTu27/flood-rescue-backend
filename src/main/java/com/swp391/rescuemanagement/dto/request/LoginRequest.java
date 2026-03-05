package com.swp391.rescuemanagement.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * DTO đăng nhập — Java 21 Record
 */
public record LoginRequest(
        @NotBlank(message = "Email không được để trống") @Email(message = "Email không đúng định dạng") String email,

        @NotBlank(message = "Mật khẩu không được để trống") String password) {
}
