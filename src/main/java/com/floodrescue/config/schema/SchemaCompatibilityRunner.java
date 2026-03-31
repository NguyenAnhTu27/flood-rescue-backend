package com.floodrescue.config.schema;

import com.floodrescue.shared.util.TextNormalizationUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.Map;

@Slf4j
@Component
@ConditionalOnProperty(value = "app.schema-compatibility.enabled", havingValue = "true", matchIfMissing = true)
@RequiredArgsConstructor
@Order(Ordered.HIGHEST_PRECEDENCE)
public class SchemaCompatibilityRunner implements ApplicationRunner {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public void run(ApplicationArguments args) {
        try {
            alignUsersTable();
            alignInventoryDocumentStatusEnums();
            alignTeamsTable();
            alignRescueRequestStatusEnums();
            alignRescueRequestEmergencyColumns();
            alignRescueRequestLocationColumns();
            alignRescueResultConfirmationColumns();
            alignNotificationColumns();
            alignTeamLocationColumns();
            alignSystemFeedbackTable();
            alignInventoryClassifications();
            alignInventoryUnits();
            alignInventoryIssueRelations();
            alignReliefRequestWorkflow();
            repairLegacyTextData();
            log.info("[SchemaCompatibility] Schema alignment completed");
        } catch (Exception e) {
            log.error("[SchemaCompatibility] Schema alignment failed: {}", e.getMessage(), e);
            throw e;
        }
    }

    private void alignUsersTable() {
        if (!columnExists("users", "is_leader")) {
            exec("ALTER TABLE users ADD COLUMN is_leader TINYINT(1) NOT NULL DEFAULT 0 AFTER status");
            log.info("[SchemaCompatibility] Added users.is_leader");
        }
        if (!columnExists("users", "failed_login_attempts")) {
            exec("ALTER TABLE users ADD COLUMN failed_login_attempts INT NULL DEFAULT 0 AFTER is_leader");
            log.info("[SchemaCompatibility] Added users.failed_login_attempts");
        }
        if (!columnExists("users", "locked_at")) {
            exec("ALTER TABLE users ADD COLUMN locked_at DATETIME(6) NULL AFTER failed_login_attempts");
            log.info("[SchemaCompatibility] Added users.locked_at");
        }
        if (!columnExists("users", "temp_locked_until")) {
            exec("ALTER TABLE users ADD COLUMN temp_locked_until DATETIME(6) NULL AFTER locked_at");
            log.info("[SchemaCompatibility] Added users.temp_locked_until");
        }
        if (!columnExists("users", "rescue_request_blocked")) {
            exec("ALTER TABLE users ADD COLUMN rescue_request_blocked TINYINT(1) NOT NULL DEFAULT 0 AFTER is_leader");
            log.info("[SchemaCompatibility] Added users.rescue_request_blocked");
        }
        if (!columnExists("users", "rescue_request_blocked_reason")) {
            exec("ALTER TABLE users ADD COLUMN rescue_request_blocked_reason TEXT NULL AFTER rescue_request_blocked");
            log.info("[SchemaCompatibility] Added users.rescue_request_blocked_reason");
        }
    }

    private void alignInventoryDocumentStatusEnums() {
        alignInventoryDocumentStatus("relief_requests", "status", "DRAFT");
        alignInventoryDocumentStatus("inventory_issues", "status", "DRAFT");
        alignInventoryDocumentStatus("inventory_receipts", "status", "DRAFT");
    }

