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
final class StocksPerDepotControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void getStocksPerDepotReturnsPaginatedEmptyListWhenNoPositions() throws Exception {
        mockMvc.perform(get("/api/stocks-per-depot"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.items", is(empty())))
            .andExpect(jsonPath("$.page", is(1)))
            .andExpect(jsonPath("$.totalItems", is(0)));
    }

    @Test
    void getStocksPerDepotSupportsSortParams() throws Exception {
        mockMvc.perform(get("/api/stocks-per-depot")
                .param("sortField", "name")
                .param("sortDir", "desc"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.items").isArray());
    }

    @Test
    void getStocksPerDepotSupportsFilterParams() throws Exception {
        mockMvc.perform(get("/api/stocks-per-depot")
                .param("country", "Germany")
                .param("branch", "Technology"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.items").isArray());
    }

    @Test
    void getStocksPerDepotSupportsPaginationParams() throws Exception {
        mockMvc.perform(get("/api/stocks-per-depot")
                .param("page", "1")
                .param("pageSize", "20"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.pageSize", is(20)));
    }

    @Test
    void getStocksPerDepotSupportsDepotFilter() throws Exception {
        mockMvc.perform(get("/api/stocks-per-depot")
                .param("depot", "DeGiro"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.items").isArray());
    }

    @Test
    void getStockFiltersReturnsCountriesBranchesAndDepots() throws Exception {
        mockMvc.perform(get("/api/stocks-per-depot/filters"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.countries").isArray())
            .andExpect(jsonPath("$.branches").isArray())
            .andExpect(jsonPath("$.depots").isArray());
    }

    @Test
    void exportStocksPerDepotCsvReturnsFile() throws Exception {
        mockMvc.perform(get("/api/stocks-per-depot/export").param("format", "csv"))
            .andExpect(status().isOk())
            .andExpect(header().string("Content-Disposition", containsString("stocks-per-depot.csv")));
    }

    @Test
    void exportStocksPerDepotExcelReturnsFile() throws Exception {
        mockMvc.perform(get("/api/stocks-per-depot/export").param("format", "xlsx"))
            .andExpect(status().isOk())
            .andExpect(header().string("Content-Disposition", containsString("stocks-per-depot.xlsx")));
    }

    @Test
    void exportStocksPerDepotWithCountryFilterReturnsFile() throws Exception {
        mockMvc.perform(get("/api/stocks-per-depot/export")
                .param("format", "csv")
                .param("country", "Germany"))
            .andExpect(status().isOk())
            .andExpect(header().string("Content-Disposition", containsString("stocks-per-depot.csv")));
    }

    @Test
    void exportStocksPerDepotWithBranchFilterReturnsFile() throws Exception {
        mockMvc.perform(get("/api/stocks-per-depot/export")
                .param("format", "csv")
                .param("branch", "Technology"))
            .andExpect(status().isOk())
            .andExpect(header().string("Content-Disposition", containsString("stocks-per-depot.csv")));
    }

    @Test
    void exportStocksPerDepotWithSortingReturnsFile() throws Exception {
        mockMvc.perform(get("/api/stocks-per-depot/export")
                .param("format", "csv")
                .param("sortField", "isin")
                .param("sortDir", "desc"))
            .andExpect(status().isOk())
            .andExpect(header().string("Content-Disposition", containsString("stocks-per-depot.csv")));
    }
}
