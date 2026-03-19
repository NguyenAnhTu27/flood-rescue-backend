package com.floodrescue.module.admin.dto.request;

import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AdminContentPagesUpdateRequest {

    @Size(max = 255, message = "Tiêu đề điều khoản không được vượt quá 255 ký tự")
    private String termsTitle;

    @Size(max = 20000, message = "Nội dung điều khoản không được vượt quá 20000 ký tự")
    private String termsContent;

    @Size(max = 100, message = "Nhãn điều khoản không được vượt quá 100 ký tự")
    private String termsLabel;

    @Size(max = 255, message = "Tiêu đề chính sách không được vượt quá 255 ký tự")
    private String privacyTitle;

    @Size(max = 20000, message = "Nội dung chính sách không được vượt quá 20000 ký tự")
    private String privacyContent;

    @Size(max = 100, message = "Nhãn chính sách không được vượt quá 100 ký tự")
    private String privacyLabel;

    @Size(max = 255, message = "Tiêu đề hỗ trợ không được vượt quá 255 ký tự")
    private String supportTitle;

    @Size(max = 20000, message = "Nội dung hỗ trợ không được vượt quá 20000 ký tự")
    private String supportContent;

    @Size(max = 100, message = "Nhãn hỗ trợ không được vượt quá 100 ký tự")
    private String supportLabel;

    @Size(max = 255, message = "URL điều khoản không được vượt quá 255 ký tự")
    private String footerTermsUrl;

    @Size(max = 255, message = "URL chính sách không được vượt quá 255 ký tự")
    private String footerPrivacyUrl;

    @Size(max = 255, message = "URL hỗ trợ không được vượt quá 255 ký tự")
    private String footerSupportUrl;
}
