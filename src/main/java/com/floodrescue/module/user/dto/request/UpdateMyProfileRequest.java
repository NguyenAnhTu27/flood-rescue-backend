package com.floodrescue.module.user.dto.request;

import com.floodrescue.shared.util.PhoneUtil;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateMyProfileRequest {

    private static final java.util.regex.Pattern EMAIL_PATTERN = java.util.regex.Pattern.compile(
            "^[A-Za-z0-9+_.-]+@([A-Za-z0-9.-]+\\.[A-Za-z]{2,})$"
    );

    @Size(min = 2, max = 120, message = "Họ và tên phải từ 2 đến 120 ký tự")
    private String fullName;

    @Size(max = 120, message = "Email không được vượt quá 120 ký tự")
    private String email;

    @Size(max = 20, message = "Số điện thoại không được vượt quá 20 ký tự")
    private String phone;

    @AssertTrue(message = "Họ và tên không được để trống")
    public boolean isFullNameValid() {
        return fullName != null && !fullName.trim().isEmpty();
    }

    @AssertTrue(message = "Email không hợp lệ")
    public boolean isEmailValid() {
        if (email == null || email.trim().isEmpty()) {
            return true;
        }
        return EMAIL_PATTERN.matcher(email.trim()).matches();
    }

    @AssertTrue(message = "Số điện thoại không hợp lệ")
    public boolean isPhoneValid() {
        if (phone == null || phone.trim().isEmpty()) {
            return true;
        }
        return PhoneUtil.isValid(phone.trim());
    }
}
