package com.floodrescue.module.rescue.dto.response;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
public class RescuerDashboardResponse {
    private Long teamId;
    private String teamName;

    private Long activeTaskGroups;
    private Long activeAssignments;

    // Danh sách nhóm nhiệm vụ gần nhất/đang hoạt động của đội
    private List<TaskGroupResponse> taskGroups;
}

