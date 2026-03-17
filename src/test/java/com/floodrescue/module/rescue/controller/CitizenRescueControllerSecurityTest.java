package com.floodrescue.module.rescue.controller;

import com.floodrescue.config.security.JwtAuthenticationFilter;
import com.floodrescue.module.rescue.service.RescueRequestService;
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

@WebMvcTest(CitizenRescueController.class)
@AutoConfigureMockMvc(addFilters = true)
class CitizenRescueControllerSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private RescueRequestService rescueRequestService;

    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Test
    void getMyRescueRequests_shouldReturnUnauthorized_whenNoUser() throws Exception {
        mockMvc.perform(get("/api/rescue/citizen/requests")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "CITIZEN")
    void getMyRescueRequests_shouldAllowCitizenRole() throws Exception {
        mockMvc.perform(get("/api/rescue/citizen/requests")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }
}

