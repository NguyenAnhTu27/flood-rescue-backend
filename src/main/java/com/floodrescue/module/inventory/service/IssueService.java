package com.floodrescue.module.inventory.service;

import com.floodrescue.module.asset.entity.AssetEntity;
import com.floodrescue.module.asset.repository.AssetReponsitory;
import com.floodrescue.module.inventory.dto.request.InventoryIssueCreateRequest;
import com.floodrescue.module.inventory.dto.response.InventoryIssueResponse;
import com.floodrescue.module.inventory.entity.InventoryIssueEntity;
import com.floodrescue.module.inventory.entity.InventoryIssueLineEntity;
import com.floodrescue.module.inventory.entity.ItemCategoryEntity;
import com.floodrescue.module.inventory.repository.IssueLineRepository;
import com.floodrescue.module.inventory.repository.IssueRepository;
import com.floodrescue.module.inventory.repository.ItemCategoryRepository;
import com.floodrescue.module.relief.entity.ReliefRequestEntity;
import com.floodrescue.module.relief.reponsitory.ReliefRequestRepository;
import com.floodrescue.module.team.entity.TeamEntity;
import com.floodrescue.module.team.repository.TeamRepository;
import com.floodrescue.shared.enums.InventoryDocumentStatus;
import com.floodrescue.shared.exception.BusinessException;
import com.floodrescue.shared.exception.NotFoundException;
import com.floodrescue.shared.util.CodeGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class IssueService {

    private final IssueRepository issueRepository;
    private final IssueLineRepository issueLineRepository;
    private final ItemCategoryRepository itemCategoryRepository;
    private final StockService stockService;
    private final ReliefRequestRepository reliefRequestRepository;
    private final TeamRepository teamRepository;
    private final AssetReponsitory assetRepository;

    @Transactional
    public InventoryIssueResponse createIssue(Long userId, InventoryIssueCreateRequest request) {
        if (request.getLines() == null || request.getLines().isEmpty()) {
            throw new BusinessException("Phiếu xuất phải có ít nhất 1 dòng");
        }

        String code = CodeGenerator.generateInventoryIssueCode();

        // Load các entity liên quan
        ReliefRequestEntity reliefRequest = null;
        if (request.getReliefRequestId() != null) {
            reliefRequest = reliefRequestRepository.findById(request.getReliefRequestId())
                    .orElseThrow(() -> new NotFoundException("Phiếu yêu cầu cứu trợ không tồn tại: " + request.getReliefRequestId()));
        }

        TeamEntity assignedTeam = null;
        if (request.getAssignedTeamId() != null) {
            assignedTeam = teamRepository.findById(request.getAssignedTeamId())
                    .orElseThrow(() -> new NotFoundException("Đội cứu hộ không tồn tại: " + request.getAssignedTeamId()));
        }

        AssetEntity asset = null;
        if (request.getAssetId() != null) {
            asset = assetRepository.findById(request.getAssetId())
                    .orElseThrow(() -> new NotFoundException("Phương tiện không tồn tại: " + request.getAssetId()));
        }

        final InventoryIssueEntity issue = InventoryIssueEntity.builder()
                .code(code)
                .status(InventoryDocumentStatus.DRAFT)
                .createdById(userId)
                .reliefRequest(reliefRequest)
                .assignedTeam(assignedTeam)
                .asset(asset)
                .note(request.getNote())
                .build();

        InventoryIssueEntity saved = issueRepository.save(issue);

        List<InventoryIssueLineEntity> lines = request.getLines().stream()
                .map(lineReq -> {
                    ItemCategoryEntity category = itemCategoryRepository.findById(lineReq.getItemCategoryId())
                            .orElseThrow(() -> new NotFoundException("Loại hàng không tồn tại: " + lineReq.getItemCategoryId()));
                    return InventoryIssueLineEntity.builder()
                            .issue(saved)
                            .itemCategory(category)
                            .qty(BigDecimal.valueOf(lineReq.getQty()))
                            .unit(lineReq.getUnit())
                            .build();
                })
                .collect(Collectors.toList());

        issueLineRepository.saveAll(lines);

        return toResponse(saved, lines);
    }

    @Transactional
    public InventoryIssueResponse approveIssue(Long id, Long userId) {
        InventoryIssueEntity issue = issueRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Phiếu xuất không tồn tại"));

        if (issue.getStatus() != InventoryDocumentStatus.DRAFT) {
            throw new BusinessException("Chỉ được duyệt phiếu ở trạng thái DRAFT");
        }

        List<InventoryIssueLineEntity> lines = issueLineRepository.findByIssue(issue);
        if (lines.isEmpty()) {
            throw new BusinessException("Phiếu xuất không có dòng nào");
        }

        stockService.applyIssue(lines);

        issue.setStatus(InventoryDocumentStatus.DONE);
        issue = issueRepository.save(issue);

        return toResponse(issue, lines);
    }

    @Transactional
    public InventoryIssueResponse cancelIssue(Long id, Long userId) {
        InventoryIssueEntity issue = issueRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Phiếu xuất không tồn tại"));

        if (issue.getStatus() != InventoryDocumentStatus.DRAFT) {
            throw new BusinessException("Chỉ được huỷ phiếu ở trạng thái DRAFT");
        }

        issue.setStatus(InventoryDocumentStatus.CANCELLED);
        issue = issueRepository.save(issue);

        List<InventoryIssueLineEntity> lines = issueLineRepository.findByIssue(issue);
        return toResponse(issue, lines);
    }

    @Transactional(readOnly = true)
    public InventoryIssueResponse getIssue(Long id) {
        InventoryIssueEntity issue = issueRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Phiếu xuất không tồn tại"));
        List<InventoryIssueLineEntity> lines = issueLineRepository.findByIssue(issue);
        return toResponse(issue, lines);
    }

    @Transactional(readOnly = true)
    public Page<InventoryIssueResponse> listIssues(InventoryDocumentStatus status, Pageable pageable) {
        Page<InventoryIssueEntity> page;
        if (status != null) {
            page = issueRepository.findByStatus(status, pageable);
        } else {
            page = issueRepository.findAll(pageable);
        }
        return page.map(i -> toResponse(i, issueLineRepository.findByIssue(i)));
    }

    private InventoryIssueResponse toResponse(InventoryIssueEntity issue, List<InventoryIssueLineEntity> lines) {
        InventoryIssueResponse.InventoryIssueResponseBuilder builder = InventoryIssueResponse.builder()
                .id(issue.getId())
                .code(issue.getCode())
                .status(issue.getStatus())
                .createdById(issue.getCreatedById())
                .note(issue.getNote())
                .createdAt(issue.getCreatedAt())
                .updatedAt(issue.getUpdatedAt())
                .lines(lines.stream().map(l -> InventoryIssueResponse.LineItem.builder()
                        .id(l.getId())
                        .itemCategoryId(l.getItemCategory().getId())
                        .itemCode(l.getItemCategory().getCode())
                        .itemName(l.getItemCategory().getName())
                        .qty(l.getQty())
                        .unit(l.getUnit())
                        .build()).collect(Collectors.toList()));

        // Map các field liên quan
        if (issue.getReliefRequest() != null) {
            builder.reliefRequestId(issue.getReliefRequest().getId())
                    .reliefRequestCode(issue.getReliefRequest().getCode());
        }

        if (issue.getAssignedTeam() != null) {
            builder.assignedTeamId(issue.getAssignedTeam().getId())
                    .assignedTeamName(issue.getAssignedTeam().getName())
                    .assignedTeamCode(issue.getAssignedTeam().getCode());
        }

        if (issue.getAsset() != null) {
            builder.assetId(issue.getAsset().getId())
                    .assetCode(issue.getAsset().getCode())
                    .assetName(issue.getAsset().getName());
        }

        return builder.build();
    }
}
