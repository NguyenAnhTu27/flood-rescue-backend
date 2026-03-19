package com.floodrescue.module.feedback.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.floodrescue.config.security.CustomUserDetailsService;
import com.floodrescue.config.security.JwtTokenProvider;
import com.floodrescue.module.admin.service.AuditLogService;
import com.floodrescue.module.feedback.dto.response.SystemFeedbackReplyResponse;
import com.floodrescue.module.feedback.service.SystemFeedbackService;
import com.floodrescue.module.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AdminFeedbackController.class)
@AutoConfigureMockMvc(addFilters = false)
class AdminFeedbackControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private SystemFeedbackService systemFeedbackService;

        @MockBean
        private AuditLogService auditLogService;

    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    @MockBean
    private CustomUserDetailsService customUserDetailsService;

    @MockBean
    private UserRepository userRepository;

    @Test
    void adminCanCreateReply() throws Exception {
        SystemFeedbackReplyResponse response = SystemFeedbackReplyResponse.builder()
                .id(1L)
                .feedbackId(10L)
                .userId(2L)
                .userName("Admin")
                .userRole("ADMIN")
                .content("Cam on ban da phan hoi")
                .createdAt(LocalDateTime.now())
                .build();

        when(systemFeedbackService.createReply(anyLong(), any(), any())).thenReturn(response);

        mockMvc.perform(post("/api/feedback/admin/10/replies")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("content", "Cam on ban da phan hoi"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.feedbackId").value(10))
                .andExpect(jsonPath("$.userRole").value("ADMIN"));
    }

    @Test
    void adminCanGetReplies() throws Exception {
        SystemFeedbackReplyResponse reply = SystemFeedbackReplyResponse.builder()
                .id(1L)
                .feedbackId(10L)
                .userRole("CITIZEN")
                .content("Toi da nhan ho tro")
                .build();

        when(systemFeedbackService.getReplies(10L)).thenReturn(List.of(reply));

        mockMvc.perform(get("/api/feedback/admin/10/replies"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].feedbackId").value(10))
                .andExpect(jsonPath("$[0].userRole").value("CITIZEN"));
    }

    @Test
    void adminCanDeleteFeedback() throws Exception {
                doNothing().when(systemFeedbackService).deleteFeedback(anyLong(), anyLong(), anyString());

        mockMvc.perform(delete("/api/feedback/admin/10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }
}