    private void alignTeamsTable() {
        if (!columnExists("teams", "description")) {
            exec("ALTER TABLE teams ADD COLUMN description VARCHAR(255) NULL");
            log.info("[SchemaCompatibility] Added teams.description");
        }

        String columnType = getColumnType("teams", "team_type");
        if (columnType == null) {
            return;
        }

        if (columnType.contains("RESCUE_TEAM")
                || columnType.contains("COORDINATOR")
                || columnType.contains("LOGISTICS")
                || columnType.contains("MANAGER")
                || !columnType.contains("RESCUE")
                || !columnType.contains("RELIEF")
                || !columnType.contains("MEDICAL")) {
            exec("ALTER TABLE teams MODIFY team_type VARCHAR(20) NOT NULL");
            exec("""
                    UPDATE teams
                    SET team_type = CASE team_type
                        WHEN 'RESCUE_TEAM' THEN 'RESCUE'
                        WHEN 'COORDINATOR' THEN 'RELIEF'
                        WHEN 'LOGISTICS' THEN 'RELIEF'
                        WHEN 'MANAGER' THEN 'RELIEF'
                        ELSE team_type
                    END
                    """);
            exec("""
                    UPDATE teams
                    SET team_type = 'RESCUE'
                    WHERE team_type NOT IN ('RESCUE', 'RELIEF', 'MEDICAL')
                    """);
            exec("ALTER TABLE teams MODIFY team_type ENUM('RESCUE','RELIEF','MEDICAL') NOT NULL");
            log.info("[SchemaCompatibility] Aligned teams.team_type enum");
        }

        String statusType = getColumnType("teams", "status");
        if (statusType != null && !statusType.toLowerCase().contains("tinyint")) {
            exec("ALTER TABLE teams MODIFY status VARCHAR(20) NOT NULL");
            exec("""
                    UPDATE teams
                    SET status = CASE
                        WHEN UPPER(status) IN ('1', 'ACTIVE', 'AVAILABLE', 'TRUE') THEN '1'
                        ELSE '0'
                    END
                    """);
            exec("ALTER TABLE teams MODIFY status TINYINT(1) NOT NULL DEFAULT 1");
            log.info("[SchemaCompatibility] Aligned teams.status to tinyint");
        }
    }

    private void alignRescueRequestStatusEnums() {
        String rrStatusType = getColumnType("rescue_requests", "status");
        if (rrStatusType != null && !rrStatusType.contains("ASSIGNED")) {
            exec("ALTER TABLE rescue_requests MODIFY status VARCHAR(20) NOT NULL");
            exec("""
                    UPDATE rescue_requests
                    SET status = 'PENDING'
                    WHERE status NOT IN ('PENDING','VERIFIED','ASSIGNED','IN_PROGRESS','COMPLETED','CANCELLED','DUPLICATE')
                    """);
            exec("ALTER TABLE rescue_requests MODIFY status ENUM('PENDING','VERIFIED','ASSIGNED','IN_PROGRESS','COMPLETED','CANCELLED','DUPLICATE') NOT NULL DEFAULT 'PENDING'");
            log.info("[SchemaCompatibility] Aligned rescue_requests.status enum");
        }

        String fromStatusType = getColumnType("rescue_request_timeline", "from_status");
        if (fromStatusType != null && !fromStatusType.contains("ASSIGNED")) {
            exec("ALTER TABLE rescue_request_timeline MODIFY from_status VARCHAR(20) NULL");
            exec("""
                    UPDATE rescue_request_timeline
                    SET from_status = NULL
                    WHERE from_status IS NOT NULL
                      AND from_status NOT IN ('PENDING','VERIFIED','ASSIGNED','IN_PROGRESS','COMPLETED','CANCELLED','DUPLICATE')
                    """);
            exec("ALTER TABLE rescue_request_timeline MODIFY from_status ENUM('PENDING','VERIFIED','ASSIGNED','IN_PROGRESS','COMPLETED','CANCELLED','DUPLICATE') NULL");
            log.info("[SchemaCompatibility] Aligned rescue_request_timeline.from_status enum");
        }

        String toStatusType = getColumnType("rescue_request_timeline", "to_status");
        if (toStatusType != null && !toStatusType.contains("ASSIGNED")) {
            exec("ALTER TABLE rescue_request_timeline MODIFY to_status VARCHAR(20) NULL");
            exec("""
                    UPDATE rescue_request_timeline
                    SET to_status = NULL
                    WHERE to_status IS NOT NULL
                      AND to_status NOT IN ('PENDING','VERIFIED','ASSIGNED','IN_PROGRESS','COMPLETED','CANCELLED','DUPLICATE')
                    """);
            exec("ALTER TABLE rescue_request_timeline MODIFY to_status ENUM('PENDING','VERIFIED','ASSIGNED','IN_PROGRESS','COMPLETED','CANCELLED','DUPLICATE') NULL");
            log.info("[SchemaCompatibility] Aligned rescue_request_timeline.to_status enum");
        }
    }

