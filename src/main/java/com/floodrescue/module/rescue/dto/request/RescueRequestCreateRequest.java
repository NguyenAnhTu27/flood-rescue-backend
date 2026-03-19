package com.floodrescue.module.rescue.dto.request;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.floodrescue.shared.enums.AttachmentFileType;
import com.floodrescue.shared.enums.RescuePriority;
import com.floodrescue.shared.validation.ValidPhoneNumber;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class RescueRequestCreateRequest {

    @NotNull(message = "Số người bị ảnh hưởng không được để trống")
    @Min(value = 1, message = "Số người bị ảnh hưởng phải lớn hơn 0")
    @JsonAlias("peopleCount")
    private Integer affectedPeopleCount;

    @NotBlank(message = "Mô tả không được để trống")
    @Size(max = 2000, message = "Mô tả không được vượt quá 2000 ký tự")
    private String description;

    @ValidPhoneNumber(message = "Số điện thoại không hợp lệ")
    @Size(max = 20, message = "Số điện thoại không được vượt quá 20 ký tự")
    @JsonAlias("contactPhone")
    private String phone;

    @Size(max = 255, message = "Địa chỉ không được vượt quá 255 ký tự")
    private String addressText;

    private Double latitude;

    private Double longitude;

    @Size(max = 500, message = "Mô tả vị trí không được vượt quá 500 ký tự")
    private String locationDescription;

    @NotNull(message = "Mức độ ưu tiên không được để trống")
    @JsonAlias("urgency")
    private RescuePriority priority;

    private List<AttachmentRequest> attachments;

    @Getter
    @Setter
    public static class AttachmentRequest {
        @NotBlank(message = "URL file không được để trống")
        @Size(max = 500, message = "URL file không được vượt quá 500 ký tự")
        private String fileUrl;

        private AttachmentFileType fileType;
    }
}
