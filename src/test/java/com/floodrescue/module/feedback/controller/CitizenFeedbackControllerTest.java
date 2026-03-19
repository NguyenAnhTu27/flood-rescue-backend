package com.floodrescue.module.feedback.controller;

import com.floodrescue.config.security.CustomUserDetailsService;
import com.floodrescue.config.security.JwtTokenProvider;
import com.floodrescue.module.feedback.dto.response.SystemFeedbackResponse;
import com.floodrescue.module.feedback.service.SystemFeedbackService;
import com.floodrescue.module.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CitizenFeedbackController.class)
@AutoConfigureMockMvc(addFilters = false)
class CitizenFeedbackControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private SystemFeedbackService systemFeedbackService;

        @MockBean
        private JwtTokenProvider jwtTokenProvider;

        @MockBean
        private CustomUserDetailsService customUserDetailsService;

        @MockBean
        private UserRepository userRepository;

    @Test
    void getFeedbacksShouldReturnPageOfFeedbacksPublicly() throws Exception {
        SystemFeedbackResponse feedback1 = SystemFeedbackResponse.builder()
                .id(1L)
                .citizenId(10L)
                .citizenName("Citizen 1")
                .citizenEmail("citizen1@example.com")
                .rating(5)
                .feedbackContent("Dịch vụ rất tốt!")
                .rescuedConfirmed(true)
                .reliefConfirmed(false)
                .createdAt(LocalDateTime.of(2026, 3, 17, 10, 0, 0))
                .build();

        SystemFeedbackResponse feedback2 = SystemFeedbackResponse.builder()
                .id(2L)
                .citizenId(11L)
                .citizenName("Citizen 2")
                .citizenEmail("citizen2@example.com")
                .rating(4)
                .feedbackContent("Tốt nhưng cần cải thiện")
                .rescuedConfirmed(false)
                .reliefConfirmed(true)
                .createdAt(LocalDateTime.of(2026, 3, 16, 10, 0, 0))
                .build();

        Page<SystemFeedbackResponse> feedbackPage = new PageImpl<>(
                List.of(feedback1, feedback2),
                PageRequest.of(0, 20),
                2
        );

        when(systemFeedbackService.getFeedbacks(any(Pageable.class)))
                .thenReturn(feedbackPage);

        mockMvc.perform(get("/api/feedback/citizen")
                        .param("page", "0")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.content[0].id").value(1))
                .andExpect(jsonPath("$.content[0].rating").value(5))
                .andExpect(jsonPath("$.content[0].feedbackContent").value("Dịch vụ rất tốt!"))
                .andExpect(jsonPath("$.content[0].citizenName").value("Citizen 1"))
                .andExpect(jsonPath("$.content[1].id").value(2))
                .andExpect(jsonPath("$.content[1].rating").value(4))
                .andExpect(jsonPath("$.totalElements").value(2));
    }

    @Test
    void getFeedbacksShouldDefaultToFirstPageWithSize20() throws Exception {
        Page<SystemFeedbackResponse> emptyPage = new PageImpl<>(
                List.of(),
                PageRequest.of(0, 20),
                0
        );

        when(systemFeedbackService.getFeedbacks(any(Pageable.class)))
                .thenReturn(emptyPage);

        mockMvc.perform(get("/api/feedback/citizen"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(0)))
                .andExpect(jsonPath("$.totalElements").value(0));
    }
}
