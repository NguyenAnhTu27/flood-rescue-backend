package com.floodrescue.config.schema;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
@Order(Ordered.HIGHEST_PRECEDENCE)
@ConditionalOnProperty(value = "app.schema-compatibility.enabled", havingValue = "true", matchIfMissing = true)
public class SchemaCompatibilityRunner implements ApplicationRunner {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public void run(ApplicationArguments args) {
        try {
            alignUsersTable();
            alignTeamsTable();
            alignRescueRequestStatusEnums();
            alignRescueRequestEmergencyColumns();
            alignRescueRequestLocationColumns();
            alignRescueResultConfirmationColumns();
            alignNotificationColumns();
            alignTeamLocationColumns();
            alignSystemFeedbackTable();
            alignSystemFeedbackRepliesTable();
            alignInventoryClassifications();
            alignInventoryUnits();
            alignReliefRequestWorkflow();
            alignAuthTokenTables();
            alignUsersEncryptedColumns();
            alignDistributionsTable();
            alignInventoryIssuesColumns();
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
        if (!columnExists("users", "rescue_request_blocked")) {
            exec("ALTER TABLE users ADD COLUMN rescue_request_blocked TINYINT(1) NOT NULL DEFAULT 0 AFTER is_leader");
            log.info("[SchemaCompatibility] Added users.rescue_request_blocked");
        }
        if (!columnExists("users", "rescue_request_blocked_reason")) {
            exec("ALTER TABLE users ADD COLUMN rescue_request_blocked_reason TEXT NULL AFTER rescue_request_blocked");
            log.info("[SchemaCompatibility] Added users.rescue_request_blocked_reason");
        }
    }