    private void alignRescueRequestEmergencyColumns() {
        if (!columnExists("rescue_requests", "waiting_for_team")) {
            exec("ALTER TABLE rescue_requests ADD COLUMN waiting_for_team TINYINT(1) NOT NULL DEFAULT 0 AFTER location_verified");
        }
        if (!columnExists("rescue_requests", "coordinator_cancel_note")) {
            exec("ALTER TABLE rescue_requests ADD COLUMN coordinator_cancel_note TEXT NULL AFTER waiting_for_team");
        }
        if (!columnExists("rescue_requests", "is_emergency")) {
            exec("ALTER TABLE rescue_requests ADD COLUMN is_emergency TINYINT(1) NOT NULL DEFAULT 0 AFTER coordinator_cancel_note");
        }
        if (!columnExists("rescue_requests", "emergency_no")) {
            exec("ALTER TABLE rescue_requests ADD COLUMN emergency_no INT NULL AFTER is_emergency");
        }
        if (!columnExists("rescue_requests", "source_team_id")) {
            exec("ALTER TABLE rescue_requests ADD COLUMN source_team_id BIGINT UNSIGNED NULL AFTER emergency_no");
        }
        if (!columnExists("rescue_requests", "emergency_parent_request_id")) {
            exec("ALTER TABLE rescue_requests ADD COLUMN emergency_parent_request_id BIGINT UNSIGNED NULL AFTER source_team_id");
        }
        if (!indexExists("rescue_requests", "idx_rr_emergency_parent")) {
            exec("ALTER TABLE rescue_requests ADD INDEX idx_rr_emergency_parent (emergency_parent_request_id)");
        }
        if (!constraintExists("rescue_requests", "fk_rr_emergency_parent")) {
            exec("ALTER TABLE rescue_requests ADD CONSTRAINT fk_rr_emergency_parent FOREIGN KEY (emergency_parent_request_id) REFERENCES rescue_requests(id)");
        }
    }

    private void alignRescueRequestLocationColumns() {
        if (!columnExists("rescue_requests", "latitude")) {
            exec("ALTER TABLE rescue_requests ADD COLUMN latitude DOUBLE NULL AFTER address_text");
        }
        if (!columnExists("rescue_requests", "longitude")) {
            exec("ALTER TABLE rescue_requests ADD COLUMN longitude DOUBLE NULL AFTER latitude");
        }
        if (!columnExists("rescue_requests", "location_description")) {
            exec("ALTER TABLE rescue_requests ADD COLUMN location_description VARCHAR(500) NULL AFTER longitude");
        }
    }

    private void alignNotificationColumns() {
        if (!columnExists("notifications", "event_code")) {
            exec("ALTER TABLE notifications ADD COLUMN event_code VARCHAR(80) NULL");
        }
        if (!columnExists("notifications", "reference_type")) {
            exec("ALTER TABLE notifications ADD COLUMN reference_type VARCHAR(50) NULL");
        }
        if (!columnExists("notifications", "reference_id")) {
            exec("ALTER TABLE notifications ADD COLUMN reference_id BIGINT UNSIGNED NULL");
        }
        if (!columnExists("notifications", "is_urgent")) {
            exec("ALTER TABLE notifications ADD COLUMN is_urgent TINYINT(1) NOT NULL DEFAULT 0");
        }
        if (!columnExists("notifications", "acknowledged_at")) {
            exec("ALTER TABLE notifications ADD COLUMN acknowledged_at DATETIME NULL");
        }
        if (!columnExists("notifications", "action_status")) {
            exec("ALTER TABLE notifications ADD COLUMN action_status VARCHAR(40) NULL");
        }
        if (!columnExists("notifications", "action_note")) {
            exec("ALTER TABLE notifications ADD COLUMN action_note TEXT NULL");
        }
        if (!columnExists("notifications", "queue_request_id")) {
            exec("ALTER TABLE notifications ADD COLUMN queue_request_id BIGINT UNSIGNED NULL");
        }
        if (!columnExists("notifications", "source_team_id")) {
            exec("ALTER TABLE notifications ADD COLUMN source_team_id BIGINT UNSIGNED NULL");
        }
    }

