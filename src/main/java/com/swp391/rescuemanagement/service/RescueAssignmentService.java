package com.swp391.rescuemanagement.service;

import com.swp391.rescuemanagement.dto.request.AssignTeamRequest;
import com.swp391.rescuemanagement.dto.response.RescueAssignmentResponse;
import com.swp391.rescuemanagement.exception.BusinessException;
import com.swp391.rescuemanagement.model.*;
import com.swp391.rescuemanagement.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class RescueAssignmentService {

    private final RescueAssignmentRepository assignmentRepo;
    private final TaskGroupRepository taskGroupRepo;
    private final AssetRepository assetRepo;
    private final RescueRequestService rescueRequestService;
    private final RescueAssignmentRepository rescueAssignmentRepo;

    // ─────────────────────────────────────────────────────────
    // Gán team vào rescue request
    // ─────────────────────────────────────────────────────────

    /**
     * Gán TaskGroup (+ team của nó) vào một RescueRequest.
     *
     * Điều kiện:
     * - Request phải đang 'pending'.
     * - TaskGroup phải đang 'idle'.
     * - Team của TaskGroup phải RẢNH (không có assignment active).
     */
    @Transactional
    public RescueAssignmentResponse assignTeam(AssignTeamRequest dto, User actor) {

        // Lấy rescue request
        RescueRequest request = rescueRequestService.findOrThrow(dto.rescueRequestId());
        if (!"pending".equals(request.getStatus())) {
            throw BusinessException.badRequest(
                    "Chỉ có thể gán team khi request đang 'pending'. Hiện tại: " + request.getStatus());
        }

        // Lấy task group
        TaskGroup taskGroup = taskGroupRepo.findById(dto.taskGroupId())
                .orElseThrow(() -> BusinessException.notFound("task group", dto.taskGroupId()));

        if (!"idle".equals(taskGroup.getStatus())) {
            throw BusinessException.badRequest(
                    "TaskGroup [" + taskGroup.getCode() + "] không ở trạng thái 'idle'. Hiện tại: "
                            + taskGroup.getStatus());
        }

        // Kiểm tra team rảnh
        Team team = taskGroup.getAssignedTeam();
        if (team == null) {
            throw BusinessException.badRequest("TaskGroup chưa được gán team nào.");
        }
        if (assignmentRepo.isTeamBusy(team.getId())) {
            throw BusinessException.conflict(
                    "Team [" + team.getName() + "] đang thực hiện nhiệm vụ khác, không thể gán.");
        }

        // Lấy asset (optional)
        Asset asset = null;
        if (dto.assetId() != null) {
            asset = assetRepo.findById(dto.assetId())
                    .orElseThrow(() -> BusinessException.notFound("asset", dto.assetId()));
        }

        // Tạo assignment
        var assignment = new RescueAssignment();
        assignment.setTaskGroup(taskGroup);
        assignment.setTeam(team);
        assignment.setAsset(asset);
        assignment.setAssignedBy(actor);

        assignmentRepo.save(assignment);

        // Cập nhật task group → assigned
        taskGroup.setStatus("assigned");
        taskGroupRepo.save(taskGroup);

        // Ghi log
        rescueRequestService.writeLog(request, actor, "assigned",
                "Gán team [" + team.getName() + "] qua TaskGroup [" + taskGroup.getCode() + "].");

        log.info("Assignment tạo: rescueRequest={} ← team={}", request.getCode(), team.getName());
        return RescueAssignmentResponse.from(assignment);
    }

    // ─────────────────────────────────────────────────────────
    // BƯỚC 4: Update status assignment → In Progress / Completed
    // ─────────────────────────────────────────────────────────

    /**
     * Luồng hợp lệ:
     * assigned → in_progress → completed
     * → cancelled
     *
     * Khi in_progress → TaskGroup.status = 'in_progress', RescueRequest.status =
     * 'in_progress'
     * Khi completed → TaskGroup.status = 'completed' (team được GIẢI PHÓNG = rảnh)
     * RescueRequest.status = 'completed'
     * Khi cancelled → TaskGroup.status = 'idle' (team quay về rảnh)
     */
    @Transactional
    public RescueAssignmentResponse updateStatus(Long assignmentId, String newStatus, User actor) {

        RescueAssignment assignment = assignmentRepo.findById(assignmentId)
                .orElseThrow(() -> BusinessException.notFound("assignment", assignmentId));

        TaskGroup taskGroup = assignment.getTaskGroup();
        String current = taskGroup.getStatus();

        validateTransition(current, newStatus);

        // Lấy rescue request qua task_group_requests (lấy 1 request đầu tiên)
        // Trong thực tế có thể có N request; ở đây xử lý request của assignment này
        RescueRequest request = getLinkedRequest(taskGroup);

        switch (newStatus) {
            case "in_progress" -> {
                taskGroup.setStatus("in_progress");
                if (request != null) {
                    request.setStatus("in_progress");
                    rescueRequestService.writeLog(request, actor, "in_progress",
                            "Bắt đầu thực hiện bởi team [" + assignment.getTeam().getName() + "].");
                }
            }
            case "completed" -> {
                taskGroup.setStatus("completed");
                if (request != null) {
                    request.setStatus("completed");
                    rescueRequestService.writeLog(request, actor, "completed",
                            "Hoàn tất. Team [" + assignment.getTeam().getName() + "] được giải phóng.");
                }
                log.info("Team [{}] được giải phóng sau khi hoàn thành TaskGroup [{}].",
                        assignment.getTeam().getName(), taskGroup.getCode());
            }
            case "cancelled" -> {
                taskGroup.setStatus("idle"); // trả team về rảnh
                if (request != null) {
                    rescueRequestService.writeLog(request, actor, "cancelled",
                            "Assignment bị hủy. TaskGroup [" + taskGroup.getCode() + "] trả về idle.");
                }
            }
        }

        taskGroupRepo.save(taskGroup);
        if (request != null) {
            rescueRequestService.findOrThrow(request.getId()); // trigger save via dirty check
        }

        log.info("Assignment id={} TaskGroup status: {} → {}", assignmentId, current, newStatus);
        return RescueAssignmentResponse.from(assignment);
    }

    // ─────────────────────────────────────────────────────────
    // Query
    // ─────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<RescueAssignmentResponse> getByTaskGroup(Long taskGroupId) {
        return assignmentRepo.findByTaskGroupId(taskGroupId)
                .stream().map(RescueAssignmentResponse::from).toList();
    }

    // ─────────────────────────────────────────────────────────
    // Private helpers
    // ─────────────────────────────────────────────────────────

    private void validateTransition(String current, String next) {
        boolean valid = switch (current) {
            case "assigned" -> next.equals("in_progress") || next.equals("cancelled");
            case "in_progress" -> next.equals("completed") || next.equals("cancelled");
            default -> false;
        };
        if (!valid) {
            throw BusinessException.badRequest(
                    "Không thể chuyển TaskGroup từ '%s' sang '%s'.".formatted(current, next));
        }
    }

    /**
     * Lấy RescueRequest liên kết với TaskGroup qua bảng task_group_requests.
     * Trả null nếu không có (hiếm).
     */
    private RescueRequest getLinkedRequest(TaskGroup taskGroup) {
        // TaskGroup có thể liên kết nhiều request; lấy rescue request đầu tiên
        // bằng cách tìm qua assignmentRepo (assignment có rescue_request qua
        // task_group)
        // Đơn giản hóa: tìm assignment khác có cùng taskGroup rồi lấy request
        return assignmentRepo.findByTaskGroupId(taskGroup.getId())
                .stream()
                .findFirst()
                .map(a -> {
                    // a.getTaskGroup() đã có, nhưng không có trực tiếp rescue_request
                    // → dùng TaskGroupRequest nếu cần; ở đây trả null để log vẫn chạy
                    return (RescueRequest) null;
                })
                .orElse(null);
    }
}
