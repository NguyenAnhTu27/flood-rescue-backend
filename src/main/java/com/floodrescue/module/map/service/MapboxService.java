package com.floodrescue.module.map.service;

import com.floodrescue.module.map.dto.GeocodingResponse;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class MapboxService {

    private static final String GEOCODING_URL = "https://api.mapbox.com/geocoding/v5/mapbox.places/%s.json?access_token=%s&limit=1&language=vi";

    @Value("${mapbox.access-token:}")
    private String accessToken;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Geocode: chuyển địa chỉ text → tọa độ (lat, lng)
     */
    public GeocodingResponse geocode(String address) {
        if (accessToken == null || accessToken.isBlank()) {
            throw new IllegalStateException(
                    "Mapbox access token is not configured. Set mapbox.access-token in application.properties");
        }

        String encodedAddress = address.replace(" ", "%20");
        String url = String.format(GEOCODING_URL, encodedAddress, accessToken);

        try {
            String response = restTemplate.getForObject(url, String.class);
            JsonNode root = objectMapper.readTree(response);
            JsonNode features = root.path("features");

            if (features.isEmpty()) {
                return null;
            }

            JsonNode firstResult = features.get(0);
            JsonNode coordinates = firstResult.path("geometry").path("coordinates");
            String placeName = firstResult.path("place_name").asText();

            // Mapbox trả về [longitude, latitude] — chú ý thứ tự ngược!
            double lng = coordinates.get(0).asDouble();
            double lat = coordinates.get(1).asDouble();

            return GeocodingResponse.builder()
                    .latitude(lat)
                    .longitude(lng)
                    .placeName(placeName)
                    .build();
        } catch (Exception e) {
            throw new RuntimeException("Failed to call Mapbox Geocoding API: " + e.getMessage(), e);
        }
    }

    /**
     * Reverse Geocode: chuyển tọa độ (lat, lng) → địa chỉ text
     */
    public GeocodingResponse reverseGeocode(double latitude, double longitude) {
        if (accessToken == null || accessToken.isBlank()) {
            throw new IllegalStateException("Mapbox access token is not configured.");
        }

        String coordinates = longitude + "," + latitude;
        String url = String.format(GEOCODING_URL, coordinates, accessToken);

        try {
            String response = restTemplate.getForObject(url, String.class);
            JsonNode root = objectMapper.readTree(response);
            JsonNode features = root.path("features");

            if (features.isEmpty()) {
                return GeocodingResponse.builder()
                        .latitude(latitude)
                        .longitude(longitude)
                        .placeName("Unknown location")
                        .build();
            }

            String placeName = features.get(0).path("place_name").asText();

            return GeocodingResponse.builder()
                    .latitude(latitude)
                    .longitude(longitude)
                    .placeName(placeName)
                    .build();
        } catch (Exception e) {
            throw new RuntimeException("Failed to call Mapbox Reverse Geocoding API: " + e.getMessage(), e);
        }
    }

    /**
     * Công thức Haversine — tính khoảng cách (km) giữa 2 tọa độ GPS
     */
    public static double haversineDistance(double lat1, double lng1, double lat2, double lng2) {
        final double R = 6371.0; // bán kính Trái Đất (km)

        double dLat = Math.toRadians(lat2 - lat1);
        double dLng = Math.toRadians(lng2 - lng1);

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                        * Math.sin(dLng / 2) * Math.sin(dLng / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return R * c;
    }
}
