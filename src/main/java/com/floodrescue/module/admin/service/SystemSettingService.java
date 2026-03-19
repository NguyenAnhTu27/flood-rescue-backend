package com.floodrescue.module.admin.service;

import com.floodrescue.module.admin.dto.request.AdminContentPagesUpdateRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class SystemSettingService {

    private final JdbcTemplate jdbcTemplate;

    public Map<String, String> getAllSystemSettings() {
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(
                "SELECT COALESCE(setting_key, key_name) AS k, COALESCE(setting_value, value_text) AS v FROM system_settings"
        );
        Map<String, String> values = new HashMap<>();
        for (Map<String, Object> row : rows) {
            String key = row.get("k") == null ? null : String.valueOf(row.get("k"));
            if (key == null || key.isBlank()) {
                continue;
            }
            values.put(key, row.get("v") == null ? "" : String.valueOf(row.get("v")));
        }
        return values;
    }

    public Map<String, Object> getSystemSettingsResponse() {
        return Map.of("values", getAllSystemSettings());
    }

    public void upsertSystemSetting(String key, String value, Long actorId) {
        jdbcTemplate.update(
                "INSERT INTO system_settings(setting_key, setting_value, key_name, value_text, value_type, updated_by, updated_at) " +
                        "VALUES (?, ?, ?, ?, 'STRING', ?, NOW()) " +
                        "ON DUPLICATE KEY UPDATE setting_value = VALUES(setting_value), value_text = VALUES(value_text), updated_by = VALUES(updated_by), updated_at = NOW()",
                key, value, key, value, actorId
        );
    }

    public void updateSystemSettings(Map<String, String> payload, Long actorId) {
        for (Map.Entry<String, String> entry : payload.entrySet()) {
            upsertSystemSetting(entry.getKey().trim(), entry.getValue(), actorId);
        }
    }

    public Map<String, Object> getContentPages() {
        Map<String, String> values = getAllSystemSettings();

        Map<String, Object> pages = new HashMap<>();
        pages.put("termsTitle", values.getOrDefault("legalTermsTitle", "Điều khoản sử dụng"));
        pages.put("termsContent", values.getOrDefault("legalTermsContent", ""));
        pages.put("termsLabel", values.getOrDefault("footerTermsLabel", "Điều khoản sử dụng"));
        pages.put("privacyTitle", values.getOrDefault("privacyPolicyTitle", "Chính sách bảo mật"));
        pages.put("privacyContent", values.getOrDefault("privacyPolicyContent", ""));
        pages.put("privacyLabel", values.getOrDefault("footerPrivacyLabel", "Chính sách bảo mật"));
        pages.put("supportTitle", values.getOrDefault("supportPageTitle", "Liên hệ hỗ trợ"));
        pages.put("supportContent", values.getOrDefault("supportPageContent", ""));
        pages.put("supportLabel", values.getOrDefault("footerSupportLabel", "Liên hệ hỗ trợ"));
        return pages;
    }

    public void updateContentPages(AdminContentPagesUpdateRequest payload, Long actorId) {
        upsertSystemSetting("legalTermsTitle", valueOrDefault(payload.getTermsTitle(), "Điều khoản sử dụng"), actorId);
        upsertSystemSetting("legalTermsContent", valueOrDefault(payload.getTermsContent(), ""), actorId);
        upsertSystemSetting("privacyPolicyTitle", valueOrDefault(payload.getPrivacyTitle(), "Chính sách bảo mật"), actorId);
        upsertSystemSetting("privacyPolicyContent", valueOrDefault(payload.getPrivacyContent(), ""), actorId);
        upsertSystemSetting("supportPageTitle", valueOrDefault(payload.getSupportTitle(), "Liên hệ hỗ trợ"), actorId);
        upsertSystemSetting("supportPageContent", valueOrDefault(payload.getSupportContent(), ""), actorId);
        upsertSystemSetting("footerTermsLabel", valueOrDefault(payload.getTermsLabel(), "Điều khoản sử dụng"), actorId);
        upsertSystemSetting("footerPrivacyLabel", valueOrDefault(payload.getPrivacyLabel(), "Chính sách bảo mật"), actorId);
        upsertSystemSetting("footerSupportLabel", valueOrDefault(payload.getSupportLabel(), "Liên hệ hỗ trợ"), actorId);
        upsertSystemSetting("footerTermsUrl", valueOrDefault(payload.getFooterTermsUrl(), "/dieu-khoan-su-dung"), actorId);
        upsertSystemSetting("footerPrivacyUrl", valueOrDefault(payload.getFooterPrivacyUrl(), "/chinh-sach-bao-mat"), actorId);
        upsertSystemSetting("footerSupportUrl", valueOrDefault(payload.getFooterSupportUrl(), "/lien-he-ho-tro"), actorId);
    }

    private String normalizeOptional(String value) {
        if (value == null) {
            return null;
        }
        String normalized = value.trim();
        return normalized.isEmpty() ? null : normalized;
    }

    private String valueOrDefault(String value, String defaultValue) {
        String normalized = normalizeOptional(value);
        return normalized == null ? defaultValue : normalized;
    }
}
