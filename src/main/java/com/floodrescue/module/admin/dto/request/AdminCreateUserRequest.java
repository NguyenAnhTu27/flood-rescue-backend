package com.floodrescue.module.admin.dto.request;

import com.floodrescue.shared.util.PhoneUtil;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AdminCreateUserRequest {

    private static final java.util.regex.Pattern EMAIL_PATTERN = java.util.regex.Pattern.compile(
            "^[A-Za-z0-9+_.-]+@([A-Za-z0-9.-]+\\.[A-Za-z]{2,})$"
    );

    @NotBlank(message = "Họ và tên không được để trống")
    @Size(max = 120, message = "Họ và tên không được vượt quá 120 ký tự")
    private String fullName;

    @Size(max = 120, message = "Email không được vượt quá 120 ký tự")
    private String email;

    @Size(max = 20, message = "Số điện thoại không được vượt quá 20 ký tự")
    private String phone;

    @NotBlank(message = "Mật khẩu không được để trống")
    @Size(min = 6, max = 72, message = "Mật khẩu phải có độ dài từ 6 đến 72 ký tự")
    @Pattern(
            regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).*$",
            message = "Mật khẩu phải chứa ít nhất một chữ hoa, một chữ thường và một chữ số"
    )
    private String password;

    @NotNull(message = "roleId không được để trống")
    @Positive(message = "roleId phải lớn hơn 0")
    private Integer roleId;

    @Positive(message = "teamId phải lớn hơn 0")
    private Long teamId;

    @AssertTrue(message = "Cần nhập ít nhất email hoặc số điện thoại")
    public boolean isContactProvided() {
        return hasText(email) || hasText(phone);
    }

    @AssertTrue(message = "Email không hợp lệ")
    public boolean isEmailValidIfPresent() {
        if (!hasText(email)) {
            return true;
        }
        return EMAIL_PATTERN.matcher(email.trim()).matches();
    }

    @AssertTrue(message = "Số điện thoại không hợp lệ")
    public boolean isPhoneValidIfPresent() {
        if (!hasText(phone)) {
            return true;
        }
        return PhoneUtil.isValid(phone.trim());
    }

    private boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }
}
