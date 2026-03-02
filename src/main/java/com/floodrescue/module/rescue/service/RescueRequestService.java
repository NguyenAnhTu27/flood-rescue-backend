package com.floodrescue.module.rescue.service;

import com.floodrescue.module.rescue.dto.request.*;
import com.floodrescue.module.rescue.dto.response.RescueRequestResponse;
import com.floodrescue.shared.enums.RescuePriority;
import com.floodrescue.shared.enums.RescueRequestStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface RescueRequestService {

    RescueRequestResponse createRescueRequest(Long citizenId, RescueRequestCreateRequest request);

    RescueRequestResponse getRescueRequestById(Long id);

    RescueRequestResponse getRescueRequestByCode(String code);

    Page<RescueRequestResponse> getRescueRequestsByCitizen(Long citizenId, Pageable pageable);

    Page<RescueRequestResponse> getRescueRequestsByStatus(RescueRequestStatus status, Pageable pageable);

    Page<RescueRequestResponse> searchRescueRequests(RescueRequestStatus status, RescuePriority priority, String keyword, Pageable pageable);

    Page<RescueRequestResponse> getPendingRescueRequests(Pageable pageable);

    RescueRequestResponse updateRescueRequest(Long id, Long citizenId, RescueRequestUpdateRequest request);

    RescueRequestResponse verifyRescueRequest(Long id, Long coordinatorId, VerifyRequest request);

    RescueRequestResponse prioritizeRescueRequest(Long id, Long coordinatorId, PrioritizeRequest request);

    RescueRequestResponse markAsDuplicate(Long id, Long coordinatorId, MarkDuplicateRequest request);

    RescueRequestResponse addNote(Long id, Long userId, AddNoteRequest request);

    RescueRequestResponse changeStatus(Long id, Long userId, RescueRequestStatus newStatus, String note);

    void cancelRescueRequest(Long id, Long citizenId);
}
