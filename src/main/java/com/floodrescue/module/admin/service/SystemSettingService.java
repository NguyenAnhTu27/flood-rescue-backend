package com.floodrescue.module.admin.service;

import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class SystemSettingService {

	private final JdbcTemplate jdbcTemplate;

	@Transactional(readOnly = true)
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

	@Transactional
	@CacheEvict(cacheNames = {"runtimeSettings", "runtimeContentPage"}, allEntries = true)
	public void updateSystemSettings(Map<String, Object> payload, Long actorId) {
		for (Map.Entry<String, Object> entry : payload.entrySet()) {
			upsertSystemSetting(entry.getKey(), String.valueOf(entry.getValue()), actorId);
		}
	}

	@Transactional(readOnly = true)
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

	@Transactional
	@CacheEvict(cacheNames = {"runtimeSettings", "runtimeContentPage"}, allEntries = true)
	public void updateContentPages(Map<String, Object> payload, Long actorId) {
		upsertSystemSetting("legalTermsTitle", String.valueOf(payload.getOrDefault("termsTitle", "Điều khoản sử dụng")), actorId);
		upsertSystemSetting("legalTermsContent", String.valueOf(payload.getOrDefault("termsContent", "")), actorId);
		upsertSystemSetting("privacyPolicyTitle", String.valueOf(payload.getOrDefault("privacyTitle", "Chính sách bảo mật")), actorId);
		upsertSystemSetting("privacyPolicyContent", String.valueOf(payload.getOrDefault("privacyContent", "")), actorId);
		upsertSystemSetting("supportPageTitle", String.valueOf(payload.getOrDefault("supportTitle", "Liên hệ hỗ trợ")), actorId);
		upsertSystemSetting("supportPageContent", String.valueOf(payload.getOrDefault("supportContent", "")), actorId);
		upsertSystemSetting("footerTermsLabel", String.valueOf(payload.getOrDefault("termsLabel", "Điều khoản sử dụng")), actorId);
		upsertSystemSetting("footerPrivacyLabel", String.valueOf(payload.getOrDefault("privacyLabel", "Chính sách bảo mật")), actorId);
		upsertSystemSetting("footerSupportLabel", String.valueOf(payload.getOrDefault("supportLabel", "Liên hệ hỗ trợ")), actorId);
		upsertSystemSetting("footerTermsUrl", String.valueOf(payload.getOrDefault("footerTermsUrl", "/dieu-khoan-su-dung")), actorId);
		upsertSystemSetting("footerPrivacyUrl", String.valueOf(payload.getOrDefault("footerPrivacyUrl", "/chinh-sach-bao-mat")), actorId);
		upsertSystemSetting("footerSupportUrl", String.valueOf(payload.getOrDefault("footerSupportUrl", "/lien-he-ho-tro")), actorId);
	}

	private void upsertSystemSetting(String key, String value, Long actorId) {
		jdbcTemplate.update(
				"INSERT INTO system_settings(setting_key, setting_value, key_name, value_text, value_type, updated_by, updated_at) " +
						"VALUES (?, ?, ?, ?, 'STRING', ?, NOW()) " +
						"ON DUPLICATE KEY UPDATE setting_value = VALUES(setting_value), value_text = VALUES(value_text), updated_by = VALUES(updated_by), updated_at = NOW()",
				key, value, key, value, actorId
		);
	}
}
