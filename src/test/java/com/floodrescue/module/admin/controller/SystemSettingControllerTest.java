package com.floodrescue.module.admin.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.floodrescue.config.security.CustomUserDetailsService;
import com.floodrescue.config.security.JwtTokenProvider;
import com.floodrescue.module.admin.service.AuditLogService;
import com.floodrescue.module.admin.service.SystemSettingService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(SystemSettingController.class)
@AutoConfigureMockMvc(addFilters = false)
class SystemSettingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private SystemSettingService systemSettingService;

    @MockBean
    private AuditLogService auditLogService;

    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    @MockBean
    private CustomUserDetailsService customUserDetailsService;

    @Test
    @WithMockUser(username = "1", roles = "ADMIN")
    void getSystemSettingsShouldReturnValues() throws Exception {
        when(systemSettingService.getAllSystemSettings())
                .thenReturn(Map.of("maxFileSize", "10MB", "maintenanceMode", "false"));

        mockMvc.perform(get("/api/admin/system-settings"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.values").isMap());
    }

    @Test
    @WithMockUser(username = "1", roles = "ADMIN")
    void updateSystemSettingsShouldReturn200() throws Exception {
        Map<String, Object> payload = Map.of("maintenanceMode", "true");

        mockMvc.perform(put("/api/admin/system-settings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Đã lưu cấu hình"));
    }

    @Test
    @WithMockUser(username = "1", roles = "ADMIN")
    void getContentPagesShouldReturnMap() throws Exception {
        when(systemSettingService.getContentPages())
                .thenReturn(Map.of("terms", "...", "privacy", "..."));

        mockMvc.perform(get("/api/admin/content-pages"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.terms").value("..."));
    }

    @Test
    @WithMockUser(username = "1", roles = "ADMIN")
    void updateContentPagesShouldReturn200() throws Exception {
        Map<String, Object> payload = Map.of("terms", "New terms");

        mockMvc.perform(put("/api/admin/content-pages")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Đã lưu nội dung trang"));
    }
}
