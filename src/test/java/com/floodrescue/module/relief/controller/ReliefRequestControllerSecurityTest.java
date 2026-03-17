package com.floodrescue.module.relief.controller;

import com.floodrescue.config.security.JwtAuthenticationFilter;
import com.floodrescue.module.relief.service.ManagerReliefDashboardService;
import com.floodrescue.module.relief.service.ManagerReliefDispatchDashboardService;
import com.floodrescue.module.relief.service.ReliefRequestService;
import com.floodrescue.module.user.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ReliefRequestController.class)
@AutoConfigureMockMvc(addFilters = true)
class ReliefRequestControllerSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ManagerReliefDashboardService dashboardService;

    @MockBean
    private ManagerReliefDispatchDashboardService dispatchDashboardService;

    @MockBean
    private ReliefRequestService reliefRequestService;

    @MockBean
    private UserService userService;

    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Test
    void listReliefRequests_shouldReturnUnauthorized_whenNoUser() throws Exception {
        mockMvc.perform(get("/api/relief/requests")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "MANAGER")
    void listReliefRequests_shouldAllowManagerRole() throws Exception {
        mockMvc.perform(get("/api/relief/requests")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }
}

