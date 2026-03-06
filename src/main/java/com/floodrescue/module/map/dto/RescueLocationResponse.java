package com.floodrescue.module.map.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RescueLocationResponse {
    private Long requestId;
    private String code;
    private String status;
    private String priority;
    private String addressText;
    private Double latitude;
    private Double longitude;
    private Integer affectedPeopleCount;
}
