package com.floodrescue.module.rescue.service;

import com.floodrescue.module.admin.service.SystemSettingService;
import com.floodrescue.module.rescue.dto.request.*;
import com.floodrescue.module.rescue.dto.response.RescueRequestResponse;
import com.floodrescue.module.rescue.entity.RescueRequestAttachmentEntity;
import com.floodrescue.module.rescue.entity.RescueRequestEntity;
import com.floodrescue.module.rescue.entity.RescueRequestTimelineEntity;
import com.floodrescue.module.rescue.mapper.RescueRequestMapper;
import com.floodrescue.module.rescue.repository.RescueAttachmentRepository;
import com.floodrescue.module.rescue.repository.RescueRequestRepository;
import com.floodrescue.module.rescue.repository.RescueTimelineRepository;
import com.floodrescue.module.user.entity.UserEntity;
import com.floodrescue.module.user.repository.UserRepository;
import com.floodrescue.shared.enums.RescuePriority;
import com.floodrescue.shared.enums.RescueRequestStatus;
import com.floodrescue.shared.enums.TimelineEventType;
import com.floodrescue.shared.exception.BusinessException;
import com.floodrescue.shared.exception.NotFoundException;
import com.floodrescue.shared.util.CodeGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RescueRequestServiceImpl implements RescueRequestService {

    private final RescueRequestRepository rescueRequestRepository;
    private final RescueAttachmentRepository attachmentRepository;
    private final RescueTimelineRepository timelineRepository;
    private final UserRepository userRepository;
    private final RescueRequestMapper mapper;
    private final SystemSettingService systemSettingService;

    private static final List<RescueRequestStatus> OPEN_REQUEST_STATUSES = List.of(
            RescueRequestStatus.PENDING,
            RescueRequestStatus.VERIFIED,
            RescueRequestStatus.IN_PROGRESS
    );

    @Override
    @Transactional
    public RescueRequestResponse createRescueRequest(Long citizenId, RescueRequestCreateRequest request) {
        UserEntity citizen = userRepository.findById(citizenId)
                .orElseThrow(() -> new NotFoundException("Người dùng không tồn tại"));

        int maxOpenRequests = systemSettingService.getMaxOpenRequestPerCitizen();
        long openRequests = rescueRequestRepository.countByCitizenIdAndStatusIn(citizenId, OPEN_REQUEST_STATUSES);
        if (openRequests >= maxOpenRequests) {
            throw new BusinessException("Bạn đang có " + openRequests + " yêu cầu đang mở. Tối đa cho phép là " + maxOpenRequests);
        }

        int slaMinutes = systemSettingService.getRescueSlaMinutes();
        LocalDateTime now = LocalDateTime.now();

        // Generate unique code
        String code;
        int attempts = 0;
        do {
            code = CodeGenerator.generateRescueRequestCode();
            attempts++;
            if (attempts > 10) {
                throw new BusinessException("Không thể tạo mã yêu cầu cứu hộ duy nhất");
            }
        } while (rescueRequestRepository.existsByCode(code));

        RescueRequestEntity entity = RescueRequestEntity.builder()
                .code(code)
                .citizen(citizen)
                .status(RescueRequestStatus.PENDING)
                .priority(request.getPriority())
                .affectedPeopleCount(request.getAffectedPeopleCount())
                .description(request.getDescription())
                .addressText(request.getAddressText())
                .locationVerified(false)
                .slaMinutes(slaMinutes)
                .slaDueAt(now.plusMinutes(slaMinutes))
                .build();

        final RescueRequestEntity savedEntity = rescueRequestRepository.save(entity);

        // Save attachments if any
        if (request.getAttachments() != null && !request.getAttachments().isEmpty()) {
            List<RescueRequestAttachmentEntity> attachments = request.getAttachments().stream()
                    .map(att -> RescueRequestAttachmentEntity.builder()
                            .rescueRequest(savedEntity)
                            .fileUrl(att.getFileUrl())
                            .fileType(att.getFileType())
                            .build())
                    .collect(Collectors.toList());
            attachmentRepository.saveAll(attachments);
        }

        // Create initial timeline entry
        createTimelineEntry(savedEntity, citizen, TimelineEventType.STATUS_CHANGE,
                null, RescueRequestStatus.PENDING, "Yêu cầu cứu hộ được tạo");

        return mapper.toResponse(savedEntity);
    }

    @Override
    @Transactional(readOnly = true)
    public RescueRequestResponse getRescueRequestById(Long id) {
        RescueRequestEntity entity = rescueRequestRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Yêu cầu cứu hộ không tồn tại"));

        List<RescueRequestAttachmentEntity> attachments = attachmentRepository.findByRescueRequestId(id);
        List<RescueRequestTimelineEntity> timeline = timelineRepository.findByRescueRequestIdOrderByCreatedAtDesc(id);

        return mapper.toResponseWithDetails(entity, attachments, timeline);
    }

    @Override
    @Transactional(readOnly = true)
    public RescueRequestResponse getRescueRequestByCode(String code) {
        RescueRequestEntity entity = rescueRequestRepository.findByCode(code)
                .orElseThrow(() -> new NotFoundException("Yêu cầu cứu hộ không tồn tại"));

        List<RescueRequestAttachmentEntity> attachments = attachmentRepository.findByRescueRequestId(entity.getId());
        List<RescueRequestTimelineEntity> timeline = timelineRepository.findByRescueRequestIdOrderByCreatedAtDesc(entity.getId());

        return mapper.toResponseWithDetails(entity, attachments, timeline);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<RescueRequestResponse> getRescueRequestsByCitizen(Long citizenId, Pageable pageable) {
        Page<RescueRequestEntity> entities = rescueRequestRepository.findByCitizenId(citizenId, pageable);
        return entities.map(mapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<RescueRequestResponse> getRescueRequestsByStatus(RescueRequestStatus status, Pageable pageable) {
        Page<RescueRequestEntity> entities = rescueRequestRepository.findByStatus(status, pageable);
        return entities.map(mapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<RescueRequestResponse> getPendingRescueRequests(Pageable pageable) {
        Page<RescueRequestEntity> entities = rescueRequestRepository.findPendingRequestsOrderedByPriority(
                RescueRequestStatus.PENDING, pageable);
        return entities.map(mapper::toResponse);
    }

    @Override
    @Transactional
    public RescueRequestResponse updateRescueRequest(Long id, Long citizenId, RescueRequestUpdateRequest request) {
        RescueRequestEntity entity = rescueRequestRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Yêu cầu cứu hộ không tồn tại"));

        // Check ownership
        if (!entity.getCitizen().getId().equals(citizenId)) {
            throw new BusinessException("Bạn không có quyền chỉnh sửa yêu cầu cứu hộ này");
        }

        // Check if can be updated
        if (entity.getStatus() == RescueRequestStatus.COMPLETED ||
                entity.getStatus() == RescueRequestStatus.CANCELLED ||
                entity.getStatus() == RescueRequestStatus.DUPLICATE) {
            throw new BusinessException("Không thể chỉnh sửa yêu cầu cứu hộ ở trạng thái này");
        }

        // Update fields
        if (request.getAffectedPeopleCount() != null) {
            entity.setAffectedPeopleCount(request.getAffectedPeopleCount());
        }
        if (request.getDescription() != null) {
            entity.setDescription(request.getDescription());
        }
        if (request.getAddressText() != null) {
            entity.setAddressText(request.getAddressText());
        }
        if (request.getPriority() != null) {
            entity.setPriority(request.getPriority());
        }

        entity = rescueRequestRepository.save(entity);

        // Add timeline entry
        UserEntity user = userRepository.findById(citizenId).orElseThrow();
        createTimelineEntry(entity, user, TimelineEventType.NOTE,
                null, null, "Yêu cầu cứu hộ được cập nhật");

        return mapper.toResponse(entity);
    }

    @Override
    @Transactional
    public RescueRequestResponse verifyRescueRequest(Long id, Long coordinatorId, VerifyRequest request) {
        RescueRequestEntity entity = rescueRequestRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Yêu cầu cứu hộ không tồn tại"));

        if (entity.getStatus() != RescueRequestStatus.PENDING) {
            throw new BusinessException("Chỉ có thể xác minh yêu cầu ở trạng thái PENDING");
        }

        entity.setLocationVerified(request.getLocationVerified());
        if (request.getLocationVerified()) {
            entity.setStatus(RescueRequestStatus.VERIFIED);
        }

        entity = rescueRequestRepository.save(entity);

        UserEntity coordinator = userRepository.findById(coordinatorId).orElseThrow();
        createTimelineEntry(entity, coordinator, TimelineEventType.VERIFY,
                RescueRequestStatus.PENDING, entity.getStatus(), request.getNote());

        return mapper.toResponse(entity);
    }

    @Override
    @Transactional
    public RescueRequestResponse prioritizeRescueRequest(Long id, Long coordinatorId, PrioritizeRequest request) {
        RescueRequestEntity entity = rescueRequestRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Yêu cầu cứu hộ không tồn tại"));

        RescuePriority oldPriority = entity.getPriority();
        entity.setPriority(request.getPriority());
        entity = rescueRequestRepository.save(entity);

        UserEntity coordinator = userRepository.findById(coordinatorId).orElseThrow();
        createTimelineEntry(entity, coordinator, TimelineEventType.NOTE,
                null, null, String.format("Thay đổi mức độ ưu tiên từ %s sang %s", oldPriority, request.getPriority()));

        return mapper.toResponse(entity);
    }

    @Override
    @Transactional
    public RescueRequestResponse markAsDuplicate(Long id, Long coordinatorId, MarkDuplicateRequest request) {
        RescueRequestEntity entity = rescueRequestRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Yêu cầu cứu hộ không tồn tại"));

        RescueRequestEntity masterRequest = rescueRequestRepository.findById(request.getMasterRequestId())
                .orElseThrow(() -> new NotFoundException("Yêu cầu cứu hộ chính không tồn tại"));

        if (masterRequest.getId().equals(id)) {
            throw new BusinessException("Không thể đánh dấu yêu cầu là bản sao của chính nó");
        }

        RescueRequestStatus oldStatus = entity.getStatus();
        entity.setStatus(RescueRequestStatus.DUPLICATE);
        entity.setMasterRequest(masterRequest);
        entity = rescueRequestRepository.save(entity);

        UserEntity coordinator = userRepository.findById(coordinatorId).orElseThrow();
        createTimelineEntry(entity, coordinator, TimelineEventType.MARK_DUPLICATE,
                oldStatus, RescueRequestStatus.DUPLICATE, request.getNote());

        return mapper.toResponse(entity);
    }

    @Override
    @Transactional
    public RescueRequestResponse addNote(Long id, Long userId, AddNoteRequest request) {
        RescueRequestEntity entity = rescueRequestRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Yêu cầu cứu hộ không tồn tại"));

        UserEntity user = userRepository.findById(userId).orElseThrow();
        createTimelineEntry(entity, user, TimelineEventType.NOTE, null, null, request.getNote());

        return mapper.toResponse(entity);
    }

    @Override
    @Transactional
    public RescueRequestResponse changeStatus(Long id, Long userId, RescueRequestStatus newStatus, String note) {
        RescueRequestEntity entity = rescueRequestRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Yêu cầu cứu hộ không tồn tại"));

        RescueRequestStatus oldStatus = entity.getStatus();
        entity.setStatus(newStatus);
        entity = rescueRequestRepository.save(entity);

        UserEntity user = userRepository.findById(userId).orElseThrow();
        createTimelineEntry(entity, user, TimelineEventType.STATUS_CHANGE, oldStatus, newStatus, note);

        return mapper.toResponse(entity);
    }

    @Override
    @Transactional
    public void cancelRescueRequest(Long id, Long citizenId) {
        RescueRequestEntity entity = rescueRequestRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Yêu cầu cứu hộ không tồn tại"));

        if (!entity.getCitizen().getId().equals(citizenId)) {
            throw new BusinessException("Bạn không có quyền hủy yêu cầu cứu hộ này");
        }

        if (entity.getStatus() == RescueRequestStatus.COMPLETED ||
                entity.getStatus() == RescueRequestStatus.CANCELLED) {
            throw new BusinessException("Không thể hủy yêu cầu cứu hộ ở trạng thái này");
        }

        RescueRequestStatus oldStatus = entity.getStatus();
        entity.setStatus(RescueRequestStatus.CANCELLED);
        entity = rescueRequestRepository.save(entity);

        UserEntity citizen = userRepository.findById(citizenId).orElseThrow();
        createTimelineEntry(entity, citizen, TimelineEventType.CANCEL,
                oldStatus, RescueRequestStatus.CANCELLED, "Yêu cầu cứu hộ được hủy bởi người tạo");
    }

    private void createTimelineEntry(
            RescueRequestEntity rescueRequest,
            UserEntity actor,
            TimelineEventType eventType,
            RescueRequestStatus fromStatus,
            RescueRequestStatus toStatus,
            String note) {

        RescueRequestTimelineEntity timeline = RescueRequestTimelineEntity.builder()
                .rescueRequest(rescueRequest)
                .actor(actor)
                .eventType(eventType)
                .fromStatus(fromStatus)
                .toStatus(toStatus)
                .note(note)
                .build();

        timelineRepository.save(timeline);
    }
}
