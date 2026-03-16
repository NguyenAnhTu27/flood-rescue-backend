package com.floodrescue.module.auth.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.floodrescue.config.security.CustomUserDetailsService;
import com.floodrescue.config.security.JwtTokenProvider;
import com.floodrescue.module.auth.dto.response.LoginResponse;
import com.floodrescue.module.auth.service.AuthService;
import com.floodrescue.module.auth.service.LoginAttemptLimiter;
import com.floodrescue.module.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerWebMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthService authService;

    @MockBean
    private UserRepository userRepository;

        @MockBean
        private LoginAttemptLimiter loginAttemptLimiter;

        @MockBean
        private JwtTokenProvider jwtTokenProvider;

        @MockBean
        private CustomUserDetailsService customUserDetailsService;

    @Test
    void registerShouldReturnTokenPayload() throws Exception {
        LoginResponse mockResponse = LoginResponse.builder()
                .message("Đăng ký Citizen thành công")
                .token("mock-jwt-token")
                .tokenType("Bearer")
                .userId(1L)
                .fullName("Nguyen Van A")
                .role("CITIZEN")
                .build();

        when(authService.registerAndLogin(any())).thenReturn(mockResponse);

        String requestBody = objectMapper.writeValueAsString(new RegisterRequestFixture(
                "Nguyen Van A",
                "0912345678",
                "user@example.com",
                "Abc12345"
        ));

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.message").value("Đăng ký Citizen thành công"))
                .andExpect(jsonPath("$.data.token").value("mock-jwt-token"))
                .andExpect(jsonPath("$.data.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.data.userId").value(1))
                .andExpect(jsonPath("$.data.role").value("CITIZEN"));
    }

    private record RegisterRequestFixture(
            String fullName,
            String phone,
            String email,
            String password
    ) {}
}
