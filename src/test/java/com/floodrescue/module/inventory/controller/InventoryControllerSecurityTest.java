package com.floodrescue.module.inventory.controller;

import com.floodrescue.config.security.JwtAuthenticationFilter;
import com.floodrescue.module.inventory.service.InventoryCatalogService;
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

@WebMvcTest(InventoryController.class)
@AutoConfigureMockMvc(addFilters = true)
class InventoryControllerSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private InventoryCatalogService inventoryCatalogService;

    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Test
    void getStockBalances_shouldReturnUnauthorized_whenNoUser() throws Exception {
        mockMvc.perform(get("/api/inventory/stock")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "MANAGER")
    void getStockBalances_shouldAllowManagerRole() throws Exception {
        mockMvc.perform(get("/api/inventory/stock")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }
}