    private void alignTeamsTable() {
        if (!columnExists("teams", "description")) {
            exec("ALTER TABLE teams ADD COLUMN description VARCHAR(255) NULL");
            log.info("[SchemaCompatibility] Added teams.description");
        }

        String statusColumnType = getColumnType("teams", "status");
        if (statusColumnType != null &&
                (statusColumnType.contains("ACTIVE")
                        || statusColumnType.contains("INACTIVE")
                        || statusColumnType.startsWith("enum("))) {
            exec("ALTER TABLE teams MODIFY status VARCHAR(20) NULL");
            exec("""
                    UPDATE teams
                    SET status = CASE
                        WHEN status IS NULL OR TRIM(status) = '' THEN '1'
                        WHEN UPPER(TRIM(status)) IN ('ACTIVE', '1', 'TRUE') THEN '1'
                        WHEN UPPER(TRIM(status)) IN ('INACTIVE', '0', 'FALSE') THEN '0'
                        ELSE '1'
                    END
                    """);
            exec("ALTER TABLE teams MODIFY status TINYINT NOT NULL DEFAULT 1");
            log.info("[SchemaCompatibility] Aligned teams.status to tinyint");
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
            // Use a wide temporary type to avoid truncation when legacy values are longer than 20 chars.
            exec("ALTER TABLE teams MODIFY team_type VARCHAR(100) NULL");
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
                WHERE team_type IS NULL
                   OR TRIM(team_type) = ''
                   OR UPPER(TRIM(team_type)) NOT IN ('RESCUE', 'RELIEF', 'MEDICAL')
                    """);
            exec("ALTER TABLE teams MODIFY team_type ENUM('RESCUE','RELIEF','MEDICAL') NOT NULL");
            log.info("[SchemaCompatibility] Aligned teams.team_type enum");
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
        if (!columnExists("rescue_requests", "contact_phone")) {
            exec("ALTER TABLE rescue_requests ADD COLUMN contact_phone VARCHAR(20) NULL AFTER description");
        }
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
        if (!tableExists("notifications")) {
            log.warn("[SchemaCompatibility] Skip notification alignment because table notifications does not exist");
            return;
        }

        if (!columnExists("notifications", "action_status")) {
            exec("ALTER TABLE notifications ADD COLUMN action_status VARCHAR(40) NULL AFTER acknowledged_at");
        }
        if (!columnExists("notifications", "action_note")) {
            exec("ALTER TABLE notifications ADD COLUMN action_note TEXT NULL AFTER action_status");
        }
        if (!columnExists("notifications", "queue_request_id")) {
            exec("ALTER TABLE notifications ADD COLUMN queue_request_id BIGINT UNSIGNED NULL AFTER action_note");
        }
        if (!columnExists("notifications", "source_team_id")) {
            exec("ALTER TABLE notifications ADD COLUMN source_team_id BIGINT UNSIGNED NULL AFTER queue_request_id");
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
                    deleted TINYINT(1) NOT NULL DEFAULT 0,
                    deleted_at DATETIME NULL,
                    deleted_by_user_id BIGINT UNSIGNED NULL,
                    delete_reason VARCHAR(500) NULL,
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
        if (!columnExists("system_feedbacks", "deleted")) {
            exec("ALTER TABLE system_feedbacks ADD COLUMN deleted TINYINT(1) NOT NULL DEFAULT 0 AFTER created_at");
        }
        if (!columnExists("system_feedbacks", "deleted_at")) {
            exec("ALTER TABLE system_feedbacks ADD COLUMN deleted_at DATETIME NULL AFTER deleted");
        }
        if (!columnExists("system_feedbacks", "deleted_by_user_id")) {
            exec("ALTER TABLE system_feedbacks ADD COLUMN deleted_by_user_id " + expectedUserIdType() + " NULL AFTER deleted_at");
        }
        if (!columnExists("system_feedbacks", "delete_reason")) {
            exec("ALTER TABLE system_feedbacks ADD COLUMN delete_reason VARCHAR(500) NULL AFTER deleted_by_user_id");
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
        if (!indexExists("system_feedbacks", "idx_system_feedbacks_deleted")) {
            exec("ALTER TABLE system_feedbacks ADD INDEX idx_system_feedbacks_deleted (deleted)");
        }
        if (!constraintExists("system_feedbacks", "fk_system_feedbacks_citizen")) {
            try {
                exec("ALTER TABLE system_feedbacks ADD CONSTRAINT fk_system_feedbacks_citizen FOREIGN KEY (citizen_id) REFERENCES users(id)");
            } catch (Exception e) {
                // Do not block startup because of legacy DB type drift; feedback module can still operate.
                log.warn("[SchemaCompatibility] Skip adding fk_system_feedbacks_citizen: {}", e.getMessage());
            }
        }
        if (!constraintExists("system_feedbacks", "fk_system_feedbacks_deleted_by")) {
            try {
                exec("ALTER TABLE system_feedbacks ADD CONSTRAINT fk_system_feedbacks_deleted_by FOREIGN KEY (deleted_by_user_id) REFERENCES users(id)");
            } catch (Exception e) {
                log.warn("[SchemaCompatibility] Skip adding fk_system_feedbacks_deleted_by: {}", e.getMessage());
            }
        }
    }

    private String expectedUserIdType() {
        String userIdColumnType = getColumnType("users", "id");
        return (userIdColumnType == null || userIdColumnType.isBlank())
                ? "BIGINT UNSIGNED"
                : userIdColumnType.toUpperCase();
    }

    private void alignSystemFeedbackRepliesTable() {
        String userIdColumnType = getColumnType("users", "id");
        String expectedUserIdType = (userIdColumnType == null || userIdColumnType.isBlank())
                ? "BIGINT UNSIGNED"
                : userIdColumnType.toUpperCase();

        String feedbackIdColumnType = getColumnType("system_feedbacks", "id");
        String expectedFeedbackIdType = (feedbackIdColumnType == null || feedbackIdColumnType.isBlank())
                ? "BIGINT"
                : feedbackIdColumnType.toUpperCase();

        exec("""
                CREATE TABLE IF NOT EXISTS system_feedback_replies (
                    id BIGINT NOT NULL AUTO_INCREMENT,
                    feedback_id BIGINT NOT NULL,
                    user_id BIGINT UNSIGNED NOT NULL,
                    role_code VARCHAR(30) NOT NULL,
                    content TEXT NOT NULL,
                    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                    PRIMARY KEY (id),
                    KEY idx_feedback_replies_feedback (feedback_id),
                    KEY idx_feedback_replies_user (user_id),
                    KEY idx_feedback_replies_created (created_at)
                )
                """);

        String currentFeedbackIdType = getColumnType("system_feedback_replies", "feedback_id");
        if (currentFeedbackIdType == null || !currentFeedbackIdType.equalsIgnoreCase(expectedFeedbackIdType)) {
            exec("ALTER TABLE system_feedback_replies MODIFY feedback_id " + expectedFeedbackIdType + " NOT NULL");
        }

        String currentUserIdType = getColumnType("system_feedback_replies", "user_id");
        if (currentUserIdType == null || !currentUserIdType.equalsIgnoreCase(expectedUserIdType)) {
            exec("ALTER TABLE system_feedback_replies MODIFY user_id " + expectedUserIdType + " NOT NULL");
        }

        if (!indexExists("system_feedback_replies", "idx_feedback_replies_feedback")) {
            exec("ALTER TABLE system_feedback_replies ADD INDEX idx_feedback_replies_feedback (feedback_id)");
        }
        if (!indexExists("system_feedback_replies", "idx_feedback_replies_user")) {
            exec("ALTER TABLE system_feedback_replies ADD INDEX idx_feedback_replies_user (user_id)");
        }
        if (!indexExists("system_feedback_replies", "idx_feedback_replies_created")) {
            exec("ALTER TABLE system_feedback_replies ADD INDEX idx_feedback_replies_created (created_at)");
        }

        if (!constraintExists("system_feedback_replies", "fk_feedback_replies_feedback")) {
            try {
                exec("ALTER TABLE system_feedback_replies ADD CONSTRAINT fk_feedback_replies_feedback FOREIGN KEY (feedback_id) REFERENCES system_feedbacks(id) ON DELETE CASCADE");
            } catch (Exception e) {
                log.warn("[SchemaCompatibility] Skip adding fk_feedback_replies_feedback: {}", e.getMessage());
            }
        }

        if (!constraintExists("system_feedback_replies", "fk_feedback_replies_user")) {
            try {
                exec("ALTER TABLE system_feedback_replies ADD CONSTRAINT fk_feedback_replies_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE");
            } catch (Exception e) {
                log.warn("[SchemaCompatibility] Skip adding fk_feedback_replies_user: {}", e.getMessage());
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

    private void alignReliefRequestWorkflow() {
        if (!columnExists("relief_requests", "delivery_status")) {
            exec("ALTER TABLE relief_requests ADD COLUMN delivery_status VARCHAR(40) NOT NULL DEFAULT 'REQUESTED' AFTER status");
        }
        if (!columnExists("relief_requests", "phone")) {
            exec("ALTER TABLE relief_requests ADD COLUMN phone VARCHAR(20) NULL AFTER target_area");
        }
        if (!columnExists("relief_requests", "priority")) {
            exec("ALTER TABLE relief_requests ADD COLUMN priority VARCHAR(10) NULL AFTER phone");
        }
        if (!columnExists("relief_requests", "people_count")) {
            exec("ALTER TABLE relief_requests ADD COLUMN people_count INT NULL AFTER priority");
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

        // Backfill legacy rejected/cancelled requests to keep status fields consistent.
        exec("""
                UPDATE relief_requests
                SET delivery_status = 'REJECTED'
                WHERE status = 'CANCELLED'
                  AND (delivery_status IS NULL OR delivery_status IN ('REQUESTED', 'MANAGER_APPROVED'))
                """);

        if (columnExists("relief_requests", "assigned_issue_id")
                && tableExists("inventory_issues")
                && !constraintExists("relief_requests", "fk_relief_assigned_issue")) {
            try {
                exec("ALTER TABLE relief_requests ADD CONSTRAINT fk_relief_assigned_issue FOREIGN KEY (assigned_issue_id) REFERENCES inventory_issues(id)");
            } catch (Exception e) {
                log.warn("[SchemaCompatibility] Skip adding fk_relief_assigned_issue: {}", e.getMessage());
            }
        }
    }

    private void alignAuthTokenTables() {
        String userIdColumnType = getColumnType("users", "id");
        String expectedUserIdType = (userIdColumnType == null || userIdColumnType.isBlank())
                ? "BIGINT UNSIGNED"
                : userIdColumnType.toUpperCase();

        exec("""
                CREATE TABLE IF NOT EXISTS auth_refresh_tokens (
                    id BIGINT NOT NULL AUTO_INCREMENT,
                    user_id BIGINT UNSIGNED NOT NULL,
                    token_hash VARCHAR(128) NOT NULL,
                    expires_at DATETIME NOT NULL,
                    revoked TINYINT(1) NOT NULL DEFAULT 0,
                    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                    revoked_at DATETIME NULL,
                    user_agent VARCHAR(255) NULL,
                    ip_address VARCHAR(64) NULL,
                    PRIMARY KEY (id),
                    UNIQUE KEY uk_auth_refresh_tokens_hash (token_hash),
                    KEY idx_auth_refresh_tokens_user (user_id),
                    KEY idx_auth_refresh_tokens_expires (expires_at)
                )
                """);

        exec("""
                CREATE TABLE IF NOT EXISTS password_reset_tokens (
                    id BIGINT NOT NULL AUTO_INCREMENT,
                    user_id BIGINT UNSIGNED NOT NULL,
                    token_hash VARCHAR(128) NOT NULL,
                    expires_at DATETIME NOT NULL,
                    used TINYINT(1) NOT NULL DEFAULT 0,
                    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                    used_at DATETIME NULL,
                    PRIMARY KEY (id),
                    UNIQUE KEY uk_password_reset_tokens_hash (token_hash),
                    KEY idx_password_reset_tokens_user (user_id),
                    KEY idx_password_reset_tokens_expires (expires_at)
                )
                """);

        String refreshUserIdType = getColumnType("auth_refresh_tokens", "user_id");
        if (refreshUserIdType == null || !refreshUserIdType.equalsIgnoreCase(expectedUserIdType)) {
            exec("ALTER TABLE auth_refresh_tokens MODIFY user_id " + expectedUserIdType + " NOT NULL");
        }

        String resetUserIdType = getColumnType("password_reset_tokens", "user_id");
        if (resetUserIdType == null || !resetUserIdType.equalsIgnoreCase(expectedUserIdType)) {
            exec("ALTER TABLE password_reset_tokens MODIFY user_id " + expectedUserIdType + " NOT NULL");
        }

        if (!constraintExists("auth_refresh_tokens", "fk_auth_refresh_tokens_user")) {
            try {
                exec("ALTER TABLE auth_refresh_tokens ADD CONSTRAINT fk_auth_refresh_tokens_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE");
            } catch (Exception e) {
                log.warn("[SchemaCompatibility] Skip adding fk_auth_refresh_tokens_user: {}", e.getMessage());
            }
        }

        if (!constraintExists("password_reset_tokens", "fk_password_reset_tokens_user")) {
            try {
                exec("ALTER TABLE password_reset_tokens ADD CONSTRAINT fk_password_reset_tokens_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE");
            } catch (Exception e) {
                log.warn("[SchemaCompatibility] Skip adding fk_password_reset_tokens_user: {}", e.getMessage());
            }
        }
    }

    private void alignUsersEncryptedColumns() {
        String phoneType = getColumnType("users", "phone");
        if (phoneType != null && phoneType.toLowerCase().contains("varchar(") && !phoneType.toLowerCase().contains("varchar(500)")) {
            exec("ALTER TABLE users MODIFY phone VARCHAR(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL");
            log.info("[SchemaCompatibility] Widened users.phone to varchar(500) for encrypted storage");
        }
        String emailType = getColumnType("users", "email");
        if (emailType != null && emailType.toLowerCase().contains("varchar(") && !emailType.toLowerCase().contains("varchar(512)")) {
            exec("ALTER TABLE users MODIFY email VARCHAR(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL");
            log.info("[SchemaCompatibility] Widened users.email to varchar(512) for encrypted storage");
        }
    }

    private void alignDistributionsTable() {
        // Rename assigned_team_id → team_id
        if (columnExists("distributions", "assigned_team_id") && !columnExists("distributions", "team_id")) {
            exec("ALTER TABLE distributions RENAME COLUMN assigned_team_id TO team_id");
            log.info("[SchemaCompatibility] Renamed distributions.assigned_team_id to team_id");
        } else if (!columnExists("distributions", "team_id")) {
            exec("ALTER TABLE distributions ADD COLUMN team_id BIGINT UNSIGNED DEFAULT NULL");
            log.info("[SchemaCompatibility] Added distributions.team_id");
        }

        // Fix code column length varchar(30) → varchar(40)
        String codeType = getColumnType("distributions", "code");
        if (codeType != null && codeType.toLowerCase().contains("varchar(30)")) {
            exec("ALTER TABLE distributions MODIFY code VARCHAR(40) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL");
            log.info("[SchemaCompatibility] Widened distributions.code to varchar(40)");
        }

        // Rename unique key uk_dist_code → uk_distribution_code
        if (indexExists("distributions", "uk_dist_code") && !indexExists("distributions", "uk_distribution_code")) {
            exec("ALTER TABLE distributions DROP INDEX uk_dist_code, ADD UNIQUE KEY uk_distribution_code (code)");
            log.info("[SchemaCompatibility] Renamed distributions unique key to uk_distribution_code");
        }

        // Align status enum to InventoryDocumentStatus
        String statusType = getColumnType("distributions", "status");
        if (statusType != null && (!statusType.contains("DRAFT") || statusType.contains("PLANNED") || statusType.contains("IN_TRANSIT") || statusType.contains("DELIVERED"))) {
            exec("ALTER TABLE distributions MODIFY status VARCHAR(20) NOT NULL DEFAULT 'DRAFT'");
            exec("""
                    UPDATE distributions SET status = CASE status
                        WHEN 'PLANNED' THEN 'DRAFT'
                        WHEN 'IN_TRANSIT' THEN 'APPROVED'
                        WHEN 'DELIVERED' THEN 'DONE'
                        ELSE status
                    END
                    WHERE status NOT IN ('DRAFT','ASSIGNED','APPROVED','DONE','CANCELLED')
                    """);
            exec("ALTER TABLE distributions MODIFY status ENUM('DRAFT','ASSIGNED','APPROVED','DONE','CANCELLED') CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'DRAFT'");
            log.info("[SchemaCompatibility] Aligned distributions.status enum");
        }

        // Make relief_request_id nullable (was NOT NULL in old schema)
        try {
            exec("ALTER TABLE distributions MODIFY relief_request_id BIGINT UNSIGNED DEFAULT NULL");
        } catch (Exception e) {
            log.warn("[SchemaCompatibility] Skip modifying distributions.relief_request_id: {}", e.getMessage());
        }

        // Add missing columns
        if (!columnExists("distributions", "created_by")) {
            exec("ALTER TABLE distributions ADD COLUMN created_by BIGINT UNSIGNED NOT NULL DEFAULT 0");
            log.info("[SchemaCompatibility] Added distributions.created_by");
        }
        if (!columnExists("distributions", "issue_ref_code")) {
            exec("ALTER TABLE distributions ADD COLUMN issue_ref_code VARCHAR(40) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL");
            log.info("[SchemaCompatibility] Added distributions.issue_ref_code");
        }
        if (!columnExists("distributions", "asset_id")) {
            exec("ALTER TABLE distributions ADD COLUMN asset_id BIGINT UNSIGNED DEFAULT NULL");
            log.info("[SchemaCompatibility] Added distributions.asset_id");
        }
        if (!columnExists("distributions", "receiver_name")) {
            exec("ALTER TABLE distributions ADD COLUMN receiver_name VARCHAR(120) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL");
            log.info("[SchemaCompatibility] Added distributions.receiver_name");
        }
        if (!columnExists("distributions", "receiver_phone")) {
            exec("ALTER TABLE distributions ADD COLUMN receiver_phone VARCHAR(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL");
            log.info("[SchemaCompatibility] Added distributions.receiver_phone");
        }
        if (!columnExists("distributions", "delivery_address")) {
            exec("ALTER TABLE distributions ADD COLUMN delivery_address VARCHAR(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL");
            log.info("[SchemaCompatibility] Added distributions.delivery_address");
        }
        if (!columnExists("distributions", "eta")) {
            exec("ALTER TABLE distributions ADD COLUMN eta DATETIME DEFAULT NULL");
            log.info("[SchemaCompatibility] Added distributions.eta");
        }
        if (!columnExists("distributions", "priority")) {
            exec("ALTER TABLE distributions ADD COLUMN priority VARCHAR(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL");
            log.info("[SchemaCompatibility] Added distributions.priority");
        }
        if (!columnExists("distributions", "note")) {
            exec("ALTER TABLE distributions ADD COLUMN note TEXT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL");
            log.info("[SchemaCompatibility] Added distributions.note");
        }

        // Add indexes for new FKs
        if (columnExists("distributions", "asset_id") && !indexExists("distributions", "idx_distribution_asset")) {
            exec("ALTER TABLE distributions ADD INDEX idx_distribution_asset (asset_id)");
        }
        if (columnExists("distributions", "team_id") && !indexExists("distributions", "idx_distribution_team")) {
            exec("ALTER TABLE distributions ADD INDEX idx_distribution_team (team_id)");
        }

        // Add FK for asset_id
        if (columnExists("distributions", "asset_id") && !constraintExists("distributions", "fk_distribution_asset")) {
            try {
                exec("ALTER TABLE distributions ADD CONSTRAINT fk_distribution_asset FOREIGN KEY (asset_id) REFERENCES assets (id)");
            } catch (Exception e) {
                log.warn("[SchemaCompatibility] Skip adding fk_distribution_asset: {}", e.getMessage());
            }
        }
    }

    private void alignInventoryIssuesColumns() {
        // Add ASSIGNED to status enum
        String statusType = getColumnType("inventory_issues", "status");
        if (statusType != null && !statusType.contains("ASSIGNED")) {
            exec("ALTER TABLE inventory_issues MODIFY status VARCHAR(20) NOT NULL DEFAULT 'DRAFT'");
            exec("ALTER TABLE inventory_issues MODIFY status ENUM('DRAFT','ASSIGNED','APPROVED','DONE','CANCELLED') CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'DRAFT'");
            log.info("[SchemaCompatibility] Added ASSIGNED to inventory_issues.status enum");
        }

        // Add relief_request_id
        if (!columnExists("inventory_issues", "relief_request_id")) {
            exec("ALTER TABLE inventory_issues ADD COLUMN relief_request_id BIGINT UNSIGNED DEFAULT NULL");
            exec("ALTER TABLE inventory_issues ADD INDEX idx_issue_relief (relief_request_id)");
            if (!constraintExists("inventory_issues", "fk_issue_relief")) {
                try {
                    exec("ALTER TABLE inventory_issues ADD CONSTRAINT fk_issue_relief FOREIGN KEY (relief_request_id) REFERENCES relief_requests (id)");
                } catch (Exception e) {
                    log.warn("[SchemaCompatibility] Skip adding fk_issue_relief: {}", e.getMessage());
                }
            }
            log.info("[SchemaCompatibility] Added inventory_issues.relief_request_id");
        }

        // Add assigned_team_id
        if (!columnExists("inventory_issues", "assigned_team_id")) {
            exec("ALTER TABLE inventory_issues ADD COLUMN assigned_team_id BIGINT UNSIGNED DEFAULT NULL");
            exec("ALTER TABLE inventory_issues ADD INDEX idx_issue_team (assigned_team_id)");
            if (!constraintExists("inventory_issues", "fk_issue_team")) {
                try {
                    exec("ALTER TABLE inventory_issues ADD CONSTRAINT fk_issue_team FOREIGN KEY (assigned_team_id) REFERENCES teams (id)");
                } catch (Exception e) {
                    log.warn("[SchemaCompatibility] Skip adding fk_issue_team: {}", e.getMessage());
                }
            }
            log.info("[SchemaCompatibility] Added inventory_issues.assigned_team_id");
        }

        // Add asset_id
        if (!columnExists("inventory_issues", "asset_id")) {
            exec("ALTER TABLE inventory_issues ADD COLUMN asset_id BIGINT UNSIGNED DEFAULT NULL");
            exec("ALTER TABLE inventory_issues ADD INDEX idx_issue_asset (asset_id)");
            if (!constraintExists("inventory_issues", "fk_issue_asset")) {
                try {
                    exec("ALTER TABLE inventory_issues ADD CONSTRAINT fk_issue_asset FOREIGN KEY (asset_id) REFERENCES assets (id)");
                } catch (Exception e) {
                    log.warn("[SchemaCompatibility] Skip adding fk_issue_asset: {}", e.getMessage());
                }
            }
            log.info("[SchemaCompatibility] Added inventory_issues.asset_id");
        }
    }

    private void exec(String sql) {
        jdbcTemplate.execute(sql);
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

    private boolean tableExists(String tableName) {
        Integer count = jdbcTemplate.queryForObject(
                """
                        SELECT COUNT(*)
                        FROM information_schema.TABLES
                        WHERE TABLE_SCHEMA = DATABASE()
                          AND TABLE_NAME = ?
                        """,
                Integer.class,
                tableName
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
