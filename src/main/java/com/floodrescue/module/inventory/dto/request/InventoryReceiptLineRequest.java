package com.floodrescue.module.inventory.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class InventoryReceiptLineRequest {

    @NotNull(message = "ID loại hàng không được để trống")
    @Positive(message = "ID loại hàng phải lớn hơn 0")
    private Integer itemCategoryId;

    @NotNull(message = "Số lượng không được để trống")
    @Positive(message = "Số lượng phải lớn hơn 0")
    private Double qty;

    @NotBlank(message = "Đơn vị tính không được để trống")
    @Size(max = 50, message = "Đơn vị tính không được vượt quá 50 ký tự")
    private String unit;

    /**
     * Tên mặt hàng cụ thể (optional - để FE hiển thị/validate).
     * Nếu không có, BE sẽ lấy từ ItemCategory theo itemCategoryId.
     */
    @Size(max = 255, message = "Tên mặt hàng không được vượt quá 255 ký tự")
    private String itemName;
}