    private void alignRescueResultConfirmationColumns() {
        if (!columnExists("rescue_requests", "rescue_result_confirmation_status")) {
            exec("ALTER TABLE rescue_requests ADD COLUMN rescue_result_confirmation_status VARCHAR(30) NULL AFTER emergency_parent_request_id");
        }
        if (!columnExists("rescue_requests", "rescue_result_confirmation_note")) {
            exec("ALTER TABLE rescue_requests ADD COLUMN rescue_result_confirmation_note TEXT NULL AFTER rescue_result_confirmation_status");
        }
        if (!columnExists("rescue_requests", "rescue_result_confirmed_at")) {
            exec("ALTER TABLE rescue_requests ADD COLUMN rescue_result_confirmed_at DATETIME NULL AFTER rescue_result_confirmation_note");
        }
    }

    private void alignTeamLocationColumns() {
        if (!columnExists("teams", "current_latitude")) {
            exec("ALTER TABLE teams ADD COLUMN current_latitude DOUBLE NULL AFTER description");
        }
        if (!columnExists("teams", "current_longitude")) {
            exec("ALTER TABLE teams ADD COLUMN current_longitude DOUBLE NULL AFTER current_latitude");
        }
        if (!columnExists("teams", "current_location_text")) {
            exec("ALTER TABLE teams ADD COLUMN current_location_text VARCHAR(255) NULL AFTER current_longitude");
        }
        if (!columnExists("teams", "current_location_updated_at")) {
            exec("ALTER TABLE teams ADD COLUMN current_location_updated_at DATETIME NULL AFTER current_location_text");
        }
    }

    private void alignSystemFeedbackTable() {
        exec("""
                CREATE TABLE IF NOT EXISTS system_feedbacks (
                    id BIGINT NOT NULL AUTO_INCREMENT,
                    citizen_id BIGINT UNSIGNED NOT NULL,
                    rating INT NOT NULL,
                    feedback_content TEXT NULL,
                    rescued_confirmed TINYINT(1) NOT NULL DEFAULT 0,
                    relief_confirmed TINYINT(1) NOT NULL DEFAULT 0,
                    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                    PRIMARY KEY (id)
                )
                """);

        if (!columnExists("system_feedbacks", "feedback_content")) {
            exec("ALTER TABLE system_feedbacks ADD COLUMN feedback_content TEXT NULL AFTER rating");
        }
        if (!columnExists("system_feedbacks", "rescued_confirmed")) {
            exec("ALTER TABLE system_feedbacks ADD COLUMN rescued_confirmed TINYINT(1) NOT NULL DEFAULT 0 AFTER feedback_content");
        }
        if (!columnExists("system_feedbacks", "relief_confirmed")) {
            exec("ALTER TABLE system_feedbacks ADD COLUMN relief_confirmed TINYINT(1) NOT NULL DEFAULT 0 AFTER rescued_confirmed");
        }
        if (!columnExists("system_feedbacks", "created_at")) {
            exec("ALTER TABLE system_feedbacks ADD COLUMN created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP AFTER relief_confirmed");
        }

        // Ensure citizen_id type is compatible with users.id (common mismatch: BIGINT vs BIGINT UNSIGNED).
        String userIdColumnType = getColumnType("users", "id");
        if (userIdColumnType != null && !userIdColumnType.isBlank()) {
            String expectedType = userIdColumnType.toUpperCase();
            String currentType = getColumnType("system_feedbacks", "citizen_id");
            if (currentType == null || !currentType.equalsIgnoreCase(expectedType)) {
                exec("ALTER TABLE system_feedbacks MODIFY citizen_id " + expectedType + " NOT NULL");
            }
        }

        if (!indexExists("system_feedbacks", "idx_system_feedbacks_citizen")) {
            exec("ALTER TABLE system_feedbacks ADD INDEX idx_system_feedbacks_citizen (citizen_id)");
        }
        if (!indexExists("system_feedbacks", "idx_system_feedbacks_rating")) {
            exec("ALTER TABLE system_feedbacks ADD INDEX idx_system_feedbacks_rating (rating)");
        }
        if (!indexExists("system_feedbacks", "idx_system_feedbacks_created")) {
            exec("ALTER TABLE system_feedbacks ADD INDEX idx_system_feedbacks_created (created_at)");
        }
        if (!constraintExists("system_feedbacks", "fk_system_feedbacks_citizen")) {
            try {
                exec("ALTER TABLE system_feedbacks ADD CONSTRAINT fk_system_feedbacks_citizen FOREIGN KEY (citizen_id) REFERENCES users(id)");
            } catch (Exception e) {
                // Do not block startup because of legacy DB type drift; feedback module can still operate.
                log.warn("[SchemaCompatibility] Skip adding fk_system_feedbacks_citizen: {}", e.getMessage());
            }
        }
    }

