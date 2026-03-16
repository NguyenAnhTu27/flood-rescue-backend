package com.floodrescue.module.admin.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.floodrescue.config.security.CustomUserDetailsService;
import com.floodrescue.config.security.JwtTokenProvider;
import com.floodrescue.module.admin.service.AuditLogService;
import com.floodrescue.module.admin.service.PermissionService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PermissionController.class)
@AutoConfigureMockMvc(addFilters = false)
class PermissionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private PermissionService permissionService;

    @MockBean
    private AuditLogService auditLogService;

    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    @MockBean
    private CustomUserDetailsService customUserDetailsService;

    @Test
    @WithMockUser(username = "1", roles = "ADMIN")
    void getPermissionsShouldReturnOverview() throws Exception {
        when(permissionService.getPermissionsOverview())
                .thenReturn(Map.of("roles", List.of(), "permissions", List.of()));

        mockMvc.perform(get("/api/admin/permissions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.roles").isArray())
                .andExpect(jsonPath("$.data.permissions").isArray());
    }

    @Test
    @WithMockUser(username = "1", roles = "ADMIN")
    void updateRolePermissionsShouldReturn200() throws Exception {
        when(permissionService.updateRolePermissions(anyString(), org.mockito.ArgumentMatchers.any()))
                .thenReturn(2L);

        Map<String, Object> payload = Map.of("permissions", List.of("READ_USER", "WRITE_USER"));

        mockMvc.perform(put("/api/admin/roles/RESCUE_TEAM/permissions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Cập nhật phân quyền thành công"));
    }

    @Test
    @WithMockUser(username = "1", roles = "ADMIN")
    void updateRolePermissionsShouldReturn400WhenRoleNotFound() throws Exception {
        when(permissionService.updateRolePermissions(anyString(), org.mockito.ArgumentMatchers.any()))
                .thenReturn(null);

        Map<String, Object> payload = Map.of("permissions", List.of());

        mockMvc.perform(put("/api/admin/roles/INVALID_ROLE/permissions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Vai trò không tồn tại"));
    }
}
