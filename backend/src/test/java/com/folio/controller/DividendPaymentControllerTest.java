package com.folio.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
final class DividendPaymentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void getDividendPaymentsReturnsEnvelopeWithEmptyItems() throws Exception {
        mockMvc.perform(get("/api/dividend-payments"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.items", is(empty())))
            .andExpect(jsonPath("$.page", is(1)))
            .andExpect(jsonPath("$.pageSize", is(10)))
            .andExpect(jsonPath("$.totalItems", is(0)))
            .andExpect(jsonPath("$.sumValue", is(0.0)));
    }

    @Test
    void getDividendPaymentsSupportsOptionalFilters() throws Exception {
        mockMvc.perform(get("/api/dividend-payments")
                .param("isin", "DE000BASF111")
                .param("depot", "DeGiro"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.items").isArray());
    }

    @Test
    void getDividendPaymentsSupportsNameFilter() throws Exception {
        mockMvc.perform(get("/api/dividend-payments")
                .param("name", "BASF"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.items").isArray());
    }

    @Test
    void getDividendPaymentsSupportsDateFilters() throws Exception {
        mockMvc.perform(get("/api/dividend-payments")
                .param("fromDate", "2025-01-01")
                .param("toDate", "2026-12-31"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.items").isArray());
    }

    @Test
    void getDividendPaymentsSupportsSortAndPagination() throws Exception {
        mockMvc.perform(get("/api/dividend-payments")
                .param("sortField", "timestamp")
                .param("sortDir", "asc")
                .param("page", "1")
                .param("pageSize", "20"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.page", is(1)))
            .andExpect(jsonPath("$.pageSize", is(20)));
    }

    @Test
    void getDividendPaymentsPageSizeMinusOneReturnsAll() throws Exception {
        mockMvc.perform(get("/api/dividend-payments").param("pageSize", "-1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.pageSize", is(-1)))
            .andExpect(jsonPath("$.totalPages", is(1)));
    }

    @Test
    void getFiltersReturnsDepots() throws Exception {
        mockMvc.perform(get("/api/dividend-payments/filters"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.depots").isArray());
    }

    @Test
    void exportDividendPaymentsCsvReturnsFile() throws Exception {
        mockMvc.perform(get("/api/dividend-payments/export").param("format", "csv"))
            .andExpect(status().isOk())
            .andExpect(header().string("Content-Disposition", containsString("dividend-payments.csv")));
    }

    @Test
    void exportDividendPaymentsExcelReturnsFile() throws Exception {
        mockMvc.perform(get("/api/dividend-payments/export").param("format", "xlsx"))
            .andExpect(status().isOk())
            .andExpect(header().string("Content-Disposition", containsString("dividend-payments.xlsx")));
    }

    @Test
    void exportDividendPaymentsWithSortAndFilterReturnsFile() throws Exception {
        mockMvc.perform(get("/api/dividend-payments/export")
                .param("format", "csv")
                .param("sortField", "timestamp")
                .param("sortDir", "desc")
                .param("depot", "DeGiro"))
            .andExpect(status().isOk())
            .andExpect(header().string("Content-Disposition", containsString("dividend-payments.csv")));
    }
}