    private void alignInventoryClassifications() {
        exec("""
                CREATE TABLE IF NOT EXISTS item_classifications (
                    id INT UNSIGNED NOT NULL AUTO_INCREMENT,
                    code VARCHAR(30) NOT NULL,
                    name VARCHAR(120) NOT NULL,
                    is_active TINYINT(1) NOT NULL DEFAULT 1,
                    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                    PRIMARY KEY (id),
                    UNIQUE KEY uk_item_classification_code (code),
                    KEY idx_item_classification_active (is_active)
                )
                """);

        if (!columnExists("item_categories", "classification_id")) {
            exec("ALTER TABLE item_categories ADD COLUMN classification_id INT UNSIGNED NULL AFTER unit");
        }
        if (!indexExists("item_categories", "idx_item_cat_classification")) {
            exec("ALTER TABLE item_categories ADD INDEX idx_item_cat_classification (classification_id)");
        }
        if (!constraintExists("item_categories", "fk_item_cat_classification")) {
            try {
                exec("ALTER TABLE item_categories ADD CONSTRAINT fk_item_cat_classification FOREIGN KEY (classification_id) REFERENCES item_classifications(id)");
            } catch (Exception e) {
                log.warn("[SchemaCompatibility] Skip adding fk_item_cat_classification: {}", e.getMessage());
            }
        }
    }

    private void alignInventoryUnits() {
        exec("""
                CREATE TABLE IF NOT EXISTS item_units (
                    id INT UNSIGNED NOT NULL AUTO_INCREMENT,
                    code VARCHAR(20) NOT NULL,
                    name VARCHAR(120) NOT NULL,
                    is_active TINYINT(1) NOT NULL DEFAULT 1,
                    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                    PRIMARY KEY (id),
                    UNIQUE KEY uk_item_units_code (code),
                    KEY idx_item_units_active (is_active)
                )
                """);
    }

