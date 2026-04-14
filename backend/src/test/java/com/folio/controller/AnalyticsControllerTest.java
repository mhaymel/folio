package com.folio.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
final class AnalyticsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void getCountryDiversificationReturnsPaginatedResponse() throws Exception {
        mockMvc.perform(get("/api/analytics/countries"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.items").isArray())
            .andExpect(jsonPath("$.page", is(1)))
            .andExpect(jsonPath("$.totalItems").isNumber());
    }

    @Test
    void getBranchDiversificationReturnsPaginatedResponse() throws Exception {
        mockMvc.perform(get("/api/analytics/branches"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.items").isArray())
            .andExpect(jsonPath("$.page", is(1)));
    }

    @Test
    void getCountryDiversificationSupportsSortParams() throws Exception {
        mockMvc.perform(get("/api/analytics/countries")
                .param("sortField", "name")
                .param("sortDir", "asc"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.items").isArray());
    }

    @Test
    void getCountryDiversificationSupportsPagination() throws Exception {
        mockMvc.perform(get("/api/analytics/countries")
                .param("page", "1")
                .param("pageSize", "20"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.pageSize", is(20)));
    }

    @Test
    void exportCountryDiversificationCsvReturnsFile() throws Exception {
        mockMvc.perform(get("/api/analytics/countries/export").param("format", "csv"))
            .andExpect(status().isOk())
            .andExpect(header().string("Content-Disposition", containsString("countries-diversification.csv")));
    }

    @Test
    void exportBranchDiversificationCsvReturnsFile() throws Exception {
        mockMvc.perform(get("/api/analytics/branches/export").param("format", "csv"))
            .andExpect(status().isOk())
            .andExpect(header().string("Content-Disposition", containsString("branches-diversification.csv")));
    }

    @Test
    void exportDiversificationExcelReturnsFile() throws Exception {
        mockMvc.perform(get("/api/analytics/countries/export").param("format", "xlsx"))
            .andExpect(status().isOk())
            .andExpect(header().string("Content-Disposition", containsString("countries-diversification.xlsx")));
    }

    @Test
    void exportDiversificationWithSortReturnsFile() throws Exception {
        mockMvc.perform(get("/api/analytics/countries/export")
                .param("format", "csv")
                .param("sortField", "investedAmount")
                .param("sortDir", "desc"))
            .andExpect(status().isOk());
    }
}