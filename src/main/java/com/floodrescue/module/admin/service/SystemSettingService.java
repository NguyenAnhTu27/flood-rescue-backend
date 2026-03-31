package com.floodrescue.module.admin.service;

import com.floodrescue.module.admin.dto.request.AdminContentPagesUpdateRequest;
import com.floodrescue.shared.util.TextNormalizationUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class SystemSettingService {

    private static final String DEFAULT_TERMS_LABEL = "\u0110i\u1ec1u kho\u1ea3n s\u1eed d\u1ee5ng";
    private static final String DEFAULT_PRIVACY_LABEL = "Ch\u00ednh s\u00e1ch b\u1ea3o m\u1eadt";
    private static final String DEFAULT_SUPPORT_LABEL = "Li\u00ean h\u1ec7 h\u1ed7 tr\u1ee3";

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
            values.put(key, normalizeValue(row.get("v")));
        }
        return values;
    }

    public Map<String, Object> getSystemSettingsResponse() {
        return Map.of("values", getAllSystemSettings());
    }

    public void upsertSystemSetting(String key, String value, Long actorId) {
        String normalizedValue = normalizeValue(value);
        jdbcTemplate.update(
                "INSERT INTO system_settings(setting_key, setting_value, key_name, value_text, value_type, updated_by, updated_at) " +
                        "VALUES (?, ?, ?, ?, 'STRING', ?, NOW()) " +
                        "ON DUPLICATE KEY UPDATE setting_value = VALUES(setting_value), value_text = VALUES(value_text), updated_by = VALUES(updated_by), updated_at = NOW()",
                key, normalizedValue, key, normalizedValue, actorId
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
        pages.put("termsTitle", values.getOrDefault("legalTermsTitle", DEFAULT_TERMS_LABEL));
        pages.put("termsContent", values.getOrDefault("legalTermsContent", ""));
        pages.put("termsLabel", values.getOrDefault("footerTermsLabel", DEFAULT_TERMS_LABEL));
        pages.put("privacyTitle", values.getOrDefault("privacyPolicyTitle", DEFAULT_PRIVACY_LABEL));
        pages.put("privacyContent", values.getOrDefault("privacyPolicyContent", ""));
        pages.put("privacyLabel", values.getOrDefault("footerPrivacyLabel", DEFAULT_PRIVACY_LABEL));
        pages.put("supportTitle", values.getOrDefault("supportPageTitle", DEFAULT_SUPPORT_LABEL));
        pages.put("supportContent", values.getOrDefault("supportPageContent", ""));
        pages.put("supportLabel", values.getOrDefault("footerSupportLabel", DEFAULT_SUPPORT_LABEL));
        return pages;
    }

    public void updateContentPages(AdminContentPagesUpdateRequest payload, Long actorId) {
        upsertSystemSetting("legalTermsTitle", valueOrDefault(payload.getTermsTitle(), DEFAULT_TERMS_LABEL), actorId);
        upsertSystemSetting("legalTermsContent", valueOrDefault(payload.getTermsContent(), ""), actorId);
        upsertSystemSetting("privacyPolicyTitle", valueOrDefault(payload.getPrivacyTitle(), DEFAULT_PRIVACY_LABEL), actorId);
        upsertSystemSetting("privacyPolicyContent", valueOrDefault(payload.getPrivacyContent(), ""), actorId);
        upsertSystemSetting("supportPageTitle", valueOrDefault(payload.getSupportTitle(), DEFAULT_SUPPORT_LABEL), actorId);
        upsertSystemSetting("supportPageContent", valueOrDefault(payload.getSupportContent(), ""), actorId);
        upsertSystemSetting("footerTermsLabel", valueOrDefault(payload.getTermsLabel(), DEFAULT_TERMS_LABEL), actorId);
        upsertSystemSetting("footerPrivacyLabel", valueOrDefault(payload.getPrivacyLabel(), DEFAULT_PRIVACY_LABEL), actorId);
        upsertSystemSetting("footerSupportLabel", valueOrDefault(payload.getSupportLabel(), DEFAULT_SUPPORT_LABEL), actorId);
        upsertSystemSetting("footerTermsUrl", valueOrDefault(payload.getFooterTermsUrl(), "/dieu-khoan-su-dung"), actorId);
        upsertSystemSetting("footerPrivacyUrl", valueOrDefault(payload.getFooterPrivacyUrl(), "/chinh-sach-bao-mat"), actorId);
        upsertSystemSetting("footerSupportUrl", valueOrDefault(payload.getFooterSupportUrl(), "/lien-he-ho-tro"), actorId);
    }

    private String normalizeOptional(String value) {
        if (value == null) {
            return null;
        }
        String normalized = normalizeValue(value).trim();
        return normalized.isEmpty() ? null : normalized;
    }

    private String valueOrDefault(String value, String defaultValue) {
        String normalized = normalizeOptional(value);
        return normalized == null ? defaultValue : normalized;
    }

    private String normalizeValue(Object value) {
        return value == null ? "" : TextNormalizationUtil.cleanDisplayText(String.valueOf(value));
    }
}
