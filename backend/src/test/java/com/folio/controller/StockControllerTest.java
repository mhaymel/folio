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
    void getStocks_returnsEmptyListWhenNoPositions() throws Exception {
        mockMvc.perform(get("/api/stocks"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", is(empty())));
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