    private void alignInventoryIssueRelations() {
        if (!columnExists("inventory_issues", "relief_request_id")) {
            exec("ALTER TABLE inventory_issues ADD COLUMN relief_request_id BIGINT UNSIGNED NULL AFTER updated_at");
        }
        if (!columnExists("inventory_issues", "assigned_team_id")) {
            exec("ALTER TABLE inventory_issues ADD COLUMN assigned_team_id BIGINT UNSIGNED NULL AFTER relief_request_id");
        }
        if (!columnExists("inventory_issues", "asset_id")) {
            exec("ALTER TABLE inventory_issues ADD COLUMN asset_id BIGINT UNSIGNED NULL AFTER assigned_team_id");
        }

        if (!indexExists("inventory_issues", "idx_issue_relief")) {
            exec("ALTER TABLE inventory_issues ADD INDEX idx_issue_relief (relief_request_id)");
        }
        if (!indexExists("inventory_issues", "idx_issue_team")) {
            exec("ALTER TABLE inventory_issues ADD INDEX idx_issue_team (assigned_team_id)");
        }
        if (!indexExists("inventory_issues", "idx_issue_asset")) {
            exec("ALTER TABLE inventory_issues ADD INDEX idx_issue_asset (asset_id)");
        }

        if (!constraintExists("inventory_issues", "fk_issue_relief")) {
            try {
                exec("ALTER TABLE inventory_issues ADD CONSTRAINT fk_issue_relief FOREIGN KEY (relief_request_id) REFERENCES relief_requests(id)");
            } catch (Exception e) {
                log.warn("[SchemaCompatibility] Skip adding fk_issue_relief: {}", e.getMessage());
            }
        }
        if (!constraintExists("inventory_issues", "fk_issue_team")) {
            try {
                exec("ALTER TABLE inventory_issues ADD CONSTRAINT fk_issue_team FOREIGN KEY (assigned_team_id) REFERENCES teams(id)");
            } catch (Exception e) {
                log.warn("[SchemaCompatibility] Skip adding fk_issue_team: {}", e.getMessage());
            }
        }
        if (!constraintExists("inventory_issues", "fk_issue_asset")) {
            try {
                exec("ALTER TABLE inventory_issues ADD CONSTRAINT fk_issue_asset FOREIGN KEY (asset_id) REFERENCES assets(id)");
            } catch (Exception e) {
                log.warn("[SchemaCompatibility] Skip adding fk_issue_asset: {}", e.getMessage());
            }
        }
    }

    private void alignReliefRequestWorkflow() {
        if (!columnExists("relief_requests", "delivery_status")) {
            exec("ALTER TABLE relief_requests ADD COLUMN delivery_status VARCHAR(40) NOT NULL DEFAULT 'REQUESTED' AFTER status");
        }
        if (!columnExists("relief_requests", "address_text")) {
            exec("ALTER TABLE relief_requests ADD COLUMN address_text VARCHAR(255) NULL AFTER target_area");
        }
        if (!columnExists("relief_requests", "latitude")) {
            exec("ALTER TABLE relief_requests ADD COLUMN latitude DOUBLE NULL AFTER address_text");
        }
        if (!columnExists("relief_requests", "longitude")) {
            exec("ALTER TABLE relief_requests ADD COLUMN longitude DOUBLE NULL AFTER latitude");
        }
        if (!columnExists("relief_requests", "location_description")) {
            exec("ALTER TABLE relief_requests ADD COLUMN location_description VARCHAR(500) NULL AFTER longitude");
        }
        if (!columnExists("relief_requests", "assigned_team_id")) {
            exec("ALTER TABLE relief_requests ADD COLUMN assigned_team_id BIGINT UNSIGNED NULL AFTER rescue_request_id");
        }
        if (!columnExists("relief_requests", "approved_by")) {
            exec("ALTER TABLE relief_requests ADD COLUMN approved_by BIGINT UNSIGNED NULL AFTER assigned_team_id");
        }
        if (!columnExists("relief_requests", "assigned_issue_id")) {
            exec("ALTER TABLE relief_requests ADD COLUMN assigned_issue_id BIGINT UNSIGNED NULL AFTER approved_by");
        }
        if (!columnExists("relief_requests", "delivery_note")) {
            exec("ALTER TABLE relief_requests ADD COLUMN delivery_note TEXT NULL AFTER note");
        }

        if (!indexExists("relief_requests", "idx_relief_delivery_status")) {
            exec("ALTER TABLE relief_requests ADD INDEX idx_relief_delivery_status (delivery_status)");
        }
        if (!indexExists("relief_requests", "idx_relief_assigned_team")) {
            exec("ALTER TABLE relief_requests ADD INDEX idx_relief_assigned_team (assigned_team_id)");
        }
        if (!indexExists("relief_requests", "idx_relief_assigned_issue")) {
            exec("ALTER TABLE relief_requests ADD INDEX idx_relief_assigned_issue (assigned_issue_id)");
        }
    }

