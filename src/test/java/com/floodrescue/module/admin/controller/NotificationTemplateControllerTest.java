package com.floodrescue.module.admin.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.floodrescue.config.security.CustomUserDetailsService;
import com.floodrescue.config.security.JwtTokenProvider;
import com.floodrescue.module.admin.service.AuditLogService;
import com.floodrescue.module.admin.service.NotificationTemplateService;
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

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(NotificationTemplateController.class)
@AutoConfigureMockMvc(addFilters = false)
class NotificationTemplateControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private NotificationTemplateService notificationTemplateService;

    @MockBean
    private AuditLogService auditLogService;

    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    @MockBean
    private CustomUserDetailsService customUserDetailsService;

    @Test
    @WithMockUser(username = "1", roles = "ADMIN")
    void getTemplatesShouldReturnPagedResult() throws Exception {
        when(notificationTemplateService.getNotificationTemplates(anyInt(), anyInt(), isNull()))
                .thenReturn(Map.of("content", List.of(), "totalElements", 0));

        mockMvc.perform(get("/api/admin/notification-templates"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    @WithMockUser(username = "1", roles = "ADMIN")
    void createTemplateShouldReturn200() throws Exception {
        when(notificationTemplateService.createNotificationTemplate(org.mockito.ArgumentMatchers.any()))
                .thenReturn("WELCOME_EMAIL");

        Map<String, Object> payload = Map.of("code", "WELCOME_EMAIL", "title", "Chào mừng", "body", "...");

        mockMvc.perform(post("/api/admin/notification-templates")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Tạo mẫu thông báo thành công"));
    }

    @Test
    @WithMockUser(username = "1", roles = "ADMIN")
    void deleteTemplateShouldReturn200() throws Exception {
        mockMvc.perform(delete("/api/admin/notification-templates/5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Xóa mẫu thông báo thành công"));
    }
}
