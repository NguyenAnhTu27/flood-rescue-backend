package com.floodrescue.module.admin.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AdminResetPasswordRequest {

    @NotBlank(message = "Mật khẩu mới không được để trống")
    @Size(min = 6, max = 72, message = "Mật khẩu phải có độ dài từ 6 đến 72 ký tự")
    @Pattern(
            regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).*$",
            message = "Mật khẩu phải chứa ít nhất một chữ hoa, một chữ thường và một chữ số"
    )
    private String password;
}
