package com.floodrescue.module.admin.service;

import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AdminCatalogService {

    private final JdbcTemplate jdbcTemplate;

    @Transactional(readOnly = true)
    @Cacheable(cacheNames = "adminCatalogs")
    public List<Map<String, Object>> getCatalogs() {
        return jdbcTemplate.query(
                "SELECT id, group_code, code, name, active, created_at, updated_at FROM admin_catalogs ORDER BY group_code, code",
                (rs, rowNum) -> {
                    Map<String, Object> row = new HashMap<>();
                    row.put("id", rs.getLong("id"));
                    row.put("groupCode", rs.getString("group_code"));
                    row.put("code", rs.getString("code"));
                    row.put("name", rs.getString("name"));
                    row.put("nameVn", rs.getString("name"));
                    row.put("active", rs.getBoolean("active"));
                    row.put("createdAt", rs.getTimestamp("created_at"));
                    row.put("updatedAt", rs.getTimestamp("updated_at"));
                    return row;
                }
        );
    }

    @Transactional
    @CacheEvict(cacheNames = {"adminCatalogs", "adminCatalogGroups"}, allEntries = true)
    public void createCatalog(Map<String, Object> payload) {
        String groupCode = String.valueOf(payload.getOrDefault("groupCode", "")).trim().toUpperCase();
        String code = String.valueOf(payload.getOrDefault("code", "")).trim().toUpperCase();
        String name = String.valueOf(payload.getOrDefault("nameVn", payload.getOrDefault("name", ""))).trim();
        boolean active = Boolean.parseBoolean(String.valueOf(payload.getOrDefault("active", "true")));

        jdbcTemplate.update(
                "INSERT INTO admin_catalogs(group_code, code, name, active, created_at, updated_at) VALUES (?, ?, ?, ?, NOW(), NOW())",
                groupCode, code, name, active ? 1 : 0
        );
    }

    @Transactional
    @CacheEvict(cacheNames = {"adminCatalogs", "adminCatalogGroups"}, allEntries = true)
    public void updateCatalog(Long id, Map<String, Object> payload) {
        String groupCode = String.valueOf(payload.getOrDefault("groupCode", "")).trim().toUpperCase();
        String code = String.valueOf(payload.getOrDefault("code", "")).trim().toUpperCase();
        String name = String.valueOf(payload.getOrDefault("nameVn", payload.getOrDefault("name", ""))).trim();
        boolean active = Boolean.parseBoolean(String.valueOf(payload.getOrDefault("active", "true")));

        jdbcTemplate.update(
                "UPDATE admin_catalogs SET group_code = ?, code = ?, name = ?, active = ?, updated_at = NOW() WHERE id = ?",
                groupCode, code, name, active ? 1 : 0, id
        );
    }

    @Transactional
    @CacheEvict(cacheNames = {"adminCatalogs", "adminCatalogGroups"}, allEntries = true)
    public void deleteCatalog(Long id) {
        jdbcTemplate.update("DELETE FROM admin_catalogs WHERE id = ?", id);
    }

    @Transactional
    @CacheEvict(cacheNames = {"adminCatalogs", "adminCatalogGroups"}, allEntries = true)
    public void toggleCatalogActive(Long id) {
        jdbcTemplate.update("UPDATE admin_catalogs SET active = IF(active = 1, 0, 1), updated_at = NOW() WHERE id = ?", id);
    }

    @Transactional(readOnly = true)
    @Cacheable(cacheNames = "adminCatalogGroups")
    public List<Map<String, Object>> getCatalogGroups() {
        return jdbcTemplate.query(
                "SELECT group_code, MAX(CASE WHEN code='__GROUP__' THEN name ELSE group_code END) AS display_name, " +
                        "SUM(CASE WHEN code<>'__GROUP__' THEN 1 ELSE 0 END) AS total_statuses " +
                        "FROM admin_catalogs GROUP BY group_code ORDER BY group_code",
                (rs, rowNum) -> {
                    Map<String, Object> item = new HashMap<>();
                    item.put("groupCode", rs.getString("group_code"));
                    item.put("name", rs.getString("display_name"));
                    item.put("totalStatuses", rs.getLong("total_statuses"));
                    return item;
                }
        );
    }

    @Transactional
    @CacheEvict(cacheNames = {"adminCatalogs", "adminCatalogGroups"}, allEntries = true)
    public void updateCatalogGroupName(String groupCode, Map<String, Object> payload) {
        String normalized = groupCode.trim().toUpperCase();
        String name = String.valueOf(payload.getOrDefault("name", normalized)).trim();

        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM admin_catalogs WHERE group_code = ? AND code = '__GROUP__'",
                Integer.class,
                normalized
        );
        if (count != null && count > 0) {
            jdbcTemplate.update(
                    "UPDATE admin_catalogs SET name = ?, updated_at = NOW() WHERE group_code = ? AND code = '__GROUP__'",
                    name, normalized
            );
            return;
        }

        jdbcTemplate.update(
                "INSERT INTO admin_catalogs(group_code, code, name, active, created_at, updated_at) VALUES (?, '__GROUP__', ?, 1, NOW(), NOW())",
                normalized, name
        );
    }

    @Transactional
    @CacheEvict(cacheNames = {"adminCatalogs", "adminCatalogGroups"}, allEntries = true)
    public void deleteCatalogGroup(String groupCode) {
        jdbcTemplate.update("DELETE FROM admin_catalogs WHERE group_code = ?", groupCode.trim().toUpperCase());
    }
}