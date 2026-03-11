package com.floodrescue.module.relief.service;

import com.floodrescue.module.inventory.entity.ItemCategoryEntity;
import com.floodrescue.module.inventory.repository.ItemCategoryRepository;
import com.floodrescue.module.relief.dto.request.ReliefRequestCreateRequest;
import com.floodrescue.module.relief.dto.response.ReliefRequestResponse;
import com.floodrescue.module.relief.entity.ReliefRequestEntity;
import com.floodrescue.module.relief.entity.ReliefRequestLineEntity;
import com.floodrescue.module.relief.repository.ReliefRequestLineRepository;
import com.floodrescue.module.relief.repository.ReliefRequestRepository;
import com.floodrescue.module.rescue.entity.RescueRequestEntity;
import com.floodrescue.module.rescue.repository.RescueRequestRepository;
import com.floodrescue.shared.enums.InventoryDocumentStatus;
import com.floodrescue.shared.exception.BusinessException;
import com.floodrescue.shared.exception.NotFoundException;
import com.floodrescue.shared.util.CodeGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReliefRequestService {

    private final ReliefRequestRepository reliefRequestRepository;
    private final ReliefRequestLineRepository reliefRequestLineRepository;
    private final ItemCategoryRepository itemCategoryRepository;
    private final RescueRequestRepository rescueRequestRepository;

    @Transactional
    public ReliefRequestResponse createReliefRequest(Long userId, ReliefRequestCreateRequest request) {
        if (request.getLines() == null || request.getLines().isEmpty()) {
            throw new BusinessException("Yêu cầu cứu trợ phải có ít nhất 1 mặt hàng");
        }
        if (userId == null) {
            throw new BusinessException("Không xác định được người tạo yêu cầu cứu trợ");
        }

        String code = CodeGenerator.generateInventoryReceiptCode(); // có thể tách hàm generate riêng nếu cần

        RescueRequestEntity rescueRequest = null;
        if (request.getRescueRequestId() != null) {
            rescueRequest = rescueRequestRepository.findById(request.getRescueRequestId())
                    .orElseThrow(() -> new NotFoundException("Yêu cầu cứu nạn không tồn tại: " + request.getRescueRequestId()));
        }

        ReliefRequestEntity relief = ReliefRequestEntity.builder()
                .code(code)
                .createdById(userId)
                .status(InventoryDocumentStatus.DRAFT)
                .targetArea(request.getTargetArea().trim())
                .rescueRequest(rescueRequest)
                .note(request.getNote())
                .build();

        ReliefRequestEntity saved = reliefRequestRepository.save(relief);

        List<ReliefRequestLineEntity> lines = request.getLines().stream()
                .map(lineReq -> {
                    ItemCategoryEntity category = itemCategoryRepository.findById(lineReq.getItemCategoryId())
                            .orElseThrow(() -> new NotFoundException("Loại hàng không tồn tại: " + lineReq.getItemCategoryId()));
                    return ReliefRequestLineEntity.builder()
                            .reliefRequest(saved)
                            .itemCategory(category)
                            .qty(BigDecimal.valueOf(lineReq.getQty()))
                            .unit(lineReq.getUnit())
                            .build();
                })
                .collect(Collectors.toList());

        reliefRequestLineRepository.saveAll(lines);

        return toResponse(saved, lines);
    }

    @Transactional(readOnly = true)
    public ReliefRequestResponse getReliefRequest(Long id) {
        ReliefRequestEntity relief = reliefRequestRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Yêu cầu cứu trợ không tồn tại"));
        List<ReliefRequestLineEntity> lines = reliefRequestLineRepository.findByReliefRequest(relief);
        return toResponse(relief, lines);
    }

    @Transactional(readOnly = true)
    public Page<ReliefRequestResponse> listReliefRequests(InventoryDocumentStatus status, Pageable pageable) {
        Page<ReliefRequestEntity> page;
        if (status != null) {
            page = reliefRequestRepository.findByStatus(status, pageable);
        } else {
            page = reliefRequestRepository.findAll(pageable);
        }
        return page.map(r -> toResponse(r, reliefRequestLineRepository.findByReliefRequest(r)));
    }

    @Transactional
    public ReliefRequestResponse approveReliefRequest(Long id, Long userId) {
        ReliefRequestEntity relief = reliefRequestRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Yêu cầu cứu trợ không tồn tại"));

        if (userId == null) {
            throw new BusinessException("Không xác định được người duyệt yêu cầu cứu trợ");
        }

        List<ReliefRequestLineEntity> lines = reliefRequestLineRepository.findByReliefRequest(relief);
        if (lines.isEmpty()) {
            throw new BusinessException("Yêu cầu cứu trợ không có dòng hàng nào");
        }

        if (relief.getStatus() == InventoryDocumentStatus.APPROVED) {
            return toResponse(relief, lines);
        }
        if (relief.getStatus() != InventoryDocumentStatus.DRAFT) {
            throw new BusinessException("Không thể duyệt yêu cầu cứu trợ ở trạng thái " + relief.getStatus());
        }

        relief.setStatus(InventoryDocumentStatus.APPROVED);
        relief = reliefRequestRepository.save(relief);

        return toResponse(relief, lines);
    }

    @Transactional
    public ReliefRequestResponse rejectReliefRequest(Long id, Long userId, String reason) {
        ReliefRequestEntity relief = reliefRequestRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Yêu cầu cứu trợ không tồn tại"));

        if (userId == null) {
            throw new BusinessException("Không xác định được người từ chối yêu cầu cứu trợ");
        }
        if (!StringUtils.hasText(reason)) {
            throw new BusinessException("Lý do từ chối không được để trống");
        }
        if (relief.getStatus() != InventoryDocumentStatus.DRAFT) {
            throw new BusinessException("Chỉ được từ chối yêu cầu cứu trợ ở trạng thái DRAFT");
        }

        String trimmedReason = reason.trim();
        String currentNote = relief.getNote();
        if (StringUtils.hasText(currentNote)) {
            relief.setNote(currentNote + "\n[TU CHOI] " + trimmedReason);
        } else {
            relief.setNote("[TU CHOI] " + trimmedReason);
        }
        relief.setStatus(InventoryDocumentStatus.CANCELLED);
        relief = reliefRequestRepository.save(relief);

        List<ReliefRequestLineEntity> lines = reliefRequestLineRepository.findByReliefRequest(relief);
        return toResponse(relief, lines);
    }

    private ReliefRequestResponse toResponse(ReliefRequestEntity relief, List<ReliefRequestLineEntity> lines) {
        Long rescueRequestId = relief.getRescueRequest() != null ? relief.getRescueRequest().getId() : null;

        return ReliefRequestResponse.builder()
                .id(relief.getId())
                .code(relief.getCode())
                .status(relief.getStatus())
                .targetArea(relief.getTargetArea())
                .createdById(relief.getCreatedById())
                .rescueRequestId(rescueRequestId)
                .note(relief.getNote())
                .createdAt(relief.getCreatedAt())
                .updatedAt(relief.getUpdatedAt())
                .lines(lines.stream().map(l -> ReliefRequestResponse.LineItem.builder()
                        .id(l.getId())
                        .itemCategoryId(l.getItemCategory().getId())
                        .itemCode(l.getItemCategory().getCode())
                        .itemName(l.getItemCategory().getName())
                        .qty(l.getQty())
                        .unit(l.getUnit())
                        .build()).collect(Collectors.toList()))
                .build();
    }
}
