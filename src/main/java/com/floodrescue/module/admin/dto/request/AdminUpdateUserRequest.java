package com.floodrescue.module.admin.dto.request;

import com.floodrescue.shared.util.PhoneUtil;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AdminUpdateUserRequest {

    private static final java.util.regex.Pattern EMAIL_PATTERN = java.util.regex.Pattern.compile(
            "^[A-Za-z0-9+_.-]+@([A-Za-z0-9.-]+\\.[A-Za-z]{2,})$"
    );

    @Size(max = 120, message = "Họ và tên không được vượt quá 120 ký tự")
    private String fullName;

    @Size(max = 120, message = "Email không được vượt quá 120 ký tự")
    private String email;

    @Size(max = 20, message = "Số điện thoại không được vượt quá 20 ký tự")
    private String phone;

    @Positive(message = "roleId phải lớn hơn 0")
    private Integer roleId;

    @Pattern(regexp = "(?i)^(ACTIVE|LOCKED)?$", message = "Trạng thái chỉ hỗ trợ ACTIVE hoặc LOCKED")
    private String status;

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

    @AssertTrue(message = "Họ và tên không được để trống")
    public boolean isFullNameValidIfPresent() {
        return fullName == null || !fullName.trim().isEmpty();
    }

    private boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }
}
