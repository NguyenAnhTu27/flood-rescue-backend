package com.floodrescue.module.publicapi.service;

import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class RuntimeSettingsServiceImpl implements RuntimeSettingsService {

    private final JdbcTemplate jdbcTemplate;

    @Override
    @Cacheable(cacheNames = "runtimeSettings")
    public Map<String, String> getRuntimeSettings() {
        Map<String, String> runtime = new HashMap<>();
        runtime.put("hotline", "1900-xxxx");
        runtime.put("footerBrandName", "QUẢN LÝ CỨU HỘ");
        runtime.put("footerDescription", "Hệ thống hỗ trợ cộng đồng trong tình huống thiên tai khẩn cấp. Thông tin được bảo mật và điều phối theo quy định của cơ quan chức năng.");
        runtime.put("footerTermsLabel", "Điều khoản sử dụng");
        runtime.put("footerTermsUrl", "/dieu-khoan-su-dung");
        runtime.put("footerPrivacyLabel", "Chính sách bảo mật");
        runtime.put("footerPrivacyUrl", "/chinh-sach-bao-mat");
        runtime.put("footerSupportLabel", "Liên hệ hỗ trợ");
        runtime.put("footerSupportUrl", "/lien-he-ho-tro");
        runtime.put("footerSupportEmail", "support@cuuho.gov.vn");
        runtime.put("footerFacebookUrl", "#");
        runtime.put("footerTwitterUrl", "#");
        runtime.put("footerYoutubeUrl", "#");
        runtime.put("footerCopyright", "© 2024 Hệ thống Quản lý Cứu hộ - Cứu trợ. Bản quyền thuộc về Cơ quan chủ quản.");

        runtime.putAll(loadAllSettings());

        return runtime;
    }

    @Override
    @Cacheable(cacheNames = "runtimeContentPage", key = "#pageKey == null ? '' : #pageKey.toLowerCase()")
    public Map<String, String> getContentPage(String pageKey) {
        if (pageKey == null || pageKey.isBlank()) {
            return null;
        }
        Map<String, String> values = loadAllSettings();

        return switch (pageKey.toLowerCase()) {
            case "terms" -> Map.of(
                    "title", values.getOrDefault("legalTermsTitle", "Điều khoản sử dụng"),
                    "content", values.getOrDefault("legalTermsContent", "")
            );
            case "privacy" -> Map.of(
                    "title", values.getOrDefault("privacyPolicyTitle", "Chính sách bảo mật"),
                    "content", values.getOrDefault("privacyPolicyContent", "")
            );
            case "support" -> Map.of(
                    "title", values.getOrDefault("supportPageTitle", "Liên hệ hỗ trợ"),
                    "content", values.getOrDefault("supportPageContent", "")
            );
            default -> null;
        };
    }

    private Map<String, String> loadAllSettings() {
        Map<String, String> values = new HashMap<>();
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(
                "SELECT COALESCE(setting_key, key_name) AS k, COALESCE(setting_value, value_text) AS v FROM system_settings"
        );
        for (Map<String, Object> row : rows) {
            String key = row.get("k") == null ? null : String.valueOf(row.get("k"));
            if (key == null || key.isBlank()) continue;
            values.put(key, row.get("v") == null ? "" : String.valueOf(row.get("v")));
        }
        return values;
    }
}