    private void repairLegacyTextData() {
        repairKnownSystemSettings();
        repairKnownUserNames();
    }

    private void repairKnownSystemSettings() {
        Map<String, String> expectedSettings = Map.ofEntries(
                Map.entry("footerBrandName", "QU\u1ea2N L\u00dd C\u1ee8U H\u1ed8"),
                Map.entry("footerDescription", "H\u1ec7 th\u1ed1ng h\u1ed7 tr\u1ee3 c\u1ed9ng \u0111\u1ed3ng trong t\u00ecnh hu\u1ed1ng thi\u00ean tai kh\u1ea9n c\u1ea5p. Th\u00f4ng tin \u0111\u01b0\u1ee3c b\u1ea3o m\u1eadt v\u00e0 \u0111i\u1ec1u ph\u1ed1i theo quy \u0111\u1ecbnh c\u1ee7a c\u01a1 quan ch\u1ee9c n\u0103ng."),
                Map.entry("footerTermsLabel", "\u0110i\u1ec1u kho\u1ea3n s\u1eed d\u1ee5ng"),
                Map.entry("footerPrivacyLabel", "Ch\u00ednh s\u00e1ch b\u1ea3o m\u1eadt"),
                Map.entry("footerSupportLabel", "Li\u00ean h\u1ec7 h\u1ed7 tr\u1ee3"),
                Map.entry("footerCopyright", "\u00a9 2024 H\u1ec7 th\u1ed1ng Qu\u1ea3n l\u00fd C\u1ee9u h\u1ed9 - C\u1ee9u tr\u1ee3. B\u1ea3n quy\u1ec1n thu\u1ed9c v\u1ec1 C\u01a1 quan ch\u1ee7 qu\u1ea3n.")
        );

        for (Map.Entry<String, String> entry : expectedSettings.entrySet()) {
            String currentValue = jdbcTemplate.query(
                    """
                            SELECT COALESCE(setting_value, value_text)
                            FROM system_settings
                            WHERE COALESCE(setting_key, key_name) = ?
                            LIMIT 1
                            """,
                    rs -> rs.next() ? rs.getString(1) : null,
                    entry.getKey()
            );

            if (!TextNormalizationUtil.isLikelyMojibake(currentValue)) {
                continue;
            }

            jdbcTemplate.update(
                    """
                            UPDATE system_settings
                            SET setting_value = ?, value_text = ?, updated_at = NOW()
                            WHERE COALESCE(setting_key, key_name) = ?
                            """,
                    entry.getValue(),
                    entry.getValue(),
                    entry.getKey()
            );
            log.info("[SchemaCompatibility] Repaired mojibake system setting {}", entry.getKey());
        }
    }

