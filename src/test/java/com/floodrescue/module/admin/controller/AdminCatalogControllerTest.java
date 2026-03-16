package com.floodrescue.module.admin.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.floodrescue.config.security.CustomUserDetailsService;
import com.floodrescue.config.security.JwtTokenProvider;
import com.floodrescue.module.admin.service.AdminCatalogService;
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

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AdminCatalogController.class)
@AutoConfigureMockMvc(addFilters = false)
class AdminCatalogControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AdminCatalogService adminCatalogService;

    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    @MockBean
    private CustomUserDetailsService customUserDetailsService;

    @Test
    @WithMockUser(username = "1", roles = "ADMIN")
    void getCatalogsShouldReturnList() throws Exception {
        when(adminCatalogService.getCatalogs())
                .thenReturn(List.of(Map.of("id", 1, "name", "Flood Zone A")));

        mockMvc.perform(get("/api/admin/catalogs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].name").value("Flood Zone A"));
    }

    @Test
    @WithMockUser(username = "1", roles = "ADMIN")
    void createCatalogShouldReturn200() throws Exception {
        Map<String, Object> payload = Map.of("name", "New Zone", "groupCode", "ZONE");

        mockMvc.perform(post("/api/admin/catalogs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Tạo catalog thành công"));
    }

    @Test
    @WithMockUser(username = "1", roles = "ADMIN")
    void deleteCatalogShouldReturn200() throws Exception {
        mockMvc.perform(delete("/api/admin/catalogs/3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Xóa catalog thành công"));
    }

    @Test
    @WithMockUser(username = "1", roles = "ADMIN")
    void getCatalogGroupsShouldReturnList() throws Exception {
        when(adminCatalogService.getCatalogGroups())
                .thenReturn(List.of(Map.of("groupCode", "ZONE", "groupName", "Khu vực")));

        mockMvc.perform(get("/api/admin/catalog-groups"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].groupCode").value("ZONE"));
    }
}
