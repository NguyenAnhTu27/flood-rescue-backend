package com.floodrescue.module.publicapi.controller;

import com.floodrescue.shared.util.TextNormalizationUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/public")
@RequiredArgsConstructor
public class RuntimeSettingsController {

    private static final String DEFAULT_BRAND = "QU\u1ea2N L\u00dd C\u1ee8U H\u1ed8";
    private static final String DEFAULT_DESCRIPTION = "H\u1ec7 th\u1ed1ng h\u1ed7 tr\u1ee3 c\u1ed9ng \u0111\u1ed3ng trong t\u00ecnh hu\u1ed1ng thi\u00ean tai kh\u1ea9n c\u1ea5p. Th\u00f4ng tin \u0111\u01b0\u1ee3c b\u1ea3o m\u1eadt v\u00e0 \u0111i\u1ec1u ph\u1ed1i theo quy \u0111\u1ecbnh c\u1ee7a c\u01a1 quan ch\u1ee9c n\u0103ng.";
    private static final String DEFAULT_TERMS_LABEL = "\u0110i\u1ec1u kho\u1ea3n s\u1eed d\u1ee5ng";
    private static final String DEFAULT_PRIVACY_LABEL = "Ch\u00ednh s\u00e1ch b\u1ea3o m\u1eadt";
    private static final String DEFAULT_SUPPORT_LABEL = "Li\u00ean h\u1ec7 h\u1ed7 tr\u1ee3";
    private static final String DEFAULT_COPYRIGHT = "\u00a9 2024 H\u1ec7 th\u1ed1ng Qu\u1ea3n l\u00fd C\u1ee9u h\u1ed9 - C\u1ee9u tr\u1ee3. B\u1ea3n quy\u1ec1n thu\u1ed9c v\u1ec1 C\u01a1 quan ch\u1ee7 qu\u1ea3n.";

    private final JdbcTemplate jdbcTemplate;

    @GetMapping("/runtime-settings")
    public ResponseEntity<Map<String, String>> getRuntimeSettings() {
        Map<String, String> runtime = new HashMap<>();
        runtime.put("hotline", "1900-xxxx");
        runtime.put("footerBrandName", DEFAULT_BRAND);
        runtime.put("footerDescription", DEFAULT_DESCRIPTION);
        runtime.put("footerTermsLabel", DEFAULT_TERMS_LABEL);
        runtime.put("footerTermsUrl", "/dieu-khoan-su-dung");
        runtime.put("footerPrivacyLabel", DEFAULT_PRIVACY_LABEL);
        runtime.put("footerPrivacyUrl", "/chinh-sach-bao-mat");
        runtime.put("footerSupportLabel", DEFAULT_SUPPORT_LABEL);
        runtime.put("footerSupportUrl", "/lien-he-ho-tro");
        runtime.put("footerSupportEmail", "support@cuuho.gov.vn");
        runtime.put("footerFacebookUrl", "#");
        runtime.put("footerTwitterUrl", "#");
        runtime.put("footerYoutubeUrl", "#");
        runtime.put("footerCopyright", DEFAULT_COPYRIGHT);

        List<Map<String, Object>> rows = jdbcTemplate.queryForList(
                "SELECT COALESCE(setting_key, key_name) AS k, COALESCE(setting_value, value_text) AS v FROM system_settings"
        );
        for (Map<String, Object> row : rows) {
            String key = row.get("k") == null ? null : String.valueOf(row.get("k"));
            if (key == null || key.isBlank()) {
                continue;
            }
            runtime.put(key, normalizeValue(row.get("v")));
        }

        return ResponseEntity.ok(runtime);
    }

    @GetMapping("/content-pages/{pageKey}")
    public ResponseEntity<Map<String, String>> getContentPage(@PathVariable String pageKey) {
        Map<String, String> values = new HashMap<>();
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(
                "SELECT COALESCE(setting_key, key_name) AS k, COALESCE(setting_value, value_text) AS v FROM system_settings"
        );
        for (Map<String, Object> row : rows) {
            String key = row.get("k") == null ? null : String.valueOf(row.get("k"));
            if (key == null || key.isBlank()) {
                continue;
            }
            values.put(key, normalizeValue(row.get("v")));
        }

        return switch (pageKey.toLowerCase()) {
            case "terms" -> ResponseEntity.ok(Map.of(
                    "title", values.getOrDefault("legalTermsTitle", DEFAULT_TERMS_LABEL),
                    "content", values.getOrDefault("legalTermsContent", "")
            ));
            case "privacy" -> ResponseEntity.ok(Map.of(
                    "title", values.getOrDefault("privacyPolicyTitle", DEFAULT_PRIVACY_LABEL),
                    "content", values.getOrDefault("privacyPolicyContent", "")
            ));
            case "support" -> ResponseEntity.ok(Map.of(
                    "title", values.getOrDefault("supportPageTitle", DEFAULT_SUPPORT_LABEL),
                    "content", values.getOrDefault("supportPageContent", "")
            ));
            default -> ResponseEntity.notFound().build();
        };
    }

    private String normalizeValue(Object value) {
        return value == null ? "" : TextNormalizationUtil.cleanDisplayText(String.valueOf(value));
    }
}