    private void repairKnownUserNames() {
        Map<String, String> expectedUserNames = Map.ofEntries(
                Map.entry("admin@gmail.com", "Qu\u1ea3n tr\u1ecb vi\u00ean h\u1ec7 th\u1ed1ng"),
                Map.entry("conchimcu@gmail.com", "\u0110i\u1ec1u ph\u1ed1i vi\u00ean m\u1eb7c \u0111\u1ecbnh"),
                Map.entry("manager@gmail.com", "Qu\u1ea3n l\u00fd h\u1ec7 th\u1ed1ng"),
                Map.entry("team1@gmail.com", "\u0110\u1ed9i tr\u01b0\u1edfng \u0110\u1ed9i C\u1ee9u h\u1ed9 s\u1ed1 1"),
                Map.entry("team2@gmail.com", "\u0110\u1ed9i tr\u01b0\u1edfng \u0110\u1ed9i C\u1ee9u h\u1ed9 s\u1ed1 2"),
                Map.entry("team3@gmail.com", "\u0110\u1ed9i tr\u01b0\u1edfng \u0110\u1ed9i C\u1ee9u h\u1ed9 s\u1ed1 3"),
                Map.entry("nguyenanhtu27@gmail.com", "L\u00f2 V\u0103n Chi")
        );

        for (Map.Entry<String, String> entry : expectedUserNames.entrySet()) {
            String currentValue = jdbcTemplate.query(
                    """
                            SELECT full_name
                            FROM users
                            WHERE email = ?
                            LIMIT 1
                            """,
                    rs -> rs.next() ? rs.getString(1) : null,
                    entry.getKey()
            );

            if (!TextNormalizationUtil.isLikelyMojibake(currentValue)) {
                continue;
            }

            jdbcTemplate.update(
                    "UPDATE users SET full_name = ?, updated_at = NOW() WHERE email = ?",
                    entry.getValue(),
                    entry.getKey()
            );
            log.info("[SchemaCompatibility] Repaired mojibake user name for {}", entry.getKey());
        }
    }

    private void exec(String sql) {
        jdbcTemplate.execute(sql);
    }

    private void alignInventoryDocumentStatus(String tableName, String columnName, String defaultValue) {
        String columnType = getColumnType(tableName, columnName);
        if (columnType == null || columnType.contains("ASSIGNED")) {
            return;
        }

        exec("ALTER TABLE " + tableName + " MODIFY " + columnName + " VARCHAR(20) NOT NULL");
        exec("""
                UPDATE %s
                SET %s = '%s'
                WHERE %s NOT IN ('DRAFT','APPROVED','ASSIGNED','DONE','CANCELLED')
                """.formatted(tableName, columnName, defaultValue, columnName));
        exec("ALTER TABLE " + tableName + " MODIFY " + columnName
                + " ENUM('DRAFT','APPROVED','ASSIGNED','DONE','CANCELLED') NOT NULL DEFAULT '" + defaultValue + "'");
        log.info("[SchemaCompatibility] Aligned {}.{} enum", tableName, columnName);
    }

    private boolean columnExists(String tableName, String columnName) {
        Integer count = jdbcTemplate.queryForObject(
                """
                        SELECT COUNT(*)
                        FROM information_schema.COLUMNS
                        WHERE TABLE_SCHEMA = DATABASE()
                          AND TABLE_NAME = ?
                          AND COLUMN_NAME = ?
                        """,
                Integer.class,
                tableName,
                columnName
        );
        return count != null && count > 0;
    }

    private boolean indexExists(String tableName, String indexName) {
        Integer count = jdbcTemplate.queryForObject(
                """
                        SELECT COUNT(*)
                        FROM information_schema.STATISTICS
                        WHERE TABLE_SCHEMA = DATABASE()
                          AND TABLE_NAME = ?
                          AND INDEX_NAME = ?
                        """,
                Integer.class,
                tableName,
                indexName
        );
        return count != null && count > 0;
    }

    private boolean constraintExists(String tableName, String constraintName) {
        Integer count = jdbcTemplate.queryForObject(
                """
                        SELECT COUNT(*)
                        FROM information_schema.TABLE_CONSTRAINTS
                        WHERE TABLE_SCHEMA = DATABASE()
                          AND TABLE_NAME = ?
                          AND CONSTRAINT_NAME = ?
                        """,
                Integer.class,
                tableName,
                constraintName
        );
        return count != null && count > 0;
    }

    private String getColumnType(String tableName, String columnName) {
        return jdbcTemplate.query(
                        """
                                SELECT COLUMN_TYPE
                                FROM information_schema.COLUMNS
                                WHERE TABLE_SCHEMA = DATABASE()
                                  AND TABLE_NAME = ?
                                  AND COLUMN_NAME = ?
                                LIMIT 1
                                """,
                        rs -> rs.next() ? rs.getString("COLUMN_TYPE") : null,
                        tableName,
                        columnName
                );
    }
}
