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
final class ReferenceDataControllerTest {

    @Autowired
    private MockMvc mockMvc;

    // --- Depots ---

    @Test
    void getDepotsReturnsPaginatedResponse() throws Exception {
        mockMvc.perform(get("/api/depots").param("pageSize", "-1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.items", hasSize(2)))
            .andExpect(jsonPath("$.items[0].name", is("DeGiro")))
            .andExpect(jsonPath("$.items[1].name", is("ZERO")))
            .andExpect(jsonPath("$.totalItems", is(2)))
            .andExpect(jsonPath("$.page", is(1)));
    }

    @Test
    void getDepotsDefaultPaginationReturnsPage1() throws Exception {
        mockMvc.perform(get("/api/depots"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.page", is(1)))
            .andExpect(jsonPath("$.pageSize", is(10)));
    }

    @Test
    void getDepotsSortDescReversesSortOrder() throws Exception {
        mockMvc.perform(get("/api/depots").param("sortDir", "desc").param("pageSize", "-1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.items[0].name", is("ZERO")))
            .andExpect(jsonPath("$.items[1].name", is("DeGiro")));
    }

    @Test
    void exportDepotsCsvReturnsDownloadableFile() throws Exception {
        mockMvc.perform(get("/api/depots/export").param("format", "csv"))
            .andExpect(status().isOk())
            .andExpect(header().string("Content-Disposition", containsString("depots.csv")));
    }

    @Test
    void exportDepotsExcelReturnsXlsx() throws Exception {
        mockMvc.perform(get("/api/depots/export").param("format", "xlsx"))
            .andExpect(status().isOk())
            .andExpect(header().string("Content-Disposition", containsString("depots.xlsx")));
    }

    // --- Currencies ---

    @Test
    void getCurrenciesReturnsAllCurrenciesSorted() throws Exception {
        mockMvc.perform(get("/api/currencies").param("pageSize", "-1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.items", hasSize(30)))
            .andExpect(jsonPath("$.items[0].name", is("AUD")))
            .andExpect(jsonPath("$.items[29].name", is("ZAR")));
    }

    @Test
    void getCurrenciesSortDescReturnsReversed() throws Exception {
        mockMvc.perform(get("/api/currencies").param("sortDir", "desc").param("pageSize", "-1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.items[0].name", is("ZAR")));
    }

    @Test
    void getCurrenciesPaginationSlicesCorrectly() throws Exception {
        mockMvc.perform(get("/api/currencies").param("page", "1").param("pageSize", "10"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.items", hasSize(10)))
            .andExpect(jsonPath("$.totalItems", is(30)))
            .andExpect(jsonPath("$.totalPages", is(3)));
    }

    @Test
    void exportCurrenciesCsvReturnsDownloadableFile() throws Exception {
        mockMvc.perform(get("/api/currencies/export").param("format", "csv"))
            .andExpect(status().isOk())
            .andExpect(header().string("Content-Disposition", containsString("currencies.csv")));
    }

    // --- Countries ---

    @Test
    void getCountriesReturnsPaginatedJsonArray() throws Exception {
        mockMvc.perform(get("/api/countries"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.items").isArray())
            .andExpect(jsonPath("$.page", is(1)));
    }

    @Test
    void getCountriesInvalidSortFieldFallsBackToDefault() throws Exception {
        mockMvc.perform(get("/api/countries").param("sortField", "invalid").param("pageSize", "-1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.items").isArray());
    }

    @Test
    void exportCountriesCsvReturnsFile() throws Exception {
        mockMvc.perform(get("/api/countries/export").param("format", "csv"))
            .andExpect(status().isOk())
            .andExpect(header().string("Content-Disposition", containsString("countries.csv")));
    }

    // --- Branches ---

    @Test
    void getBranchesReturnsPaginatedJsonArray() throws Exception {
        mockMvc.perform(get("/api/branches"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.items").isArray())
            .andExpect(jsonPath("$.page", is(1)));
    }

    @Test
    void exportBranchesCsvReturnsFile() throws Exception {
        mockMvc.perform(get("/api/branches/export").param("format", "csv"))
            .andExpect(status().isOk())
            .andExpect(header().string("Content-Disposition", containsString("branches.csv")));
    }

    @Test
    void exportBranchesWithSortDescReturnsFile() throws Exception {
        mockMvc.perform(get("/api/branches/export")
                .param("format", "csv")
                .param("sortField", "name")
                .param("sortDir", "desc"))
            .andExpect(status().isOk())
            .andExpect(header().string("Content-Disposition", containsString("branches.csv")));
    }

    // --- Pagination edge cases ---

    @Test
    void getDepotsOutOfRangePageReturnsEmptyItems() throws Exception {
        mockMvc.perform(get("/api/depots").param("page", "999").param("pageSize", "10"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.items", is(empty())))
            .andExpect(jsonPath("$.totalItems", is(2)));
    }

    @Test
    void getDepotsPageSizeMinusOneReturnsAll() throws Exception {
        mockMvc.perform(get("/api/depots").param("pageSize", "-1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.items", hasSize(2)))
            .andExpect(jsonPath("$.page", is(1)))
            .andExpect(jsonPath("$.totalPages", is(1)));
    }

    @Test
    void getDepotsInvalidPageSizeFallsBackToTen() throws Exception {
        mockMvc.perform(get("/api/depots").param("pageSize", "7"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.pageSize", is(10)));
    }
}