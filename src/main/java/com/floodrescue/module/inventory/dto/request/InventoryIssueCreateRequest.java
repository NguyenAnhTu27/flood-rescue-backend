package com.floodrescue.module.inventory.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class InventoryIssueCreateRequest {

    private String note;

    @NotEmpty(message = "Danh sách dòng phiếu xuất không được để trống")
    @Valid
    private List<InventoryIssueLineRequest> lines;
}

