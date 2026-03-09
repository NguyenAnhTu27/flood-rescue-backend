package com.floodrescue.module.admin.dto.response;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuditLogResponse {
    private Long id;
    private String action;
    private String actor;
    private String target;
    private String level;
    private String detail;
    private LocalDateTime createdAt;
}
