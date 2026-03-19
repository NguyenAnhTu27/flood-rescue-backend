package com.floodrescue.module.rescue.dto.request;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class VerifyRequest {

    @NotNull(message = "Trạng thái xác minh không được để trống")
    private Boolean locationVerified;

    @Size(max = 2000, message = "Ghi chú không được vượt quá 2000 ký tự")
    private String note;

    private Boolean cancelRequest;

    // DELETE: hủy khỏi hệ thống xử lý, WAITING_TEAM: đưa lại hàng đợi với nhãn chờ đội
    @Pattern(
            regexp = "^(DELETE|WAITING_TEAM)?$",
            message = "Hành động hủy chỉ hỗ trợ DELETE hoặc WAITING_TEAM"
    )
    private String cancelAction;

    @Size(max = 1000, message = "Lý do hủy không được vượt quá 1000 ký tự")
    private String cancelReason;
}
