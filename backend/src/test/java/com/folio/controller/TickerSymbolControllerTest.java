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
final class TickerSymbolControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void getTickerSymbolsReturnsPaginatedResponse() throws Exception {
        mockMvc.perform(get("/api/ticker-symbols"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.items").isArray())
            .andExpect(jsonPath("$.page", is(1)))
            .andExpect(jsonPath("$.totalItems").isNumber());
    }

    @Test
    void getTickerSymbolsSupportsSortParams() throws Exception {
        mockMvc.perform(get("/api/ticker-symbols")
                .param("sortField", "tickerSymbol")
                .param("sortDir", "desc"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.items").isArray());
    }

    @Test
    void getTickerSymbolsSupportsPagination() throws Exception {
        mockMvc.perform(get("/api/ticker-symbols")
                .param("page", "1")
                .param("pageSize", "50"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.pageSize", is(50)));
    }

    @Test
    void exportTickerSymbolsCsvReturnsFile() throws Exception {
        mockMvc.perform(get("/api/ticker-symbols/export").param("format", "csv"))
            .andExpect(status().isOk())
            .andExpect(header().string("Content-Disposition", containsString("ticker-symbols.csv")));
    }

    @Test
    void exportTickerSymbolsWithSortReturnsFile() throws Exception {
        mockMvc.perform(get("/api/ticker-symbols/export")
                .param("format", "csv")
                .param("sortField", "tickerSymbol")
                .param("sortDir", "asc"))
            .andExpect(status().isOk());
    }
}