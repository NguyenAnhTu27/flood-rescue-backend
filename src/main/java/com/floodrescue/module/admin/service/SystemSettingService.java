package com.floodrescue.module.admin.service;

import com.floodrescue.module.admin.entity.SystemSettingEntity;
import com.floodrescue.module.admin.repository.SystemSettingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class SystemSettingService {

    private final SystemSettingRepository systemSettingRepository;
    private final AuditLogService auditLogService;

    @Transactional
    public Map<String, Object> getSettings() {
        ensureDefaults();
        List<SystemSettingEntity> settings = systemSettingRepository.findAllByOrderBySettingKeyAsc();
        Map<String, String> values = new LinkedHashMap<>();
        Map<String, String> descriptions = new LinkedHashMap<>();
        for (SystemSettingEntity setting : settings) {
            String key = setting.getSettingKey() != null ? setting.getSettingKey() : setting.getKeyName();
            String value = setting.getSettingValue() != null ? setting.getSettingValue() : setting.getValueText();
            values.put(key, value);
            descriptions.put(key, setting.getDescription());
        }
        return Map.of("values", values, "descriptions", descriptions);
    }

    @Transactional(readOnly = true)
    public boolean isMaintenanceModeEnabled() {
        return getBooleanSetting("maintenanceMode", false);
    }

    @Transactional(readOnly = true)
    public int getAutoLockAfterFailedLogin() {
        return getIntSetting("autoLockAfterFailedLogin", 5, 1, 20);
    }

    @Transactional(readOnly = true)
    public int getFailedLoginLockMinutes() {
        return getIntSetting("failedLoginLockMinutes", 15, 1, 1440);
    }

    @Transactional(readOnly = true)
    public int getMaxOpenRequestPerCitizen() {
        return getIntSetting("maxOpenRequestPerCitizen", 1, 1, 20);
    }

    @Transactional(readOnly = true)
    public int getRescueSlaMinutes() {
        return getIntSetting("rescueSlaMinutes", 30, 1, 240);
    }

    @Transactional(readOnly = true)
    public int getMapRefreshSeconds() {
        return getIntSetting("mapRefreshSeconds", 20, 5, 300);
    }

    @Transactional(readOnly = true)
    public String getHotline() {
        return getStringSetting("hotline", "1900-xxxx");
    }

    @Transactional
    public Map<String, Object> getPublicRuntimeSettings() {
        ensureDefaults();
        String hotline = getHotline();
        Map<String, Object> runtime = new LinkedHashMap<>();
        runtime.put("maintenanceMode", isMaintenanceModeEnabled());
        runtime.put("mapRefreshSeconds", getMapRefreshSeconds());
        runtime.put("hotline", hotline);
        runtime.put("maxOpenRequestPerCitizen", getMaxOpenRequestPerCitizen());
        runtime.put("rescueSlaMinutes", getRescueSlaMinutes());
        runtime.put("failedLoginLockMinutes", getFailedLoginLockMinutes());
        runtime.put("footerBrandName", getStringSetting("footerBrandName", "QUẢN LÝ CỨU HỘ"));
        runtime.put("footerDescription", getStringSetting("footerDescription", "Hệ thống hỗ trợ cộng đồng trong tình huống thiên tai khẩn cấp. Thông tin được bảo mật và điều phối theo quy định của cơ quan chức năng."));
        runtime.put("footerTermsLabel", getStringSetting("footerTermsLabel", "Điều khoản sử dụng"));
        runtime.put("footerTermsUrl", getStringSetting("footerTermsUrl", "#"));
        runtime.put("footerPrivacyLabel", getStringSetting("footerPrivacyLabel", "Chính sách bảo mật"));
        runtime.put("footerPrivacyUrl", getStringSetting("footerPrivacyUrl", "#"));
        runtime.put("footerSupportLabel", getStringSetting("footerSupportLabel", "Liên hệ hỗ trợ"));
        runtime.put("footerSupportUrl", getStringSetting("footerSupportUrl", "#"));
        runtime.put("footerSupportEmail", getStringSetting("footerSupportEmail", "support@cuuho.gov.vn"));
        runtime.put("footerSupportPhone", hotline);
        runtime.put("footerFacebookUrl", getStringSetting("footerFacebookUrl", "#"));
        runtime.put("footerTwitterUrl", getStringSetting("footerTwitterUrl", "#"));
        runtime.put("footerYoutubeUrl", getStringSetting("footerYoutubeUrl", "#"));
        runtime.put("footerCopyright", getStringSetting("footerCopyright", "© 2024 Hệ thống Quản lý Cứu hộ - Cứu trợ. Bản quyền thuộc về Cơ quan chủ quản."));
        return runtime;
    }

    @Transactional
    public String updateSettings(Map<String, Object> request) {
        if (request == null || request.isEmpty()) {
            return "Không có dữ liệu thay đổi";
        }

        ensureDefaults();
        LocalDateTime now = LocalDateTime.now();

        for (Map.Entry<String, Object> entry : request.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue() == null ? "" : String.valueOf(entry.getValue());

            SystemSettingEntity setting = systemSettingRepository.findBySettingKey(key)
                    .or(() -> systemSettingRepository.findByKeyName(key))
                    .orElseGet(() -> SystemSettingEntity.builder()
                            .keyName(key)
                            .valueText(value)
                            .valueType("STRING")
                            .updatedBy(null)
                            .settingKey(key)
                            .settingValue(value)
                            .description("Custom setting")
                            .updatedAt(now)
                            .build());

            setting.setKeyName(key);
            setting.setValueText(value);
            if (setting.getValueType() == null || setting.getValueType().isBlank()) {
                setting.setValueType("STRING");
            }
            setting.setSettingValue(value);
            setting.setSettingKey(key);
            setting.setUpdatedAt(now);
            systemSettingRepository.save(setting);
        }

        auditLogService.log("UPDATE_SYSTEM_SETTINGS", "system", "SUCCESS",
                "Updated keys = " + request.keySet());
        return "Cập nhật cấu hình thành công";
    }

    @Transactional
    public void ensureDefaults() {
        LocalDateTime now = LocalDateTime.now();
        List<SystemSettingEntity> defaults = new ArrayList<>(List.of(
                setting("rescueSlaMinutes", "30", "SLA phản hồi cứu hộ (phút)", now),
                setting("maxOpenRequestPerCitizen", "1", "Số yêu cầu mở tối đa / công dân", now),
                setting("autoLockAfterFailedLogin", "5", "Số lần nhập sai trước khi khóa", now),
                setting("failedLoginLockMinutes", "15", "Thời gian khóa tạm khi nhập sai (phút)", now),
                setting("maintenanceMode", "false", "Chế độ bảo trì", now),
                setting("mapRefreshSeconds", "20", "Chu kỳ refresh bản đồ (giây)", now),
                setting("hotline", "1900-xxxx", "Hotline hỗ trợ", now),
                setting("footerBrandName", "QUẢN LÝ CỨU HỘ", "Tên thương hiệu phần footer", now),
                setting("footerDescription", "Hệ thống hỗ trợ cộng đồng trong tình huống thiên tai khẩn cấp. Thông tin được bảo mật và điều phối theo quy định của cơ quan chức năng.", "Mô tả footer", now),
                setting("footerTermsLabel", "Điều khoản sử dụng", "Nhãn link điều khoản", now),
                setting("footerTermsUrl", "#", "URL điều khoản sử dụng", now),
                setting("footerPrivacyLabel", "Chính sách bảo mật", "Nhãn link chính sách bảo mật", now),
                setting("footerPrivacyUrl", "#", "URL chính sách bảo mật", now),
                setting("footerSupportLabel", "Liên hệ hỗ trợ", "Nhãn link liên hệ hỗ trợ", now),
                setting("footerSupportUrl", "#", "URL liên hệ hỗ trợ", now),
                setting("footerSupportEmail", "support@cuuho.gov.vn", "Email hỗ trợ footer", now),
                setting("footerFacebookUrl", "#", "Liên kết Facebook footer", now),
                setting("footerTwitterUrl", "#", "Liên kết Twitter footer", now),
                setting("footerYoutubeUrl", "#", "Liên kết Youtube footer", now),
                setting("footerCopyright", "© 2024 Hệ thống Quản lý Cứu hộ - Cứu trợ. Bản quyền thuộc về Cơ quan chủ quản.", "Nội dung dòng bản quyền footer", now)
        ));

        for (SystemSettingEntity defaultSetting : defaults) {
            boolean exists = systemSettingRepository.findBySettingKey(defaultSetting.getSettingKey()).isPresent()
                    || systemSettingRepository.findByKeyName(defaultSetting.getKeyName()).isPresent();
            if (!exists) {
                systemSettingRepository.save(defaultSetting);
            }
        }
    }

    private SystemSettingEntity setting(String key, String value, String desc, LocalDateTime now) {
        return SystemSettingEntity.builder()
                .keyName(key)
                .valueText(value)
                .valueType("STRING")
                .updatedBy(null)
                .settingKey(key)
                .settingValue(value)
                .description(desc)
                .updatedAt(now)
                .build();
    }

    private String getSettingRawValue(String key, String defaultValue) {
        return systemSettingRepository.findBySettingKey(key)
                .map(setting -> setting.getSettingValue() != null ? setting.getSettingValue() : setting.getValueText())
                .or(() -> systemSettingRepository.findByKeyName(key)
                        .map(setting -> setting.getSettingValue() != null ? setting.getSettingValue() : setting.getValueText()))
                .filter(v -> v != null && !v.isBlank())
                .orElse(defaultValue);
    }

    private boolean getBooleanSetting(String key, boolean defaultValue) {
        String raw = getSettingRawValue(key, String.valueOf(defaultValue));
        return "true".equalsIgnoreCase(raw) || "1".equals(raw);
    }

    private int getIntSetting(String key, int defaultValue, int minValue, int maxValue) {
        String raw = getSettingRawValue(key, String.valueOf(defaultValue));
        try {
            int value = Integer.parseInt(raw.trim());
            if (value < minValue) return minValue;
            if (value > maxValue) return maxValue;
            return value;
        } catch (Exception ignored) {
            return defaultValue;
        }
    }

    private String getStringSetting(String key, String defaultValue) {
        String raw = getSettingRawValue(key, defaultValue);
        return raw == null || raw.isBlank() ? defaultValue : raw.trim();
    }
}
