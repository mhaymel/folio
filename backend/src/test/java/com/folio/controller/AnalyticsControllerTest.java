package com.folio.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
final class AnalyticsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void getCountryDiversification_returnsExpectedStructure() throws Exception {
        mockMvc.perform(get("/api/analytics/countries"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.entries").isArray())
            .andExpect(jsonPath("$.totalInvested").isNumber());
    }

    @Test
    void getBranchDiversification_returnsExpectedStructure() throws Exception {
        mockMvc.perform(get("/api/analytics/branches"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.entries").isArray())
            .andExpect(jsonPath("$.totalInvested").isNumber());
    }

    @Test
    void exportCountryDiversification_csv_returnsFile() throws Exception {
        mockMvc.perform(get("/api/analytics/countries/export").param("format", "csv"))
            .andExpect(status().isOk())
            .andExpect(header().string("Content-Disposition", containsString("countries-diversification.csv")));
    }

    @Test
    void exportBranchDiversification_csv_returnsFile() throws Exception {
        mockMvc.perform(get("/api/analytics/branches/export").param("format", "csv"))
            .andExpect(status().isOk())
            .andExpect(header().string("Content-Disposition", containsString("branches-diversification.csv")));
    }

    @Test
    void exportDiversification_excel_returnsFile() throws Exception {
        mockMvc.perform(get("/api/analytics/countries/export").param("format", "xlsx"))
            .andExpect(status().isOk())
            .andExpect(header().string("Content-Disposition", containsString("countries-diversification.xlsx")));
    }

    @Test
    void exportDiversification_withSort_returnsFile() throws Exception {
        mockMvc.perform(get("/api/analytics/countries/export")
                .param("format", "csv")
                .param("sortField", "investedAmount")
                .param("sortDir", "desc"))
            .andExpect(status().isOk());
    }
}
