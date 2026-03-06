package com.floodrescue.module.map.dto;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TeamLocationResponse {
    private Long teamId;
    private String code;
    private String name;
    private String status;
    private Double latitude;
    private Double longitude;
    private LocalDateTime lastLocationUpdate;
    private Double distanceKm;
}
