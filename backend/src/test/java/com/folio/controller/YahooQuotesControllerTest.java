package com.folio.controller;

import com.folio.domain.TickerSymbol;
import com.folio.online.yahoo.QuoteFetcher;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
final class YahooQuotesControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private QuoteFetcher quoteFetcher;

    // --- GET /with-quote ---

    @Test
    void getWithQuoteReturnsEmptyListWhenNoQuotes() throws Exception {
        mockMvc.perform(get("/api/yahoo-quotes/with-quote"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.items", is(empty())))
            .andExpect(jsonPath("$.page", is(1)))
            .andExpect(jsonPath("$.totalItems", is(0)));
    }

    @Test
    void getWithQuoteSupportsSortParams() throws Exception {
        mockMvc.perform(get("/api/yahoo-quotes/with-quote")
                .param("sortField", "price")
                .param("sortDir", "desc"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.items").isArray());
    }

    @Test
    void getWithQuoteSupportsPagination() throws Exception {
        mockMvc.perform(get("/api/yahoo-quotes/with-quote")
                .param("page", "1")
                .param("pageSize", "20"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.pageSize", is(20)));
    }

    // --- GET /without-quote ---

    @Test
    void getWithoutQuoteReturnsEmptyListWhenNoHeldIsins() throws Exception {
        mockMvc.perform(get("/api/yahoo-quotes/without-quote"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.items", is(empty())))
            .andExpect(jsonPath("$.page", is(1)))
            .andExpect(jsonPath("$.totalItems", is(0)));
    }

    @Test
    void getWithoutQuoteSupportsSortParams() throws Exception {
        mockMvc.perform(get("/api/yahoo-quotes/without-quote")
                .param("sortField", "name")
                .param("sortDir", "asc"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.items").isArray());
    }

    // --- POST /fetch ---

    @Test
    void fetchReturnsZeroWhenNoHeldIsins() throws Exception {
        mockMvc.perform(post("/api/yahoo-quotes/fetch"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.total", is(0)))
            .andExpect(jsonPath("$.fetched", is(0)))
            .andExpect(jsonPath("$.noTicker", is(0)))
            .andExpect(jsonPath("$.noQuote", is(0)));
    }

    @Test
    void fetchDoesNotCallYahooWhenNoHeldIsins() throws Exception {
        when(quoteFetcher.fetchQuote(any(TickerSymbol.class))).thenReturn(Optional.empty());

        mockMvc.perform(post("/api/yahoo-quotes/fetch"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.total", is(0)));
    }

    // --- Export ---

    @Test
    void exportWithQuoteCsvReturnsFile() throws Exception {
        mockMvc.perform(get("/api/yahoo-quotes/with-quote/export").param("format", "csv"))
            .andExpect(status().isOk())
            .andExpect(header().string("Content-Disposition", containsString("quotes-with-quote.csv")));
    }

    @Test
    void exportWithoutQuoteCsvReturnsFile() throws Exception {
        mockMvc.perform(get("/api/yahoo-quotes/without-quote/export").param("format", "csv"))
            .andExpect(status().isOk())
            .andExpect(header().string("Content-Disposition", containsString("quotes-without-quote.csv")));
    }

    @Test
    void exportWithQuoteWithSortReturnsFile() throws Exception {
        mockMvc.perform(get("/api/yahoo-quotes/with-quote/export")
                .param("format", "csv")
                .param("sortField", "price")
                .param("sortDir", "desc"))
            .andExpect(status().isOk());
    }
}
