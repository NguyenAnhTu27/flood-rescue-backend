package com.floodrescue.module.relief.dto.request;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.floodrescue.shared.enums.RescuePriority;
import com.floodrescue.shared.validation.ValidPhoneNumber;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
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

    @ValidPhoneNumber(message = "Số điện thoại không hợp lệ")
    @Size(max = 20, message = "Số điện thoại không được vượt quá 20 ký tự")
    private String phone;

    @JsonAlias("urgency")
    private RescuePriority priority;

    @Min(value = 1, message = "Số người cần hỗ trợ phải lớn hơn 0")
    @JsonAlias("affectedPeopleCount")
    private Integer peopleCount;

    private String addressText;

    private Double latitude;

    private Double longitude;

    private String locationDescription;

    // Có thể null nếu tạo yêu cầu cứu trợ độc lập, không gắn với yêu cầu cứu nạn cụ thể
    private Long rescueRequestId;

    private String note;

    @Valid
    private List<ReliefRequestLineRequest> lines;
}
