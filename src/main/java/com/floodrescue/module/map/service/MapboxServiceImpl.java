package com.floodrescue.module.map.service;

import com.floodrescue.module.map.dto.GeocodingResponse;
import com.floodrescue.shared.exception.BusinessException;
import com.fasterxml.jackson.databind.JsonNode;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Slf4j
@Service
@RequiredArgsConstructor
public class MapboxServiceImpl implements MapboxService {

    private final RestTemplate restTemplate;

    @Value("${mapbox.access-token}")
    private String accessToken;

    private static final String GEOCODING_URL = "https://api.mapbox.com/geocoding/v5/mapbox.places/{query}.json";
    private static final String REVERSE_GEOCODING_URL = "https://api.mapbox.com/geocoding/v5/mapbox.places/{lng},{lat}.json";
    private static final double EARTH_RADIUS_KM = 6371.0;
    private static final String MAPBOX_RESILIENCE = "mapboxGeocoding";

    @Override
    @Retry(name = MAPBOX_RESILIENCE, fallbackMethod = "geocodeFallback")
    @CircuitBreaker(name = MAPBOX_RESILIENCE, fallbackMethod = "geocodeFallback")
    public GeocodingResponse geocode(String address) {
        String url = UriComponentsBuilder
                .fromUriString(GEOCODING_URL)
                .queryParam("access_token", accessToken)
                .queryParam("limit", 1)
                .queryParam("language", "vi")
                .buildAndExpand(address)
                .toUriString();

        JsonNode response = restTemplate.getForObject(url, JsonNode.class);
        if (response == null || !response.has("features") || response.get("features").isEmpty()) {
            throw new BusinessException("Không tìm thấy tọa độ cho địa chỉ: " + address);
        }

        JsonNode feature = response.get("features").get(0);
        JsonNode center = feature.get("center");
        double lng = center.get(0).asDouble();
        double lat = center.get(1).asDouble();
        String placeName = feature.get("place_name").asText();

        return GeocodingResponse.builder()
                .address(placeName)
                .latitude(lat)
                .longitude(lng)
                .build();
    }

    @Override
    @Retry(name = MAPBOX_RESILIENCE, fallbackMethod = "reverseGeocodeFallback")
    @CircuitBreaker(name = MAPBOX_RESILIENCE, fallbackMethod = "reverseGeocodeFallback")
    public GeocodingResponse reverseGeocode(double latitude, double longitude) {
        String url = UriComponentsBuilder
                .fromUriString(REVERSE_GEOCODING_URL)
                .queryParam("access_token", accessToken)
                .queryParam("limit", 1)
                .queryParam("language", "vi")
                .buildAndExpand(longitude, latitude)
                .toUriString();

        JsonNode response = restTemplate.getForObject(url, JsonNode.class);
        if (response == null || !response.has("features") || response.get("features").isEmpty()) {
            throw new BusinessException("Không tìm thấy địa chỉ cho tọa độ: " + latitude + ", " + longitude);
        }

        JsonNode feature = response.get("features").get(0);
        String placeName = feature.get("place_name").asText();

        return GeocodingResponse.builder()
                .address(placeName)
                .latitude(latitude)
                .longitude(longitude)
                .build();
    }

    @Override
    public double calculateDistance(double lat1, double lng1, double lat2, double lng2) {
        double dLat = Math.toRadians(lat2 - lat1);
        double dLng = Math.toRadians(lng2 - lng1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLng / 2) * Math.sin(dLng / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return EARTH_RADIUS_KM * c;
    }

    private GeocodingResponse geocodeFallback(String address, Throwable throwable) {
        log.warn("Mapbox geocode failed for address='{}': {}", address, throwable.getMessage());
        throw new BusinessException("Dịch vụ bản đồ đang bận, vui lòng thử lại sau");
    }

    private GeocodingResponse reverseGeocodeFallback(double latitude, double longitude, Throwable throwable) {
        log.warn("Mapbox reverse geocode failed for lat={}, lng={}: {}", latitude, longitude, throwable.getMessage());
        throw new BusinessException("Dịch vụ bản đồ đang bận, vui lòng thử lại sau");
    }
}
