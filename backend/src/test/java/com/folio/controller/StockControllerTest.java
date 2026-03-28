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
final class StockControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void getStocks_returnsPaginatedEmptyListWhenNoPositions() throws Exception {
        mockMvc.perform(get("/api/stocks"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.items", is(empty())))
            .andExpect(jsonPath("$.page", is(1)))
            .andExpect(jsonPath("$.totalItems", is(0)));
    }

    @Test
    void getStocks_supportsSortParams() throws Exception {
        mockMvc.perform(get("/api/stocks")
                .param("sortField", "name")
                .param("sortDir", "desc"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.items").isArray());
    }

    @Test
    void getStocks_supportsFilterParams() throws Exception {
        mockMvc.perform(get("/api/stocks")
                .param("country", "Germany")
                .param("branch", "Technology"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.items").isArray());
    }

    @Test
    void getStocks_supportsPaginationParams() throws Exception {
        mockMvc.perform(get("/api/stocks")
                .param("page", "1")
                .param("pageSize", "20"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.pageSize", is(20)));
    }

    @Test
    void getStockFilters_returnsCountriesAndBranches() throws Exception {
        mockMvc.perform(get("/api/stocks/filters"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.countries").isArray())
            .andExpect(jsonPath("$.branches").isArray());
    }

    @Test
    void exportStocks_csv_returnsFile() throws Exception {
        mockMvc.perform(get("/api/stocks/export").param("format", "csv"))
            .andExpect(status().isOk())
            .andExpect(header().string("Content-Disposition", containsString("stocks.csv")));
    }

    @Test
    void exportStocks_excel_returnsFile() throws Exception {
        mockMvc.perform(get("/api/stocks/export").param("format", "xlsx"))
            .andExpect(status().isOk())
            .andExpect(header().string("Content-Disposition", containsString("stocks.xlsx")));
    }

    @Test
    void exportStocks_withCountryFilter_returnsFile() throws Exception {
        mockMvc.perform(get("/api/stocks/export")
                .param("format", "csv")
                .param("country", "Germany"))
            .andExpect(status().isOk())
            .andExpect(header().string("Content-Disposition", containsString("stocks.csv")));
    }

    @Test
    void exportStocks_withBranchFilter_returnsFile() throws Exception {
        mockMvc.perform(get("/api/stocks/export")
                .param("format", "csv")
                .param("branch", "Technology"))
            .andExpect(status().isOk())
            .andExpect(header().string("Content-Disposition", containsString("stocks.csv")));
    }

    @Test
    void exportStocks_withSorting_returnsFile() throws Exception {
        mockMvc.perform(get("/api/stocks/export")
                .param("format", "csv")
                .param("sortField", "isin")
                .param("sortDir", "desc"))
            .andExpect(status().isOk())
            .andExpect(header().string("Content-Disposition", containsString("stocks.csv")));
    }
}