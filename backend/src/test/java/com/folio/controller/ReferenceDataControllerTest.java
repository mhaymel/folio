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
    void getDepots_returnsSeededDepotsSortedAlphabetically() throws Exception {
        mockMvc.perform(get("/api/depots"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(2)))
            .andExpect(jsonPath("$[0].name", is("DeGiro")))
            .andExpect(jsonPath("$[1].name", is("ZERO")));
    }

    @Test
    void exportDepots_csv_returnsDownloadableFile() throws Exception {
        mockMvc.perform(get("/api/depots/export").param("format", "csv"))
            .andExpect(status().isOk())
            .andExpect(header().string("Content-Disposition", containsString("depots.csv")));
    }

    @Test
    void exportDepots_excel_returnsXlsx() throws Exception {
        mockMvc.perform(get("/api/depots/export").param("format", "xlsx"))
            .andExpect(status().isOk())
            .andExpect(header().string("Content-Disposition", containsString("depots.xlsx")));
    }

    // --- Currencies ---

    @Test
    void getCurrencies_returnsSeededCurrenciesSortedAlphabetically() throws Exception {
        mockMvc.perform(get("/api/currencies"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(30)))
            .andExpect(jsonPath("$[0].name", is("AUD")))
            .andExpect(jsonPath("$[29].name", is("ZAR")));
    }

    @Test
    void exportCurrencies_csv_returnsDownloadableFile() throws Exception {
        mockMvc.perform(get("/api/currencies/export").param("format", "csv"))
            .andExpect(status().isOk())
            .andExpect(header().string("Content-Disposition", containsString("currencies.csv")));
    }

    // --- Countries ---

    @Test
    void getCountries_returnsJsonArray() throws Exception {
        mockMvc.perform(get("/api/countries"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isArray());
    }

    @Test
    void exportCountries_csv_returnsFile() throws Exception {
        mockMvc.perform(get("/api/countries/export").param("format", "csv"))
            .andExpect(status().isOk())
            .andExpect(header().string("Content-Disposition", containsString("countries.csv")));
    }

    // --- Branches ---

    @Test
    void getBranches_returnsJsonArray() throws Exception {
        mockMvc.perform(get("/api/branches"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isArray());
    }

    @Test
    void exportBranches_csv_returnsFile() throws Exception {
        mockMvc.perform(get("/api/branches/export").param("format", "csv"))
            .andExpect(status().isOk())
            .andExpect(header().string("Content-Disposition", containsString("branches.csv")));
    }

    @Test
    void exportBranches_withSortDesc_returnsFile() throws Exception {
        mockMvc.perform(get("/api/branches/export")
                .param("format", "csv")
                .param("sortField", "name")
                .param("sortDir", "desc"))
            .andExpect(status().isOk())
            .andExpect(header().string("Content-Disposition", containsString("branches.csv")));
    }
}
