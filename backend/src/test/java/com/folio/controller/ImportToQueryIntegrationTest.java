package com.folio.controller;

import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * End-to-end integration test: imports reference data and transactions,
 * then verifies the query endpoints return consistent, computed results.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
final class ImportToQueryIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    // countries.csv format: ISIN;Name;CountryEntity (no header)
    @Test
    @Order(1)
    void step1ImportCountries() throws Exception {
        String csv = "DE000BASF111;BASF SE;Germany\nUS0378331005;Apple Inc.;USA";
        MockMultipartFile file = new MockMultipartFile("file", "countries.csv", "text/csv", csv.getBytes());

        mockMvc.perform(multipart("/api/import/countries").file(file))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success", is(true)))
            .andExpect(jsonPath("$.imported", is(2)));
    }

    // branches.csv format: ISIN;Name;BranchEntity (no header)
    @Test
    @Order(2)
    void step2ImportBranches() throws Exception {
        String csv = "DE000BASF111;BASF SE;Chemicals\nUS0378331005;Apple Inc.;Technology";
        MockMultipartFile file = new MockMultipartFile("file", "branches.csv", "text/csv", csv.getBytes());

        mockMvc.perform(multipart("/api/import/branches").file(file))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success", is(true)))
            .andExpect(jsonPath("$.imported", is(2)));
    }

    // dividende.csv format: ISIN;Name;CurrencyEntity;DPS (no header)
    @Test
    @Order(3)
    void step3ImportDividends() throws Exception {
        String csv = "DE000BASF111;BASF SE;EUR;3,40\nUS0378331005;Apple Inc.;USD;0,96";
        MockMultipartFile file = new MockMultipartFile("file", "dividende.csv", "text/csv", csv.getBytes());

        mockMvc.perform(multipart("/api/import/dividends").file(file))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success", is(true)))
            .andExpect(jsonPath("$.imported", is(2)));
    }

    // ticker_symbol.csv format: ISIN;TickerSymbolEntity;Name (no header)
    @Test
    @Order(4)
    void step4ImportTickerSymbols() throws Exception {
        String csv = "DE000BASF111;BAS.DE;BASF SE\nUS0378331005;AAPL;Apple Inc.";
        MockMultipartFile file = new MockMultipartFile("file", "ticker_symbol.csv", "text/csv", csv.getBytes());

        mockMvc.perform(multipart("/api/import/ticker-symbols").file(file))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success", is(true)))
            .andExpect(jsonPath("$.imported", is(2)));
    }

    @Test
    @Order(5)
    void step5VerifyCountriesImported() throws Exception {
        mockMvc.perform(get("/api/countries").param("pageSize", "-1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.items", hasSize(greaterThanOrEqualTo(2))))
            .andExpect(jsonPath("$.items[?(@.name == 'Germany')]").exists())
            .andExpect(jsonPath("$.items[?(@.name == 'USA')]").exists());
    }

    @Test
    @Order(6)
    void step6VerifyBranchesImported() throws Exception {
        mockMvc.perform(get("/api/branches").param("pageSize", "-1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.items", hasSize(greaterThanOrEqualTo(2))))
            .andExpect(jsonPath("$.items[?(@.name == 'Chemicals')]").exists())
            .andExpect(jsonPath("$.items[?(@.name == 'Technology')]").exists());
    }

    @Test
    @Order(7)
    void step7VerifyIsinNames() throws Exception {
        mockMvc.perform(get("/api/isin-names").param("pageSize", "-1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.items", hasSize(greaterThanOrEqualTo(2))))
            .andExpect(jsonPath("$.items[?(@.name == 'BASF SE')]").exists())
            .andExpect(jsonPath("$.items[?(@.name == 'Apple Inc.')]").exists());
    }

    @Test
    @Order(8)
    void step8VerifyTickerSymbols() throws Exception {
        mockMvc.perform(get("/api/ticker-symbols").param("pageSize", "-1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.items", hasSize(greaterThanOrEqualTo(2))))
            .andExpect(jsonPath("$.items[?(@.isin == 'DE000BASF111')].tickerSymbol", contains("BAS.DE")))
            .andExpect(jsonPath("$.items[?(@.isin == 'US0378331005')].tickerSymbol", contains("AAPL")));
    }

    @Test
    @Order(9)
    void step9VerifyStocksEmptyWithoutTransactions() throws Exception {
        mockMvc.perform(get("/api/stocks"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.items", is(empty())));
    }

    @Test
    @Order(10)
    void step10VerifyDashboardEmptyWithoutTransactions() throws Exception {
        mockMvc.perform(get("/api/dashboard"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.stockCount", is(0.0)))
            .andExpect(jsonPath("$.totalPortfolioValue", is(0.0)));
    }

    @Test
    @Order(11)
    void step11VerifyAnalyticsEmptyWithoutTransactions() throws Exception {
        mockMvc.perform(get("/api/analytics/countries"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.items", is(empty())));

        mockMvc.perform(get("/api/analytics/branches"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.items", is(empty())));
    }
}