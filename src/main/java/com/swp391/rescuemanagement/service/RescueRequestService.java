package com.swp391.rescuemanagement.service;

import com.swp391.rescuemanagement.dto.request.CreateRescueRequestRequest;
import com.swp391.rescuemanagement.dto.response.RescueRequestResponse;
import com.swp391.rescuemanagement.exception.BusinessException;
import com.swp391.rescuemanagement.model.RescueRequest;
import com.swp391.rescuemanagement.model.RescueRequestLog;
import com.swp391.rescuemanagement.model.User;
import com.swp391.rescuemanagement.repository.RescueRequestLogRepository;
import com.swp391.rescuemanagement.repository.RescueRequestRepository;
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
public class RescueRequestService {

    private final RescueRequestRepository rescueRequestRepo;
    private final RescueRequestLogRepository logRepo;

    // ─────────────────────────────────────────────────────────
    // BƯỚC 2: Tạo rescue request
    // ─────────────────────────────────────────────────────────

    /**
     * Tạo rescue request mới.
     * Không cho phép 1 caller có 2 request 'pending' cùng lúc (duplicate check).
     */
    @Transactional
    public RescueRequestResponse create(CreateRescueRequestRequest dto, User caller) {

        // Kiểm tra duplicate: caller đã có request 'pending' chưa?
        if (rescueRequestRepo.existsByCallerAndStatus(caller, "pending")) {
            throw BusinessException.conflict(
                    "Bạn đang có một yêu cầu cứu hộ chờ xử lý. " +
                            "Vui lòng đợi hoàn thành trước khi tạo yêu cầu mới.");
        }

        // Sinh mã tự động
        String code = generateCode();

        // Tạo entity
        var request = new RescueRequest();
        request.setCode(code);
        request.setCaller(caller);
        request.setStatus("pending");
        request.setPriority(dto.priority());
        request.setDescription(dto.description());
        request.setAddressText(dto.addressText());
        request.setAffectedPeopleCount(dto.affectedPeopleCount());

        RescueRequest saved = rescueRequestRepo.save(request);

        // Ghi audit log
        writeLog(saved, caller, "created", "Yêu cầu cứu hộ [" + code + "] được tạo.");

        log.info("RescueRequest tạo thành công: code={} caller={}", code, caller.getEmail());
        return RescueRequestResponse.from(saved);
    }

    // ─────────────────────────────────────────────────────────
    // Query
    // ─────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<RescueRequestResponse> getAll() {
        return rescueRequestRepo.findAll()
                .stream().map(RescueRequestResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public List<RescueRequestResponse> getByStatus(String status) {
        return rescueRequestRepo.findByStatusOrderByCreatedAtDesc(status)
                .stream().map(RescueRequestResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public RescueRequestResponse getById(Long id) {
        return RescueRequestResponse.from(findOrThrow(id));
    }

    // ─────────────────────────────────────────────────────────
    // BƯỚC 4 (phần request): Cập nhật status
    // ─────────────────────────────────────────────────────────

    /**
     * Cập nhật status rescue_request.
     * Cho phép: pending → in_progress → completed | cancelled
     */
    @Transactional
    public RescueRequestResponse updateStatus(Long id, String newStatus, User actor) {
        RescueRequest request = findOrThrow(id);
        String currentStatus = request.getStatus();

        validateStatusTransition(currentStatus, newStatus);

        request.setStatus(newStatus);
        rescueRequestRepo.save(request);

        writeLog(request, actor, newStatus,
                "Trạng thái thay đổi: [%s] → [%s]".formatted(currentStatus, newStatus));

        log.info("RescueRequest id={} status: {} → {}", id, currentStatus, newStatus);
        return RescueRequestResponse.from(request);
    }

    // ─────────────────────────────────────────────────────────
    // Helper (package-accessible cho RescueAssignmentService)
    // ─────────────────────────────────────────────────────────

    public RescueRequest findOrThrow(Long id) {
        return rescueRequestRepo.findById(id)
                .orElseThrow(() -> BusinessException.notFound("rescue request", id));
    }

    public void writeLog(RescueRequest request, User actor, String action, String note) {
        var logEntry = new RescueRequestLog();
        logEntry.setRescueRequest(request);
        logEntry.setActor(actor);
        logEntry.setAction(action);
        logEntry.setNote(note);
        logRepo.save(logEntry);
    }

    // ─────────────────────────────────────────────────────────
    // Private helpers
    // ─────────────────────────────────────────────────────────

    private void validateStatusTransition(String current, String next) {
        boolean valid = switch (current) {
            case "pending" -> next.equals("in_progress") || next.equals("cancelled");
            case "in_progress" -> next.equals("completed") || next.equals("cancelled");
            default -> false;
        };
        if (!valid) {
            throw BusinessException.badRequest(
                    "Không thể chuyển trạng thái từ '%s' sang '%s'.".formatted(current, next));
        }
    }

    private String generateCode() {
        String year = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy"));
        String prefix = "RR-" + year + "-";
        long seq = rescueRequestRepo.countByCodeStartingWith(prefix) + 1;
        return "%s%04d".formatted(prefix, seq);
    }
}
