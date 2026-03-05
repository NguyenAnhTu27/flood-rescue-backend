-- ================================================================
-- RESCUE MANAGEMENT DATABASE SCHEMA
-- Phiên bản: 2.0 | Java 21 + Spring Boot 3.4.3
-- Chạy file này trong MySQL Workbench hoặc CLI
-- ================================================================

CREATE DATABASE IF NOT EXISTS rescue_management_db
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE rescue_management_db;

-- ================================================================
-- 1. TEAMS
-- ================================================================
CREATE TABLE IF NOT EXISTS teams (
    id         BIGINT       NOT NULL AUTO_INCREMENT PRIMARY KEY,
    code       VARCHAR(50)  NOT NULL UNIQUE          COMMENT 'T-001',
    name       VARCHAR(100) NOT NULL,
    team_type  VARCHAR(50)                           COMMENT 'rescue|medical|logistics|search',
    status     VARCHAR(20)  NOT NULL DEFAULT 'active' COMMENT 'active|busy|inactive',
    created_at DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ================================================================
-- 2. USERS
-- ================================================================
CREATE TABLE IF NOT EXISTS users (
    id            BIGINT       NOT NULL AUTO_INCREMENT PRIMARY KEY,
    full_name     VARCHAR(100) NOT NULL,
    phone         VARCHAR(20),
    email         VARCHAR(150) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL              COMMENT 'BCrypt hash hoặc plain (dev)',
    role          VARCHAR(50)  NOT NULL DEFAULT 'citizen'
                               COMMENT 'admin|dispatcher|team_leader|member|citizen',
    team_id       BIGINT                             COMMENT 'FK → teams.id',
    status        VARCHAR(20)  NOT NULL DEFAULT 'active' COMMENT 'active|inactive|suspended',
    created_at    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_users_team FOREIGN KEY (team_id) REFERENCES teams(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ================================================================
-- 3. ASSETS (Phương tiện / trang thiết bị)
-- ================================================================
CREATE TABLE IF NOT EXISTS assets (
    id            BIGINT       NOT NULL AUTO_INCREMENT PRIMARY KEY,
    code          VARCHAR(50)  NOT NULL UNIQUE       COMMENT 'AS-001',
    name          VARCHAR(100) NOT NULL,
    asset_type    VARCHAR(50)                        COMMENT 'vehicle|equipment|medical|other',
    status        VARCHAR(20)  NOT NULL DEFAULT 'available'
                               COMMENT 'available|in_use|maintenance',
    assigned_team BIGINT                             COMMENT 'FK → teams.id',
    created_at    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_assets_team FOREIGN KEY (assigned_team) REFERENCES teams(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ================================================================
-- 3.1. RESCUE VEHICLES (Phương tiện cứu hộ do điều phối thực hiện)
-- ================================================================
CREATE TABLE IF NOT EXISTS rescue_vehicles (
    id               BIGINT       NOT NULL AUTO_INCREMENT PRIMARY KEY,
    code             VARCHAR(50)  NOT NULL UNIQUE    COMMENT 'RV-001, RV-002, ...',
    name             VARCHAR(100) NOT NULL,
    vehicle_type     VARCHAR(50)  NOT NULL           COMMENT 'rescue_person|transport_supplies|technical_support|small_equipment',
    icon             VARCHAR(10)                     COMMENT '🚑|🚛|🏗|📦',
    description      TEXT,
    status           VARCHAR(20)  NOT NULL DEFAULT 'available'
                                  COMMENT 'available|in_use|maintenance|retired',
    capacity         INT                             COMMENT 'Sức chứa, số lượng người hoặc kg',
    dispatcher_id    BIGINT                          COMMENT 'FK → users.id (điều phối viên vận hành)',
    assigned_team_id BIGINT                          COMMENT 'FK → teams.id',
    license_plate    VARCHAR(20)                     COMMENT 'Biển số xe',
    contact_number   VARCHAR(20),
    created_at       DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at       DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_rv_dispatcher FOREIGN KEY (dispatcher_id) REFERENCES users(id) ON DELETE SET NULL,
    CONSTRAINT fk_rv_team       FOREIGN KEY (assigned_team_id) REFERENCES teams(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Index cho tìm kiếm nhanh
CREATE INDEX idx_rv_status ON rescue_vehicles(status);
CREATE INDEX idx_rv_type ON rescue_vehicles(vehicle_type);
CREATE INDEX idx_rv_dispatcher ON rescue_vehicles(dispatcher_id);
CREATE INDEX idx_rv_team ON rescue_vehicles(assigned_team_id);

-- ================================================================
-- 4. RESCUE REQUESTS (Yêu cầu cứu hộ)
-- ================================================================
CREATE TABLE IF NOT EXISTS rescue_requests (
    id                  BIGINT       NOT NULL AUTO_INCREMENT PRIMARY KEY,
    code                VARCHAR(50)  NOT NULL UNIQUE COMMENT 'RR-2026-0001',
    caller_id           BIGINT       NOT NULL        COMMENT 'FK → users.id',
    status              VARCHAR(20)  NOT NULL DEFAULT 'pending'
                                     COMMENT 'pending|in_progress|completed|cancelled',
    priority            VARCHAR(20)  NOT NULL DEFAULT 'medium'
                                     COMMENT 'low|medium|high|critical',
    affected_people_count INT,
    description         TEXT,
    address_text        VARCHAR(500),
    location_verified   TINYINT(1)   NOT NULL DEFAULT 0,
    transfer_request_id BIGINT                       COMMENT 'FK → rescue_requests.id (self)',
    created_at          DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_rr_caller   FOREIGN KEY (caller_id)           REFERENCES users(id),
    CONSTRAINT fk_rr_transfer FOREIGN KEY (transfer_request_id) REFERENCES rescue_requests(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Index cho duplicate check (caller_id + status)
CREATE INDEX idx_rr_caller_status ON rescue_requests(caller_id, status);

-- ================================================================
-- 5. RESCUE REQUEST ATTACHMENTS
-- ================================================================
CREATE TABLE IF NOT EXISTS rescue_request_attachments (
    id                BIGINT       NOT NULL AUTO_INCREMENT PRIMARY KEY,
    rescue_request_id BIGINT       NOT NULL,
    file_url          VARCHAR(500) NOT NULL,
    file_type         VARCHAR(50)              COMMENT 'image|video|document',
    created_at        DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_rra_request FOREIGN KEY (rescue_request_id)
        REFERENCES rescue_requests(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ================================================================
-- 6. RESCUE REQUEST LOGS (Audit trail)
-- ================================================================
CREATE TABLE IF NOT EXISTS rescue_request_logs (
    id                BIGINT      NOT NULL AUTO_INCREMENT PRIMARY KEY,
    rescue_request_id BIGINT      NOT NULL,
    actor_id          BIGINT                COMMENT 'FK → users.id',
    action            VARCHAR(50) NOT NULL  COMMENT 'created|assigned|in_progress|completed|cancelled',
    note              TEXT,
    created_at        DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_rrl_request FOREIGN KEY (rescue_request_id)
        REFERENCES rescue_requests(id) ON DELETE CASCADE,
    CONSTRAINT fk_rrl_actor FOREIGN KEY (actor_id)
        REFERENCES users(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ================================================================
-- 7. TASK GROUPS
-- ================================================================
CREATE TABLE IF NOT EXISTS task_groups (
    id               BIGINT      NOT NULL AUTO_INCREMENT PRIMARY KEY,
    code             VARCHAR(50) NOT NULL UNIQUE COMMENT 'TG-2026-0001',
    status           VARCHAR(20) NOT NULL DEFAULT 'idle'
                                  COMMENT 'idle|assigned|in_progress|completed',
    assigned_team_id BIGINT                COMMENT 'FK → teams.id',
    created_by       BIGINT                COMMENT 'FK → users.id',
    created_at       DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at       DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_tg_team FOREIGN KEY (assigned_team_id) REFERENCES teams(id) ON DELETE SET NULL,
    CONSTRAINT fk_tg_creator FOREIGN KEY (created_by)    REFERENCES users(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Index để tìm task_group theo team + status (dùng cho isTeamBusy)
CREATE INDEX idx_tg_team_status ON task_groups(assigned_team_id, status);

-- ================================================================
-- 8. TASK GROUP REQUESTS (N-N: task_group ↔ rescue_request)
-- ================================================================
CREATE TABLE IF NOT EXISTS task_group_requests (
    id                BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    task_group_id     BIGINT NOT NULL COMMENT 'FK → task_groups.id',
    rescue_request_id BIGINT NOT NULL COMMENT 'FK → rescue_requests.id',
    CONSTRAINT fk_tgr_group   FOREIGN KEY (task_group_id)
        REFERENCES task_groups(id) ON DELETE CASCADE,
    CONSTRAINT fk_tgr_request FOREIGN KEY (rescue_request_id)
        REFERENCES rescue_requests(id) ON DELETE CASCADE,
    UNIQUE KEY uq_tgr (task_group_id, rescue_request_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ================================================================
-- 9. RESCUE ASSIGNMENTS
-- ================================================================
CREATE TABLE IF NOT EXISTS rescue_assignments (
    id            BIGINT   NOT NULL AUTO_INCREMENT PRIMARY KEY,
    task_group_id BIGINT   NOT NULL COMMENT 'FK → task_groups.id',
    team_id       BIGINT   NOT NULL COMMENT 'FK → teams.id',
    asset_id      BIGINT            COMMENT 'FK → assets.id (optional)',
    assigned_by   BIGINT            COMMENT 'FK → users.id',
    assigned_at   DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_ra_task_group FOREIGN KEY (task_group_id) REFERENCES task_groups(id),
    CONSTRAINT fk_ra_team       FOREIGN KEY (team_id)       REFERENCES teams(id),
    CONSTRAINT fk_ra_asset      FOREIGN KEY (asset_id)      REFERENCES assets(id) ON DELETE SET NULL,
    CONSTRAINT fk_ra_assigner   FOREIGN KEY (assigned_by)   REFERENCES users(id)  ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Index để isTeamBusy query nhanh
CREATE INDEX idx_ra_team ON rescue_assignments(team_id);

-- ================================================================
-- 10. ITEM CATEGORIES (Loại vật tư)
-- ================================================================
CREATE TABLE IF NOT EXISTS item_categories (
    id   BIGINT      NOT NULL AUTO_INCREMENT PRIMARY KEY,
    code VARCHAR(50) NOT NULL UNIQUE,
    name VARCHAR(100) NOT NULL,
    unit VARCHAR(30)            COMMENT 'kg|thùng|cái|lít'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ================================================================
-- 11. STOCK BALANCES (Tồn kho)
-- ================================================================
CREATE TABLE IF NOT EXISTS stock_balances (
    id               BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    item_category_id BIGINT NOT NULL COMMENT 'FK → item_categories.id',
    source_type      VARCHAR(50)     COMMENT 'warehouse|donation|purchase',
    qty              INT    NOT NULL DEFAULT 0,
    CONSTRAINT fk_sb_item FOREIGN KEY (item_category_id) REFERENCES item_categories(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ================================================================
-- 12. INVENTORY RECEIPTS (Phiếu nhập kho)
-- ================================================================
CREATE TABLE IF NOT EXISTS inventory_receipts (
    id         BIGINT      NOT NULL AUTO_INCREMENT PRIMARY KEY,
    code       VARCHAR(50) NOT NULL UNIQUE,
    source_type VARCHAR(50),
    status     VARCHAR(20) NOT NULL DEFAULT 'pending',
    created_by BIGINT               COMMENT 'FK → users.id',
    created_at DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_ir_user FOREIGN KEY (created_by) REFERENCES users(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS inventory_receipt_lines (
    id               BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    receipt_id       BIGINT NOT NULL COMMENT 'FK → inventory_receipts.id',
    item_category_id BIGINT NOT NULL COMMENT 'FK → item_categories.id',
    qty              INT    NOT NULL DEFAULT 0,
    CONSTRAINT fk_irl_receipt FOREIGN KEY (receipt_id)       REFERENCES inventory_receipts(id) ON DELETE CASCADE,
    CONSTRAINT fk_irl_item    FOREIGN KEY (item_category_id) REFERENCES item_categories(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ================================================================
-- 13. INVENTORY ISSUES (Phiếu xuất kho)
-- ================================================================
CREATE TABLE IF NOT EXISTS inventory_issues (
    id         BIGINT      NOT NULL AUTO_INCREMENT PRIMARY KEY,
    code       VARCHAR(50) NOT NULL UNIQUE,
    status     VARCHAR(20) NOT NULL DEFAULT 'pending',
    created_by BIGINT               COMMENT 'FK → users.id',
    created_at DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_ii_user FOREIGN KEY (created_by) REFERENCES users(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS inventory_issue_lines (
    id               BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    issue_id         BIGINT NOT NULL COMMENT 'FK → inventory_issues.id',
    item_category_id BIGINT NOT NULL COMMENT 'FK → item_categories.id',
    qty              INT    NOT NULL DEFAULT 0,
    CONSTRAINT fk_iil_issue FOREIGN KEY (issue_id)         REFERENCES inventory_issues(id) ON DELETE CASCADE,
    CONSTRAINT fk_iil_item  FOREIGN KEY (item_category_id) REFERENCES item_categories(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ================================================================
-- 14. RELIEF REQUESTS (Yêu cầu cứu trợ vật tư)
-- ================================================================
CREATE TABLE IF NOT EXISTS relief_requests (
    id                BIGINT       NOT NULL AUTO_INCREMENT PRIMARY KEY,
    code              VARCHAR(50)  NOT NULL UNIQUE,
    rescue_request_id BIGINT                COMMENT 'FK → rescue_requests.id',
    target_area       VARCHAR(200),
    status            VARCHAR(20)  NOT NULL DEFAULT 'pending'
                                    COMMENT 'pending|approved|rejected|fulfilled',
    created_by        BIGINT                COMMENT 'FK → users.id',
    created_at        DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_relief_rr   FOREIGN KEY (rescue_request_id) REFERENCES rescue_requests(id) ON DELETE SET NULL,
    CONSTRAINT fk_relief_user FOREIGN KEY (created_by)        REFERENCES users(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS relief_request_lines (
    id               BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    relief_request_id BIGINT NOT NULL,
    item_category_id BIGINT NOT NULL,
    qty_requested    INT    NOT NULL DEFAULT 0,
    qty_approved     INT             DEFAULT 0,
    unit             VARCHAR(30),
    CONSTRAINT fk_rrl_relief FOREIGN KEY (relief_request_id) REFERENCES relief_requests(id) ON DELETE CASCADE,
    CONSTRAINT fk_rrl_item   FOREIGN KEY (item_category_id)  REFERENCES item_categories(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ================================================================
-- 15. DISTRIBUTIONS (Phân phối hàng cứu trợ)
-- ================================================================
CREATE TABLE IF NOT EXISTS distributions (
    id                BIGINT       NOT NULL AUTO_INCREMENT PRIMARY KEY,
    code              VARCHAR(50)  NOT NULL UNIQUE,
    status            VARCHAR(20)  NOT NULL DEFAULT 'pending',
    relief_request_id BIGINT                COMMENT 'FK → relief_requests.id',
    assigned_team_id  BIGINT                COMMENT 'FK → teams.id',
    asset_id          BIGINT                COMMENT 'FK → assets.id',
    issue_id          BIGINT                COMMENT 'FK → inventory_issues.id',
    planned_date      DATETIME,
    delivered_at      DATETIME,
    created_at        DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_dist_relief FOREIGN KEY (relief_request_id) REFERENCES relief_requests(id) ON DELETE SET NULL,
    CONSTRAINT fk_dist_team   FOREIGN KEY (assigned_team_id)  REFERENCES teams(id) ON DELETE SET NULL,
    CONSTRAINT fk_dist_asset  FOREIGN KEY (asset_id)          REFERENCES assets(id) ON DELETE SET NULL,
    CONSTRAINT fk_dist_issue  FOREIGN KEY (issue_id)          REFERENCES inventory_issues(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS distribution_lines (
    id               BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    distribution_id  BIGINT NOT NULL,
    item_category_id BIGINT NOT NULL,
    qty_planned      INT             DEFAULT 0,
    qty_picked       INT             DEFAULT 0,
    qty_delivered    INT             DEFAULT 0,
    unit             VARCHAR(30),
    CONSTRAINT fk_dl_dist FOREIGN KEY (distribution_id)  REFERENCES distributions(id) ON DELETE CASCADE,
    CONSTRAINT fk_dl_item FOREIGN KEY (item_category_id) REFERENCES item_categories(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ================================================================
-- 16. DISTRIBUTION PROOF (Chứng từ giao nhận)
-- ================================================================
CREATE TABLE IF NOT EXISTS distribution_proof (
    id              BIGINT       NOT NULL AUTO_INCREMENT PRIMARY KEY,
    distribution_id BIGINT       NOT NULL COMMENT 'FK → distributions.id',
    proof_type      VARCHAR(50)           COMMENT 'photo|signature|note',
    file_url        VARCHAR(500),
    note            TEXT,
    confirmed_by    BIGINT                COMMENT 'FK → users.id',
    confirmed_at    DATETIME,
    CONSTRAINT fk_dp_dist FOREIGN KEY (distribution_id) REFERENCES distributions(id) ON DELETE CASCADE,
    CONSTRAINT fk_dp_user FOREIGN KEY (confirmed_by)    REFERENCES users(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ================================================================
-- 17. RELIEF LOGS (Nhật ký hoạt động cứu trợ)
-- ================================================================
CREATE TABLE IF NOT EXISTS relief_logs (
    id          BIGINT      NOT NULL AUTO_INCREMENT PRIMARY KEY,
    entity_type VARCHAR(50) NOT NULL COMMENT 'rescue_request|distribution|relief_request',
    entity_id   BIGINT      NOT NULL,
    actor_id    BIGINT                COMMENT 'FK → users.id',
    action_note TEXT,
    created_at  DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_rl_actor FOREIGN KEY (actor_id) REFERENCES users(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
