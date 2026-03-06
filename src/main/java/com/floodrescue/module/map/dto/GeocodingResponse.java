package com.floodrescue.module.map.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GeocodingResponse {
    private Double latitude;
    private Double longitude;
    private String placeName;
}
