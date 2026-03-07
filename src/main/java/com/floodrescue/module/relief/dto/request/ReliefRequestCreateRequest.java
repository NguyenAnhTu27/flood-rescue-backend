package com.floodrescue.module.relief.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class ReliefRequestCreateRequest {

    @NotBlank(message = "Khu vực cứu trợ không được để trống")
    private String targetArea;

    // Có thể null nếu tạo yêu cầu cứu trợ độc lập, không gắn với yêu cầu cứu nạn cụ thể
    private Long rescueRequestId;

    private String note;

    @NotEmpty(message = "Danh sách mặt hàng cứu trợ không được để trống")
    @Valid
    private List<ReliefRequestLineRequest> lines;
}

