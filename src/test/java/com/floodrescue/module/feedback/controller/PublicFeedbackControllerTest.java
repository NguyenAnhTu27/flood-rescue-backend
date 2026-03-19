package com.floodrescue.module.feedback.controller;

import com.floodrescue.config.security.CustomUserDetailsService;
import com.floodrescue.config.security.JwtTokenProvider;
import com.floodrescue.module.feedback.dto.response.SystemFeedbackResponse;
import com.floodrescue.module.feedback.service.SystemFeedbackService;
import com.floodrescue.module.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PublicFeedbackController.class)
@AutoConfigureMockMvc(addFilters = false)
class PublicFeedbackControllerTest {

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
    void publicFeedbackEndpointShouldReturnFeedbackWithoutEmail() throws Exception {
        SystemFeedbackResponse feedback = SystemFeedbackResponse.builder()
                .id(1L)
                .citizenId(10L)
                .citizenName("Citizen 1")
                .citizenEmail("citizen1@example.com")
                .rating(5)
                .feedbackContent("Rat hai long")
                .rescuedConfirmed(true)
                .reliefConfirmed(true)
                .createdAt(LocalDateTime.of(2026, 3, 17, 10, 0, 0))
                .build();

        Page<SystemFeedbackResponse> page = new PageImpl<>(List.of(feedback), PageRequest.of(0, 20), 1);
        when(systemFeedbackService.getFeedbacks(any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/api/public/feedbacks"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].citizenName").value("Citizen 1"))
                .andExpect(jsonPath("$.content[0].rating").value(5))
                .andExpect(jsonPath("$.content[0].feedbackContent").value("Rat hai long"))
                .andExpect(jsonPath("$.content[0].citizenEmail").doesNotExist())
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    void publicFeedbackAliasShouldWork() throws Exception {
        when(systemFeedbackService.getFeedbacks(any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(), PageRequest.of(0, 20), 0));

        mockMvc.perform(get("/api/public/citizen-feedbacks/list"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(0)));
    }
}