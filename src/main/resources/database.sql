
-- DROP DATABASE flood_rescue;
-- CREATE DATABASE flood_rescue CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

/* =========================================================
   V1__init_schema.sql
   FLOOD RESCUE & RELIEF SYSTEM - MySQL 8+
   Engine: InnoDB | Charset: utf8mb4
   Notes:
   - Single warehouse only (no warehouses table)
   - Status/priority/source stored as ENUM
   ========================================================= */

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- =========================
-- 0) DROP (optional for dev)
-- =========================
-- (Bạn có thể bỏ phần DROP nếu không muốn reset)
DROP TABLE IF EXISTS distribution_proofs;
DROP TABLE IF EXISTS distribution_assignments;
DROP TABLE IF EXISTS distribution_lines;
DROP TABLE IF EXISTS distributions;

DROP TABLE IF EXISTS relief_request_lines;
DROP TABLE IF EXISTS relief_requests;

DROP TABLE IF EXISTS inventory_issue_lines;
DROP TABLE IF EXISTS inventory_issues;
DROP TABLE IF EXISTS inventory_receipt_lines;
DROP TABLE IF EXISTS inventory_receipts;

DROP TABLE IF EXISTS stock_balances;
DROP TABLE IF EXISTS item_categories;

DROP TABLE IF EXISTS rescue_assignments;
DROP TABLE IF EXISTS task_group_timeline;
DROP TABLE IF EXISTS task_group_requests;
DROP TABLE IF EXISTS task_groups;

DROP TABLE IF EXISTS rescue_request_timeline;
DROP TABLE IF EXISTS rescue_request_attachments;
DROP TABLE IF EXISTS rescue_requests;

DROP TABLE IF EXISTS notifications;
DROP TABLE IF EXISTS notification_rules;
DROP TABLE IF EXISTS notification_templates;

DROP TABLE IF EXISTS role_permissions;
DROP TABLE IF EXISTS permissions;
DROP TABLE IF EXISTS system_settings;
DROP TABLE IF EXISTS audit_logs;

DROP TABLE IF EXISTS users;
DROP TABLE IF EXISTS teams;
DROP TABLE IF EXISTS roles;
DROP TABLE IF EXISTS assets; 

SET FOREIGN_KEY_CHECKS = 1;

-- =========================
-- 1) CORE: ROLES / TEAMS / USERS
-- =========================

