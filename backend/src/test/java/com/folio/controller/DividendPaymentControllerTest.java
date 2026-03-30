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
final class DividendPaymentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void getDividendPayments_returnsEnvelopeWithEmptyItems() throws Exception {
        mockMvc.perform(get("/api/dividend-payments"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.items", is(empty())))
            .andExpect(jsonPath("$.page", is(1)))
            .andExpect(jsonPath("$.pageSize", is(10)))
            .andExpect(jsonPath("$.totalItems", is(0)))
            .andExpect(jsonPath("$.sumValue", is(0.0)));
    }

    @Test
    void getDividendPayments_supportsOptionalFilters() throws Exception {
        mockMvc.perform(get("/api/dividend-payments")
                .param("isin", "DE000BASF111")
                .param("depot", "DeGiro"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.items").isArray());
    }

    @Test
    void getDividendPayments_supportsNameFilter() throws Exception {
        mockMvc.perform(get("/api/dividend-payments")
                .param("name", "BASF"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.items").isArray());
    }

    @Test
    void getDividendPayments_supportsDateFilters() throws Exception {
        mockMvc.perform(get("/api/dividend-payments")
                .param("fromDate", "2025-01-01")
                .param("toDate", "2026-12-31"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.items").isArray());
    }

    @Test
    void getDividendPayments_supportsSortAndPagination() throws Exception {
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
    void getDividendPayments_pageSizeMinusOne_returnsAll() throws Exception {
        mockMvc.perform(get("/api/dividend-payments").param("pageSize", "-1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.pageSize", is(-1)))
            .andExpect(jsonPath("$.totalPages", is(1)));
    }

    @Test
    void getFilters_returnsDepots() throws Exception {
        mockMvc.perform(get("/api/dividend-payments/filters"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.depots").isArray());
    }

    @Test
    void exportDividendPayments_csv_returnsFile() throws Exception {
        mockMvc.perform(get("/api/dividend-payments/export").param("format", "csv"))
            .andExpect(status().isOk())
            .andExpect(header().string("Content-Disposition", containsString("dividend-payments.csv")));
    }

    @Test
    void exportDividendPayments_excel_returnsFile() throws Exception {
        mockMvc.perform(get("/api/dividend-payments/export").param("format", "xlsx"))
            .andExpect(status().isOk())
            .andExpect(header().string("Content-Disposition", containsString("dividend-payments.xlsx")));
    }

    @Test
    void exportDividendPayments_withSortAndFilter_returnsFile() throws Exception {
        mockMvc.perform(get("/api/dividend-payments/export")
                .param("format", "csv")
                .param("sortField", "timestamp")
                .param("sortDir", "desc")
                .param("depot", "DeGiro"))
            .andExpect(status().isOk())
            .andExpect(header().string("Content-Disposition", containsString("dividend-payments.csv")));
    }
}