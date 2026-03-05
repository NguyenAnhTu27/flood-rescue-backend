package com.swp391.rescuemanagement.service;

import com.swp391.rescuemanagement.dto.response.TeamResponse;
import com.swp391.rescuemanagement.exception.BusinessException;
import com.swp391.rescuemanagement.model.Team;
import com.swp391.rescuemanagement.repository.RescueAssignmentRepository;
import com.swp391.rescuemanagement.repository.TeamRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class TeamService {

    private final TeamRepository teamRepo;
    private final RescueAssignmentRepository assignmentRepo;

    // ─────────────────────────────────────────────────────────
    // BƯỚC 3: Logic team rảnh / bận
    // ─────────────────────────────────────────────────────────

    /**
     * Trả về danh sách team ĐANG RẢNH.
     *
     * Điều kiện:
     * (1) team.status = 'active'
     * (2) Không có TaskGroup nào đang 'assigned' | 'in_progress' gắn với team đó
     */
    @Transactional(readOnly = true)
    public List<TeamResponse> getAvailableTeams() {
        List<Team> teams = teamRepo.findAvailableTeams();
        log.debug("Available teams: {}", teams.size());
        return teams.stream().map(TeamResponse::from).toList();
    }

    /**
     * Trả về danh sách team ĐANG BẬN.
     * Bận = có ít nhất 1 TaskGroup đang 'assigned' | 'in_progress'.
     */
    @Transactional(readOnly = true)
    public List<TeamResponse> getBusyTeams() {
        List<Team> teams = teamRepo.findBusyTeams();
        log.debug("Busy teams: {}", teams.size());
        return teams.stream().map(TeamResponse::from).toList();
    }

    /**
     * Kiểm tra trạng thái 1 team cụ thể.
     * Trả về: "available" | "busy" | team.status (nếu inactive...)
     */
    @Transactional(readOnly = true)
    public String getTeamAvailability(Long teamId) {
        Team team = teamRepo.findById(teamId)
                .orElseThrow(() -> BusinessException.notFound("team", teamId));

        if (!"active".equals(team.getStatus())) {
            return team.getStatus(); // 'inactive' hoặc status khác
        }

        return assignmentRepo.isTeamBusy(teamId) ? "busy" : "available";
    }

    /**
     * Lấy tất cả team.
     */
    @Transactional(readOnly = true)
    public List<TeamResponse> getAll() {
        return teamRepo.findAll().stream().map(TeamResponse::from).toList();
    }

    /**
     * Cập nhật status team (admin).
     * Không cho phép active team đang bận.
     */
    @Transactional
    public TeamResponse updateStatus(Long teamId, String newStatus) {
        Team team = teamRepo.findById(teamId)
                .orElseThrow(() -> BusinessException.notFound("team", teamId));

        // Không cho đổi sang 'active' nếu team còn assignment chưa xong
        if ("active".equals(newStatus) && assignmentRepo.isTeamBusy(teamId)) {
            throw BusinessException.badRequest(
                    "Team đang có công việc chưa hoàn thành, không thể chuyển sang 'active'.");
        }

        team.setStatus(newStatus);
        log.info("Team id={} status → '{}'", teamId, newStatus);
        return TeamResponse.from(teamRepo.save(team));
    }

    // Package-level helper dùng bởi RescueAssignmentService
    public Team findOrThrow(Long teamId) {
        return teamRepo.findById(teamId)
                .orElseThrow(() -> BusinessException.notFound("team", teamId));
    }
}
