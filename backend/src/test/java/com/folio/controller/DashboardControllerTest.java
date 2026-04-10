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
final class DashboardControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void getDashboardReturnsExpectedStructure() throws Exception {
        mockMvc.perform(get("/api/dashboard"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.totalPortfolioValue").isNumber())
            .andExpect(jsonPath("$.stockCount").isNumber())
            .andExpect(jsonPath("$.totalDividendRatio").isNumber())
            .andExpect(jsonPath("$.top5Holdings").isArray())
            .andExpect(jsonPath("$.top5DividendSources").isArray());
    }

    @Test
    void getDashboardEmptyPortfolioReturnsZeros() throws Exception {
        mockMvc.perform(get("/api/dashboard"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.totalPortfolioValue", is(0.0)))
            .andExpect(jsonPath("$.stockCount", is(0)))
            .andExpect(jsonPath("$.top5Holdings", hasSize(0)))
            .andExpect(jsonPath("$.top5DividendSources", hasSize(0)));
    }

    @Test
    void getDashboardLastQuoteFetchAtNullWhenNoFetch() throws Exception {
        mockMvc.perform(get("/api/dashboard"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.lastQuoteFetchAt").doesNotExist());
    }

    @Test
    void exportHoldingsCsvReturnsFile() throws Exception {
        mockMvc.perform(get("/api/dashboard/holdings/export").param("format", "csv"))
            .andExpect(status().isOk())
            .andExpect(header().string("Content-Disposition", containsString("top5-holdings.csv")));
    }

    @Test
    void exportHoldingsExcelReturnsFile() throws Exception {
        mockMvc.perform(get("/api/dashboard/holdings/export").param("format", "xlsx"))
            .andExpect(status().isOk())
            .andExpect(header().string("Content-Disposition", containsString("top5-holdings.xlsx")));
    }

    @Test
    void exportDividendsCsvReturnsFile() throws Exception {
        mockMvc.perform(get("/api/dashboard/dividends/export").param("format", "csv"))
            .andExpect(status().isOk())
            .andExpect(header().string("Content-Disposition", containsString("top5-dividends.csv")));
    }
}
