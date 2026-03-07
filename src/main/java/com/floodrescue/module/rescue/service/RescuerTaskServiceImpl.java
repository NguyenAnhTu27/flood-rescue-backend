package com.floodrescue.module.rescue.service;

import com.floodrescue.module.rescue.dto.response.RescuerDashboardResponse;
import com.floodrescue.module.rescue.dto.response.TaskGroupResponse;
import com.floodrescue.module.rescue.entity.RescueAssigmentEntity;
import com.floodrescue.module.rescue.entity.TaskGroupEntity;
import com.floodrescue.module.rescue.entity.TaskGroupRequestEntity;
import com.floodrescue.module.rescue.entity.TaskGroupTimelineEntity;
import com.floodrescue.module.rescue.mapper.TaskGroupMapper;
import com.floodrescue.module.rescue.repository.RescueAssignmentRepository;
import com.floodrescue.module.rescue.repository.TaskGroupRepository;
import com.floodrescue.module.rescue.repository.TaskGroupRequestRepository;
import com.floodrescue.module.rescue.repository.TaskGroupTimelineRepository;
import com.floodrescue.module.team.entity.TeamEntity;
import com.floodrescue.module.team.repository.TeamRepository;
import com.floodrescue.module.user.entity.UserEntity;
import com.floodrescue.module.user.repository.UserRepository;
import com.floodrescue.shared.enums.TaskGroupStatus;
import com.floodrescue.shared.exception.BusinessException;
import com.floodrescue.shared.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RescuerTaskServiceImpl implements RescuerTaskService {

    private final UserRepository userRepository;
    private final TeamRepository teamRepository;
    private final TaskGroupRepository taskGroupRepository;
    private final TaskGroupRequestRepository taskGroupRequestRepository;
    private final TaskGroupTimelineRepository taskGroupTimelineRepository;
    private final RescueAssignmentRepository rescueAssignmentRepository;
    private final TaskGroupMapper taskGroupMapper;

    @Override
    @Transactional(readOnly = true)
    public RescuerDashboardResponse getDashboard(Long rescuerUserId) {
        Long teamId = getRequiredTeamId(rescuerUserId);

        TeamEntity team = teamRepository.findById(teamId)
                .orElseThrow(() -> new NotFoundException("Đội cứu hộ không tồn tại"));

        // Active assignments of this team
        List<RescueAssigmentEntity> activeAssignments = rescueAssignmentRepository.findByTeamIdAndIsActiveTrue(teamId);

        // Task groups assigned to this team (paged latest)
        Page<TaskGroupEntity> groupsPage = taskGroupRepository.findByAssignedTeamId(teamId, Pageable.ofSize(10));
        List<TaskGroupResponse> groups = groupsPage.getContent().stream()
                .map(taskGroupMapper::toResponse)
                .toList();

        return RescuerDashboardResponse.builder()
                .teamId(team.getId())
                .teamName(team.getName())
                .activeAssignments((long) activeAssignments.size())
                .activeTaskGroups(groupsPage.getTotalElements())
                .taskGroups(groups)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<TaskGroupResponse> getMyTaskGroups(Long rescuerUserId, TaskGroupStatus status, Pageable pageable) {
        Long teamId = getRequiredTeamId(rescuerUserId);

        Page<TaskGroupEntity> page;
        if (status != null) {
            page = taskGroupRepository.findByAssignedTeamIdAndStatus(teamId, status, pageable);
        } else {
            page = taskGroupRepository.findByAssignedTeamId(teamId, pageable);
        }
        return page.map(taskGroupMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public TaskGroupResponse getMyTaskGroup(Long rescuerUserId, Long taskGroupId) {
        Long teamId = getRequiredTeamId(rescuerUserId);

        TaskGroupEntity group = taskGroupRepository.findById(taskGroupId)
                .orElseThrow(() -> new NotFoundException("Nhóm nhiệm vụ không tồn tại"));

        verifyGroupBelongsToTeam(group, teamId);

        List<TaskGroupRequestEntity> links = taskGroupRequestRepository.findByTaskGroupId(group.getId());
        List<RescueAssigmentEntity> assignments = rescueAssignmentRepository.findByTaskGroupIdAndIsActiveTrue(group.getId());
        List<TaskGroupTimelineEntity> timeline = taskGroupTimelineRepository.findByTaskGroupIdOrderByCreatedAtDesc(group.getId());

        return taskGroupMapper.toResponseWithDetails(group, links, assignments, timeline);
    }

    @Override
    @Transactional
    public TaskGroupResponse updateMyTaskGroupStatus(Long rescuerUserId, Long taskGroupId, TaskGroupStatus status, String note) {
        if (status == null) {
            throw new BusinessException("Trạng thái không được để trống");
        }

        Long teamId = getRequiredTeamId(rescuerUserId);

        TaskGroupEntity group = taskGroupRepository.findById(taskGroupId)
                .orElseThrow(() -> new NotFoundException("Nhóm nhiệm vụ không tồn tại"));

        verifyGroupBelongsToTeam(group, teamId);

        group.setStatus(status);
        taskGroupRepository.save(group);

        // Timeline (optional) - reuse existing timeline table
        UserEntity actor = userRepository.findById(rescuerUserId)
                .orElseThrow(() -> new NotFoundException("Người dùng không tồn tại"));
        TaskGroupTimelineEntity tl = TaskGroupTimelineEntity.builder()
                .taskGroup(group)
                .actor(actor)
                .eventType("RESCUER_STATUS_CHANGE")
                .note(note)
                .build();
        taskGroupTimelineRepository.save(tl);

        return getMyTaskGroup(rescuerUserId, taskGroupId);
    }

    private Long getRequiredTeamId(Long rescuerUserId) {
        UserEntity user = userRepository.findById(rescuerUserId)
                .orElseThrow(() -> new NotFoundException("Người dùng không tồn tại"));
        if (user.getTeamId() == null) {
            throw new BusinessException("Tài khoản đội cứu hộ chưa được gán vào đội");
        }
        return user.getTeamId();
    }

    private void verifyGroupBelongsToTeam(TaskGroupEntity group, Long teamId) {
        if (group.getAssignedTeam() == null || group.getAssignedTeam().getId() == null) {
            throw new BusinessException("Nhóm nhiệm vụ chưa được phân công cho đội nào");
        }
        if (!group.getAssignedTeam().getId().equals(teamId)) {
            throw new BusinessException("Bạn không có quyền truy cập nhóm nhiệm vụ của đội khác");
        }
    }
}