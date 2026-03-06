package com.floodrescue.module.user.dto.response.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateUserAdminRequest {
    @NotBlank(message = "Họ tên không được để trống")
    @Size(min = 2, max = 120, message = "Họ tên phải từ 2 đến 120 ký tự")
    private String fullName;

    @NotBlank(message = "Email không được để trống")
    @Email(message = "Email không đúng định dạng")
    @Size(max = 120, message = "Email không được vượt quá 120 ký tự")
    private String email;

    @NotBlank(message = "Số điện thoại không được để trống")
    @Pattern(
            regexp = "^(0[3-9]\\d{8}|\\+84[3-9]\\d{8})$",
            message = "Số điện thoại phải là 10 số (bắt đầu 03-09) hoặc định dạng +84"
    )
    private String phone;

    @NotBlank(message = "Mật khẩu không được để trống")
    @Pattern(
            regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^A-Za-z\\d]).{8,72}$",
            message = "Mật khẩu phải 8-72 ký tự, gồm chữ hoa, chữ thường, số và ký tự đặc biệt"
    )
    private String password;

    @NotNull(message = "Vai trò không được để trống")
    private Long roleId;

    private Byte status;


}
