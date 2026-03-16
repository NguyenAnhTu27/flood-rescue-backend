package com.floodrescue.module.admin.controller;

import com.floodrescue.config.security.CustomUserDetailsService;
import com.floodrescue.config.security.JwtTokenProvider;
import com.floodrescue.module.admin.dto.AdminDashboardStatsResponse;
import com.floodrescue.module.admin.service.AdminDashboardService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AdminDashboardController.class)
@AutoConfigureMockMvc(addFilters = false)
class AdminDashboardControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AdminDashboardService adminDashboardService;

    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    @MockBean
    private CustomUserDetailsService customUserDetailsService;

    @Test
    @WithMockUser(username = "1", roles = "ADMIN")
    void getStatsShouldReturnUserCounts() throws Exception {
        when(adminDashboardService.getStats()).thenReturn(
                AdminDashboardStatsResponse.builder()
                        .totalUsers(100L)
                        .activeUsers(80L)
                        .lockedUsers(20L)
                        .build()
        );

        mockMvc.perform(get("/api/admin/stats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalUsers").value(100))
                .andExpect(jsonPath("$.data.activeUsers").value(80))
                .andExpect(jsonPath("$.data.lockedUsers").value(20));
    }


}
