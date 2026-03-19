package com.floodrescue.module.relief.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class ReliefRequestCreateRequest {

    @NotBlank(message = "Khu vực cứu trợ không được để trống")
    private String targetArea;

    @Size(max = 255, message = "Địa chỉ không được vượt quá 255 ký tự")
    private String addressText;

    @DecimalMin(value = "-90.0", message = "Vĩ độ phải lớn hơn hoặc bằng -90")
    @DecimalMax(value = "90.0", message = "Vĩ độ phải nhỏ hơn hoặc bằng 90")
    private Double latitude;

    @DecimalMin(value = "-180.0", message = "Kinh độ phải lớn hơn hoặc bằng -180")
    @DecimalMax(value = "180.0", message = "Kinh độ phải nhỏ hơn hoặc bằng 180")
    private Double longitude;

    @Size(max = 500, message = "Mô tả vị trí không được vượt quá 500 ký tự")
    private String locationDescription;

    // Có thể null nếu tạo yêu cầu cứu trợ độc lập, không gắn với yêu cầu cứu nạn cụ thể
    private Long rescueRequestId;

    @Size(max = 2000, message = "Ghi chú không được vượt quá 2000 ký tự")
    private String note;

    @Valid
    private List<ReliefRequestLineRequest> lines;
}
