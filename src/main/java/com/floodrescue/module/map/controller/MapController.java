package com.floodrescue.module.map.controller;

import com.floodrescue.module.map.dto.*;
import com.floodrescue.module.map.service.MapboxService;
import com.floodrescue.module.rescue.entity.RescueRequestEntity;
import com.floodrescue.module.rescue.repository.RescueRequestRepository;
import com.floodrescue.module.team.service.TeamService;
import com.floodrescue.shared.enums.RescueRequestStatus;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/map")
@RequiredArgsConstructor
public class MapController {

    private final TeamService teamService;
    private final MapboxService mapboxService;
    private final RescueRequestRepository rescueRequestRepository;

    // ==============================
    // TEAM LOCATION APIs
    // ==============================

    /**
     * PUT /api/map/teams/{teamId}/location
     * Cập nhật vị trí hiện tại của đội cứu hộ
     */
    @PutMapping("/teams/{teamId}/location")
    public ResponseEntity<TeamLocationResponse> updateTeamLocation(
            @PathVariable Long teamId,
            @Valid @RequestBody LocationUpdateRequest request) {
        TeamLocationResponse response = teamService.updateTeamLocation(teamId, request);
        return ResponseEntity.ok(response);
    }

    /**
     * GET /api/map/teams/locations
     * Lấy vị trí tất cả đội cứu hộ đang hoạt động (cho hiển thị trên bản đồ)
     */
    @GetMapping("/teams/locations")
    public ResponseEntity<List<TeamLocationResponse>> getAllTeamLocations() {
        List<TeamLocationResponse> locations = teamService.getAllTeamLocations();
        return ResponseEntity.ok(locations);
    }

    /**
     * GET /api/map/teams/nearest?lat=16.05&lng=108.20&radius=10
     * Tìm đội cứu hộ gần nhất trong bán kính (km)
     */
    @GetMapping("/teams/nearest")
    public ResponseEntity<List<TeamLocationResponse>> findNearestTeams(
            @RequestParam("lat") double latitude,
            @RequestParam("lng") double longitude,
            @RequestParam(value = "radius", defaultValue = "10") double radiusKm) {
        List<TeamLocationResponse> nearestTeams = teamService.findNearestTeams(latitude, longitude, radiusKm);
        return ResponseEntity.ok(nearestTeams);
    }

    // ==============================
    // RESCUE REQUEST LOCATION APIs
    // ==============================

    /**
     * GET /api/map/rescue-requests/locations
     * Lấy vị trí tất cả yêu cầu cứu hộ đang chờ xử lý (PENDING, VERIFIED)
     */
    @GetMapping("/rescue-requests/locations")
    public ResponseEntity<List<RescueLocationResponse>> getRescueRequestLocations() {
        Page<RescueRequestEntity> pendingRequests = rescueRequestRepository
                .findByStatus(RescueRequestStatus.PENDING, Pageable.unpaged());

        List<RescueLocationResponse> locations = pendingRequests.getContent().stream()
                .filter(r -> r.getLatitude() != null && r.getLongitude() != null)
                .map(r -> RescueLocationResponse.builder()
                        .requestId(r.getId())
                        .code(r.getCode())
                        .status(r.getStatus().name())
                        .priority(r.getPriority().name())
                        .addressText(r.getAddressText())
                        .latitude(r.getLatitude())
                        .longitude(r.getLongitude())
                        .affectedPeopleCount(r.getAffectedPeopleCount())
                        .build())
                .collect(Collectors.toList());

        return ResponseEntity.ok(locations);
    }

    // ==============================
    // GEOCODING APIs (Mapbox)
    // ==============================

    /**
     * GET /api/map/geocode?address=Đà Nẵng
     * Chuyển địa chỉ text → tọa độ GPS (gọi Mapbox Geocoding API)
     */
    @GetMapping("/geocode")
    public ResponseEntity<GeocodingResponse> geocode(@RequestParam("address") String address) {
        GeocodingResponse result = mapboxService.geocode(address);
        if (result == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(result);
    }

    /**
     * GET /api/map/reverse-geocode?lat=16.05&lng=108.20
     * Chuyển tọa độ GPS → địa chỉ text (gọi Mapbox Geocoding API)
     */
    @GetMapping("/reverse-geocode")
    public ResponseEntity<GeocodingResponse> reverseGeocode(
            @RequestParam("lat") double latitude,
            @RequestParam("lng") double longitude) {
        GeocodingResponse result = mapboxService.reverseGeocode(latitude, longitude);
        return ResponseEntity.ok(result);
    }
}