CREATE TABLE roles (
  id INT UNSIGNED NOT NULL AUTO_INCREMENT,
  code VARCHAR(30) NOT NULL,
  name VARCHAR(80) NOT NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY uk_roles_code (code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE teams (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  code VARCHAR(30) NOT NULL,
  name VARCHAR(120) NOT NULL,
  team_type ENUM('RESCUE_TEAM','COORDINATOR','LOGISTICS','MANAGER') NOT NULL,
  status TINYINT NOT NULL DEFAULT 1, -- 1 active, 0 inactive
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY uk_teams_code (code),
  KEY idx_teams_type_status (team_type, status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE users (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  role_id INT UNSIGNED NOT NULL,
  team_id BIGINT UNSIGNED NULL, -- citizen có thể NULL
  full_name VARCHAR(120) NOT NULL,
  phone VARCHAR(20) NULL,
  email VARCHAR(120) NULL,
  password_hash VARCHAR(255) NOT NULL,
  status TINYINT NOT NULL DEFAULT 1,
  last_login_at DATETIME NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY uk_users_phone (phone),
  UNIQUE KEY uk_users_email (email),
  KEY idx_users_role (role_id),
  KEY idx_users_team (team_id),
  CONSTRAINT fk_users_role FOREIGN KEY (role_id) REFERENCES roles(id),
  CONSTRAINT fk_users_team FOREIGN KEY (team_id) REFERENCES teams(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =========================
-- 2) RESCUE FLOW (CỨU HỘ)
-- =========================

CREATE TABLE rescue_requests (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  code VARCHAR(30) NOT NULL,
  citizen_id BIGINT UNSIGNED NOT NULL,

  status ENUM('PENDING','VERIFIED','IN_PROGRESS','COMPLETED','CANCELLED','DUPLICATE') NOT NULL DEFAULT 'PENDING',
  priority ENUM('HIGH','MEDIUM','LOW') NOT NULL DEFAULT 'MEDIUM',

  master_request_id BIGINT UNSIGNED NULL, -- duplicate -> master
  affected_people_count INT NOT NULL DEFAULT 1,
  description TEXT NULL,
  address_text VARCHAR(255) NULL,

  location_verified TINYINT(1) NOT NULL DEFAULT 0,

  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

  PRIMARY KEY (id),
  UNIQUE KEY uk_rr_code (code),
  KEY idx_rr_citizen (citizen_id),
  KEY idx_rr_status_created (status, created_at),
  KEY idx_rr_priority_people_time (priority, affected_people_count, created_at),
  KEY idx_rr_master (master_request_id),

  CONSTRAINT fk_rr_citizen FOREIGN KEY (citizen_id) REFERENCES users(id),
  CONSTRAINT fk_rr_master FOREIGN KEY (master_request_id) REFERENCES rescue_requests(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE rescue_request_attachments (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  rescue_request_id BIGINT UNSIGNED NOT NULL,
  file_url VARCHAR(500) NOT NULL,
  file_type VARCHAR(30) NULL, -- IMAGE/VIDEO/OTHER...
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  KEY idx_rr_att_req (rescue_request_id),
  CONSTRAINT fk_rr_att_req FOREIGN KEY (rescue_request_id) REFERENCES rescue_requests(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE rescue_request_timeline (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  rescue_request_id BIGINT UNSIGNED NOT NULL,
  actor_id BIGINT UNSIGNED NOT NULL,

  event_type VARCHAR(50) NOT NULL, -- STATUS_CHANGE/NOTE/VERIFY/ASSIGN/COMPLETE...
  from_status ENUM('PENDING','VERIFIED','IN_PROGRESS','COMPLETED','CANCELLED','DUPLICATE') NULL,
  to_status   ENUM('PENDING','VERIFIED','IN_PROGRESS','COMPLETED','CANCELLED','DUPLICATE') NULL,
  note TEXT NULL,

  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  KEY idx_rr_tl_req_time (rescue_request_id, created_at),
  KEY idx_rr_tl_actor (actor_id),
  CONSTRAINT fk_rr_tl_req FOREIGN KEY (rescue_request_id) REFERENCES rescue_requests(id) ON DELETE CASCADE,
  CONSTRAINT fk_rr_tl_actor FOREIGN KEY (actor_id) REFERENCES users(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =========================
-- 3) TASK GROUP (GỘP NHIỆM VỤ) + ASSIGN
-- =========================

CREATE TABLE task_groups (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  code VARCHAR(30) NOT NULL,

  status ENUM('NEW','ASSIGNED','IN_PROGRESS','DONE','CANCELLED') NOT NULL DEFAULT 'NEW',
  assigned_team_id BIGINT UNSIGNED NULL,

  note TEXT NULL,
  created_by BIGINT UNSIGNED NULL,

  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

  PRIMARY KEY (id),
  UNIQUE KEY uk_tg_code (code),
  KEY idx_tg_status (status),
  KEY idx_tg_assigned_team (assigned_team_id),
  KEY idx_tg_created_by (created_by),

  CONSTRAINT fk_tg_team FOREIGN KEY (assigned_team_id) REFERENCES teams(id),
  CONSTRAINT fk_tg_created_by FOREIGN KEY (created_by) REFERENCES users(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- N-N: 1 task_group chứa nhiều rescue_request; 1 request có thể thuộc group (tuỳ nghiệp vụ)
CREATE TABLE task_group_requests (
  task_group_id BIGINT UNSIGNED NOT NULL,
  rescue_request_id BIGINT UNSIGNED NOT NULL,
  added_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (task_group_id, rescue_request_id),
  KEY idx_tgr_req (rescue_request_id),
  CONSTRAINT fk_tgr_group FOREIGN KEY (task_group_id) REFERENCES task_groups(id) ON DELETE CASCADE,
  CONSTRAINT fk_tgr_req FOREIGN KEY (rescue_request_id) REFERENCES rescue_requests(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE task_group_timeline (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  task_group_id BIGINT UNSIGNED NOT NULL,
  actor_id BIGINT UNSIGNED NOT NULL,
  event_type VARCHAR(50) NOT NULL,
  note TEXT NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  KEY idx_tg_tl_group_time (task_group_id, created_at),
  KEY idx_tg_tl_actor (actor_id),
  CONSTRAINT fk_tg_tl_group FOREIGN KEY (task_group_id) REFERENCES task_groups(id) ON DELETE CASCADE,
  CONSTRAINT fk_tg_tl_actor FOREIGN KEY (actor_id) REFERENCES users(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =========================
-- 4) ASSETS (PHƯƠNG TIỆN / THIẾT BỊ)
-- =========================

CREATE TABLE assets (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  code VARCHAR(30) NOT NULL,
  name VARCHAR(120) NOT NULL,
  asset_type VARCHAR(50) NOT NULL, -- BOAT/TRUCK/VAN/MEDICAL_KIT...
  status ENUM('AVAILABLE','IN_USE','MAINTENANCE','BROKEN','INACTIVE') NOT NULL DEFAULT 'AVAILABLE',
  capacity INT NULL,
  assigned_team_id BIGINT UNSIGNED NULL,
  note TEXT NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

  PRIMARY KEY (id),
  UNIQUE KEY uk_assets_code (code),
  KEY idx_assets_status (status),
  KEY idx_assets_team (assigned_team_id),
  CONSTRAINT fk_assets_team FOREIGN KEY (assigned_team_id) REFERENCES teams(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Gán team + asset cho task_group (cứu hộ)
CREATE TABLE rescue_assignments (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  task_group_id BIGINT UNSIGNED NOT NULL,
  team_id BIGINT UNSIGNED NOT NULL,
  asset_id BIGINT UNSIGNED NULL,
  assigned_by BIGINT UNSIGNED NULL,
  assigned_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  is_active TINYINT(1) NOT NULL DEFAULT 1,

  PRIMARY KEY (id),
  KEY idx_ra_group (task_group_id),
  KEY idx_ra_team (team_id),
  KEY idx_ra_asset (asset_id),
  KEY idx_ra_active (is_active),

  CONSTRAINT fk_ra_group FOREIGN KEY (task_group_id) REFERENCES task_groups(id),
  CONSTRAINT fk_ra_team FOREIGN KEY (team_id) REFERENCES teams(id),
  CONSTRAINT fk_ra_asset FOREIGN KEY (asset_id) REFERENCES assets(id),
  CONSTRAINT fk_ra_user FOREIGN KEY (assigned_by) REFERENCES users(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =========================
-- 5) INVENTORY (1 KHO DUY NHẤT)
-- =========================

CREATE TABLE item_categories (
  id INT UNSIGNED NOT NULL AUTO_INCREMENT,
  code VARCHAR(30) NOT NULL,
  name VARCHAR(120) NOT NULL,
  unit VARCHAR(20) NOT NULL,
  is_active TINYINT(1) NOT NULL DEFAULT 1,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY uk_item_cat_code (code),
  KEY idx_item_cat_active (is_active)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- tồn kho theo loại hàng + nguồn (DONATION/PURCHASE)
CREATE TABLE stock_balances (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  item_category_id INT UNSIGNED NOT NULL,
  source_type ENUM('DONATION','PURCHASE') NOT NULL,
  qty DECIMAL(14,2) NOT NULL DEFAULT 0,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

  PRIMARY KEY (id),
  UNIQUE KEY uk_stock_item_source (item_category_id, source_type),
  KEY idx_stock_item (item_category_id),
  CONSTRAINT fk_stock_item FOREIGN KEY (item_category_id) REFERENCES item_categories(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ---- Phiếu nhập kho
CREATE TABLE inventory_receipts (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  code VARCHAR(30) NOT NULL,
  source_type ENUM('DONATION','PURCHASE') NOT NULL,
  status ENUM('DRAFT','APPROVED','DONE','CANCELLED') NOT NULL DEFAULT 'DRAFT',
  created_by BIGINT UNSIGNED NOT NULL,
  note TEXT NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

  PRIMARY KEY (id),
  UNIQUE KEY uk_receipt_code (code),
  KEY idx_receipt_status (status),
  KEY idx_receipt_created_by (created_by),
  CONSTRAINT fk_receipt_user FOREIGN KEY (created_by) REFERENCES users(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE inventory_receipt_lines (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  receipt_id BIGINT UNSIGNED NOT NULL,
  item_category_id INT UNSIGNED NOT NULL,
  qty DECIMAL(14,2) NOT NULL,
  unit VARCHAR(20) NOT NULL,
  PRIMARY KEY (id),
  KEY idx_receipt_line_receipt (receipt_id),
  KEY idx_receipt_line_item (item_category_id),
  CONSTRAINT fk_receipt_line_receipt FOREIGN KEY (receipt_id) REFERENCES inventory_receipts(id) ON DELETE CASCADE,
  CONSTRAINT fk_receipt_line_item FOREIGN KEY (item_category_id) REFERENCES item_categories(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ---- Phiếu xuất kho
CREATE TABLE inventory_issues (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  code VARCHAR(30) NOT NULL,
  status ENUM('DRAFT','APPROVED','DONE','CANCELLED') NOT NULL DEFAULT 'DRAFT',
  created_by BIGINT UNSIGNED NOT NULL,
  note TEXT NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

  PRIMARY KEY (id),
  UNIQUE KEY uk_issue_code (code),
  KEY idx_issue_status (status),
  KEY idx_issue_created_by (created_by),
  CONSTRAINT fk_issue_user FOREIGN KEY (created_by) REFERENCES users(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
DROP TABLE IF EXISTS inventory_issue_lines;
DROP TABLE IF EXISTS inventory_issues;

CREATE TABLE inventory_issues (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  code VARCHAR(30) NOT NULL,

  -- trạng thái phiếu
  status ENUM('DRAFT','APPROVED','DONE','CANCELLED') NOT NULL DEFAULT 'DRAFT',

  -- ai tạo phiếu
  created_by BIGINT UNSIGNED NOT NULL,

  -- liên kết cứu trợ (phiếu yêu cầu cứu trợ)
  relief_request_id BIGINT UNSIGNED NULL,

  -- đội vận chuyển / thực hiện xuất & giao
  assigned_team_id BIGINT UNSIGNED NULL,

  -- phương tiện
  asset_id BIGINT UNSIGNED NULL,

  -- kho xuất (nếu bạn chốt 1 kho duy nhất vẫn có thể lưu để đúng UI)
  warehouse_id BIGINT UNSIGNED NULL,

  note TEXT NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

  PRIMARY KEY (id),
  UNIQUE KEY uk_issue_code (code),

  KEY idx_issue_status (status),
  KEY idx_issue_created_by (created_by),
  KEY idx_issue_relief (relief_request_id),
  KEY idx_issue_team (assigned_team_id),
  KEY idx_issue_asset (asset_id),
  KEY idx_issue_warehouse (warehouse_id),

  CONSTRAINT fk_issue_user FOREIGN KEY (created_by) REFERENCES users(id),
  CONSTRAINT fk_issue_relief FOREIGN KEY (relief_request_id) REFERENCES relief_requests(id),
  CONSTRAINT fk_issue_team FOREIGN KEY (assigned_team_id) REFERENCES teams(id),
  CONSTRAINT fk_issue_asset FOREIGN KEY (asset_id) REFERENCES assets(id),
  CONSTRAINT fk_issue_warehouse FOREIGN KEY (warehouse_id) REFERENCES warehouses(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE inventory_issue_lines (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  issue_id BIGINT UNSIGNED NOT NULL,
  item_category_id INT UNSIGNED NOT NULL,
  qty DECIMAL(14,2) NOT NULL,
  unit VARCHAR(20) NOT NULL,
  PRIMARY KEY (id),
  KEY idx_issue_line_issue (issue_id),
  KEY idx_issue_line_item (item_category_id),
  CONSTRAINT fk_issue_line_issue FOREIGN KEY (issue_id) REFERENCES inventory_issues(id) ON DELETE CASCADE,
  CONSTRAINT fk_issue_line_item FOREIGN KEY (item_category_id) REFERENCES item_categories(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =========================
-- 6) RELIEF FLOW (CỨU TRỢ / PHÂN PHỐI)
-- =========================

USE flood_rescue;
-- Manager tạo relief_request (có thể link qua rescue_request để kết nối 2 luồng)
CREATE TABLE relief_requests (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  code VARCHAR(30) NOT NULL,
  created_by BIGINT UNSIGNED NOT NULL,
  status ENUM('DRAFT','APPROVED','DONE','CANCELLED') NOT NULL DEFAULT 'DRAFT',
  target_area VARCHAR(255) NOT NULL,
  rescue_request_id BIGINT UNSIGNED NULL, -- kết nối 2 luồng
  note TEXT NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

  PRIMARY KEY (id),
  UNIQUE KEY uk_relief_code (code),
  KEY idx_relief_status (status),
  KEY idx_relief_created_by (created_by),
  KEY idx_relief_rescue (rescue_request_id),

  CONSTRAINT fk_relief_user FOREIGN KEY (created_by) REFERENCES users(id),
  CONSTRAINT fk_relief_rescue FOREIGN KEY (rescue_request_id) REFERENCES rescue_requests(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE relief_request_lines (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  relief_request_id BIGINT UNSIGNED NOT NULL,
  item_category_id INT UNSIGNED NOT NULL,
  qty DECIMAL(14,2) NOT NULL,
  unit VARCHAR(20) NOT NULL,
  PRIMARY KEY (id),
  KEY idx_rrl_relief (relief_request_id),
  KEY idx_rrl_item (item_category_id),
  CONSTRAINT fk_rrl_relief FOREIGN KEY (relief_request_id) REFERENCES relief_requests(id) ON DELETE CASCADE,
  CONSTRAINT fk_rrl_item FOREIGN KEY (item_category_id) REFERENCES item_categories(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Phiếu phân phối (gắn với relief_request + issue)
CREATE TABLE distributions (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  code VARCHAR(30) NOT NULL,
  relief_request_id BIGINT UNSIGNED NOT NULL,
  assigned_team_id BIGINT UNSIGNED NOT NULL,
  status ENUM('PLANNED','ASSIGNED','IN_TRANSIT','DELIVERED','CANCELLED') NOT NULL DEFAULT 'PLANNED',
  issue_id BIGINT UNSIGNED NULL, -- liên kết phiếu xuất kho
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

  PRIMARY KEY (id),
  UNIQUE KEY uk_dist_code (code),
  KEY idx_dist_status (status),
  KEY idx_dist_team (assigned_team_id),
  KEY idx_dist_relief (relief_request_id),
  KEY idx_dist_issue (issue_id),

  CONSTRAINT fk_dist_relief FOREIGN KEY (relief_request_id) REFERENCES relief_requests(id),
  CONSTRAINT fk_dist_team FOREIGN KEY (assigned_team_id) REFERENCES teams(id),
  CONSTRAINT fk_dist_issue FOREIGN KEY (issue_id) REFERENCES inventory_issues(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE distribution_lines (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  distribution_id BIGINT UNSIGNED NOT NULL,
  item_category_id INT UNSIGNED NOT NULL,
  qty DECIMAL(14,2) NOT NULL,
  unit VARCHAR(20) NOT NULL,
  PRIMARY KEY (id),
  KEY idx_dline_dist (distribution_id),
  KEY idx_dline_item (item_category_id),
  CONSTRAINT fk_dline_dist FOREIGN KEY (distribution_id) REFERENCES distributions(id) ON DELETE CASCADE,
  CONSTRAINT fk_dline_item FOREIGN KEY (item_category_id) REFERENCES item_categories(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Điều phối vận chuyển: gán team + asset
CREATE TABLE distribution_assignments (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  distribution_id BIGINT UNSIGNED NOT NULL,
  team_id BIGINT UNSIGNED NOT NULL,
  asset_id BIGINT UNSIGNED NOT NULL,
  status ENUM('PLANNED','DISPATCHED','DELIVERED','FAILED','CANCELLED') NOT NULL DEFAULT 'PLANNED',
  dispatched_at DATETIME NULL,
  completed_at DATETIME NULL,
  note TEXT NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,

  PRIMARY KEY (id),
  KEY idx_da_dist (distribution_id),
  KEY idx_da_team (team_id),
  KEY idx_da_asset (asset_id),
  KEY idx_da_status (status),

  CONSTRAINT fk_da_dist FOREIGN KEY (distribution_id) REFERENCES distributions(id) ON DELETE CASCADE,
  CONSTRAINT fk_da_team FOREIGN KEY (team_id) REFERENCES teams(id),
  CONSTRAINT fk_da_asset FOREIGN KEY (asset_id) REFERENCES assets(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Proof (ảnh/ghi chú) khi giao hàng (optional nhưng rất hữu ích)
CREATE TABLE distribution_proofs (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  distribution_id BIGINT UNSIGNED NOT NULL,
  proof_type ENUM('PHOTO','SIGNATURE','NOTE') NOT NULL DEFAULT 'PHOTO',
  file_url VARCHAR(500) NULL,
  note TEXT NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  KEY idx_proof_dist (distribution_id),
  CONSTRAINT fk_proof_dist FOREIGN KEY (distribution_id) REFERENCES distributions(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =========================
-- 7) NOTIFICATIONS
-- =========================

CREATE TABLE notification_templates (
  id INT UNSIGNED NOT NULL AUTO_INCREMENT,
  code VARCHAR(80) NOT NULL,
  title VARCHAR(150) NOT NULL,
  content TEXT NOT NULL,
  channel ENUM('WEB','EMAIL','SMS') NOT NULL DEFAULT 'WEB',
  is_active TINYINT(1) NOT NULL DEFAULT 1,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY uk_ntpl_code (code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE notification_rules (
  id INT UNSIGNED NOT NULL AUTO_INCREMENT,
  event_code VARCHAR(80) NOT NULL,
  template_id INT UNSIGNED NOT NULL,
  target_role_id INT UNSIGNED NULL,
  is_active TINYINT(1) NOT NULL DEFAULT 1,
  PRIMARY KEY (id),
  KEY idx_nrule_event (event_code),
  KEY idx_nrule_role (target_role_id),
  CONSTRAINT fk_nrule_tpl FOREIGN KEY (template_id) REFERENCES notification_templates(id),
  CONSTRAINT fk_nrule_role FOREIGN KEY (target_role_id) REFERENCES roles(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE notifications (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  user_id BIGINT UNSIGNED NOT NULL,
  title VARCHAR(150) NOT NULL,
  content TEXT NOT NULL,
  is_read TINYINT(1) NOT NULL DEFAULT 0,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  KEY idx_notif_user_read (user_id, is_read, created_at),
  CONSTRAINT fk_notif_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =========================
-- 😎 ADMIN: PERMISSION / SETTINGS / AUDIT
-- =========================

CREATE TABLE permissions (
  id INT UNSIGNED NOT NULL AUTO_INCREMENT,
  code VARCHAR(80) NOT NULL,
  name VARCHAR(120) NOT NULL,
  module VARCHAR(50) NULL,
  description TEXT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uk_perm_code (code),
  KEY idx_perm_module (module)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE role_permissions (
  role_id INT UNSIGNED NOT NULL,
  permission_id INT UNSIGNED NOT NULL,
  PRIMARY KEY (role_id, permission_id),
  CONSTRAINT fk_rp_role FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE CASCADE,
  CONSTRAINT fk_rp_perm FOREIGN KEY (permission_id) REFERENCES permissions(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE system_settings (
  id INT UNSIGNED NOT NULL AUTO_INCREMENT,
  key_name VARCHAR(120) NOT NULL,
  value_text TEXT NOT NULL,
  value_type ENUM('STRING','INT','BOOL','JSON') NOT NULL DEFAULT 'STRING',
  description TEXT NULL,
  updated_by BIGINT UNSIGNED NULL,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY uk_settings_key (key_name),
  KEY idx_settings_updated_by (updated_by),
  CONSTRAINT fk_settings_user FOREIGN KEY (updated_by) REFERENCES users(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE audit_logs (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  actor_id BIGINT UNSIGNED NOT NULL,
  action ENUM('CREATE','UPDATE','DELETE','LOGIN','LOGOUT') NOT NULL,
  entity_type VARCHAR(50) NOT NULL,
  entity_id BIGINT UNSIGNED NULL,
  old_data JSON NULL,
  new_data JSON NULL,
  ip_address VARCHAR(50) NULL,
  user_agent VARCHAR(255) NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  KEY idx_audit_actor_time (actor_id, created_at),
  KEY idx_audit_entity (entity_type, entity_id),
  CONSTRAINT fk_audit_actor FOREIGN KEY (actor_id) REFERENCES users(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


INSERT INTO roles (code, name, created_at)
VALUES
('CITIZEN', 'Công dân', NOW()),
('COORDINATOR', 'Điều phối cứu hộ', NOW()),
('RESCUER', 'Đội cứu hộ', NOW()),
('MANAGER', 'Quản lý', NOW()),
('ADMIN', 'Quản trị hệ thống', NOW())
ON DUPLICATE KEY UPDATE
name = VALUES(name);

SET FOREIGN_KEY_CHECKS = 1;


-- add user for cooradator
SELECT id, email, phone, role_id
FROM users
WHERE email = 'conchimcu@gmail.com'
   OR phone = '0900000002';
   
   
   -- 
   SELECT 
    r.id,
    r.code,
    r.citizen_id,
    r.affected_people_count,
    r.priority,
    r.status,
    r.address_text,
    r.description,
    r.location_verified,
    r.created_at,
    r.updated_at
FROM rescue_requests r
ORDER BY r.created_at DESC
LIMIT 20;

--
SELECT
    a.id,
    a.rescue_request_id,
    a.file_url,
    a.file_type,
    a.created_at
FROM rescue_request_attachments a
WHERE a.rescue_request_id = 123   -- id request
ORDER BY a.created_at ASC; 

SELECT * FROM rescue_request_attachments
ORDER BY created_at DESC;


-- 3/3/2026
-- 1) Kiểm tra nhóm nhiệm vụ (Task Group) đã tạo chưa
SELECT id, code, status, assigned_team_id, created_at, updated_at
FROM task_groups
ORDER BY id DESC
LIMIT 10;

-- 2) Kiểm tra các rescue request đã được gắn vào nhóm chưa
SELECT task_group_id, rescue_request_id
FROM task_group_requests
ORDER BY task_group_id DESC
LIMIT 50;

-- 3) Kiểm tra phân công (Team + Asset) đã lưu chưa
SELECT id, task_group_id, team_id, asset_id, assigned_by, assigned_at, is_active
FROM rescue_assignments
ORDER BY id DESC
LIMIT 20;

-- II kiểm tra đội
-- 1. kiểm tra đội đã được lưu dưới DB
SELECT id, code, name, description, created_at, updated_at
FROM teams
ORDER BY id DESC
LIMIT 20;

-- 2) Kiểm tra phương tiện / thiết bị (assets)
SELECT id, code, name, asset_type, status, capacity, assigned_team_id, created_at, updated_at
FROM assets
ORDER BY id DESC
LIMIT 20;

-- 3) Nếu muốn kiểm tra phương tiện đang thuộc đội nào (join)
SELECT a.id, a.code, a.name, a.status, t.id AS team_id, t.name AS team_name
FROM assets a
LEFT JOIN teams t ON t.id = a.assigned_team_id
ORDER BY a.id DESC
LIMIT 50;

SELECT id, code, name, assigned_team_id
FROM assets
ORDER BY id DESC;

SELECT a.id, a.code, a.name, a.status,
       a.assigned_team_id AS team_id,
       t.name AS team_name
FROM assets a
LEFT JOIN teams t ON t.id = a.assigned_team_id
ORDER BY a.id DESC;

-- lệnh dùng để xóa data của đội, phương tiện, gộp nhiệm vụ 

-- USE flood_rescue;

-- -- Xoá user có role RESCUER (đội cứu hộ)
-- DELETE u
-- FROM users u
-- JOIN roles r ON r.id = u.role_id
-- WHERE r.code = 'RESCUER';

SET SQL_SAFE_UPDATES = 1;
USE flood_rescue;


DELETE FROM rescue_assignments    WHERE id > 0;
DELETE FROM task_group_requests   WHERE task_group_id > 0;
DELETE FROM task_group_timeline   WHERE task_group_id > 0;
DELETE FROM task_groups           WHERE id > 0;

-- xem tất cả các loại hàng 
SELECT * FROM item_categories ORDER BY id; 

DELETE FROM item_categories WHERE id > 0 ; 

SELECT * FROM inventory_receipts 
ORDER BY created_at DESC 
LIMIT 5;

-- 1. Kiểm tra có phiếu nhập không
 SELECT 
    id, 
    code, 
    status, 
    source_type,
    created_at
FROM inventory_receipts
ORDER BY created_at DESC;

-- 2. Kiểm tra có dữ liệu tồn kho không
SELECT 
    sb.id,
    sb.item_category_id,
    ic.code AS item_code,
    ic.name AS item_name,
    sb.source_type,
    sb.qty,
    sb.updated_at
FROM stock_balances sb
JOIN item_categories ic ON sb.item_category_id = ic.id
ORDER BY sb.updated_at DESC;


UPDATE inventory_receipts 
SET status = 'DONE' 
WHERE status = 'DRAFT';

 -- 3. Kiểm tra tất cả mặt hàng 
 SELECT 
    id,
    code,
    name,
    unit,
    is_active,
    created_at
FROM item_categories
ORDER BY created_at DESC;

SELECT 
    id,
    code,
    name,
    unit,
    is_active
FROM item_categories
WHERE name = 'Gạo tẻ (25kg)';  -- Thay bằng tên bạn muốn tìm


-- Xem tất cả yêu cầu cứu trợ
SELECT 
    id,
    code,
    created_by,
    status,
    target_area,
    rescue_request_id,
    note,
    created_at,
    updated_at
FROM relief_requests
ORDER BY created_at DESC;

-- Xem tất cả các gán nhiệm vụ
SELECT 
    ra.id,
    ra.task_group_id,
    tg.code AS task_group_code,
    ra.team_id,
    t.name AS team_name,
    ra.asset_id,
    a.code AS asset_code,
    a.name AS asset_name,
    ra.assigned_by,
    u.username AS assigned_by_username,
    u.full_name AS assigned_by_name,
    ra.assigned_at,
    ra.is_active
FROM rescue_assignments ra
LEFT JOIN task_groups tg ON ra.task_group_id = tg.id
LEFT JOIN teams t ON ra.team_id = t.id
LEFT JOIN assets a ON ra.asset_id = a.id
LEFT JOIN users u ON ra.assigned_by = u.id
ORDER BY ra.assigned_at DESC;

-- Kiểm tra tồn kho của mặt hàng "Lương Thực"
SELECT 
    ic.id,
    ic.code,
    ic.name,
    sb.source_type,
    sb.qty AS stock_qty,
    sb.unit
FROM stock_balances sb
INNER JOIN item_categories ic ON sb.item_category_id = ic.id
WHERE ic.name LIKE '%Lương Thực%'
ORDER BY sb.source_type;

-- Tổng tồn kho của mặt hàng "Lương Thực" (tất cả nguồn)
SELECT 
    ic.name,
    SUM(sb.qty) AS total_stock
FROM stock_balances sb
INNER JOIN item_categories ic ON sb.item_category_id = ic.id
WHERE ic.name LIKE '%Lương Thực%'
GROUP BY ic.id, ic.name;

-- Tổng tồn kho của mặt hàng "Lương Thực" (tất cả nguồn)
SELECT 
    ic.name,
    SUM(sb.qty) AS total_stock
FROM stock_balances sb
INNER JOIN item_categories ic ON sb.item_category_id = ic.id
WHERE ic.name LIKE '%Lương Thực%'
GROUP BY ic.id, ic.name;

SELECT 
    ic.id AS item_id,
    ic.code AS item_code,
    ic.name AS item_name,
    SUM(sb.qty) AS total_stock,
    ic.unit
FROM stock_balances sb
INNER JOIN item_categories ic ON sb.item_category_id = ic.id
GROUP BY ic.id, ic.code, ic.name, ic.unit
ORDER BY ic.name;

SELECT 
    ic.id,
    ic.code,
    ic.name,
    'KHÔNG CÓ TỒN KHO' AS status
FROM item_categories ic
LEFT JOIN stock_balances sb ON ic.id = sb.item_category_id
WHERE sb.id IS NULL
ORDER BY ic.name;  

SELECT 
    ic.name AS item_name,
    sb.qty AS stock_qty,
    sb.unit
FROM stock_balances sb
INNER JOIN item_categories ic ON sb.item_category_id = ic.id
WHERE sb.source_type = 'DONATION'
ORDER BY ic.name;


SELECT COUNT(*) FROM inventory_receipts WHERE status = 'APPROVED';