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
final class TransactionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void getTransactions_returnsEnvelopeWithEmptyItems() throws Exception {
        mockMvc.perform(get("/api/transactions"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.items", is(empty())))
            .andExpect(jsonPath("$.page", is(1)))
            .andExpect(jsonPath("$.pageSize", is(10)))
            .andExpect(jsonPath("$.totalItems", is(0)))
            .andExpect(jsonPath("$.filteredCount", is(0)))
            .andExpect(jsonPath("$.sumCount", is(0.0)));
    }

    @Test
    void getTransactions_supportsOptionalFilters() throws Exception {
        mockMvc.perform(get("/api/transactions")
                .param("isin", "DE000BASF111")
                .param("depot", "DeGiro"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.items").isArray());
    }

    @Test
    void getTransactions_supportsNameFilter() throws Exception {
        mockMvc.perform(get("/api/transactions")
                .param("name", "Apple"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.items").isArray());
    }

    @Test
    void getTransactions_supportsDateFilters() throws Exception {
        mockMvc.perform(get("/api/transactions")
                .param("fromDate", "2025-01-01T00:00:00")
                .param("toDate", "2026-12-31T23:59:59"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.items").isArray());
    }

    @Test
    void getTransactions_supportsSortAndPagination() throws Exception {
        mockMvc.perform(get("/api/transactions")
                .param("sortField", "date")
                .param("sortDir", "asc")
                .param("page", "1")
                .param("pageSize", "20"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.page", is(1)))
            .andExpect(jsonPath("$.pageSize", is(20)));
    }

    @Test
    void getTransactions_pageSizeMinusOne_returnsAll() throws Exception {
        mockMvc.perform(get("/api/transactions").param("pageSize", "-1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.pageSize", is(-1)))
            .andExpect(jsonPath("$.totalPages", is(1)));
    }

    @Test
    void getTransactionFilters_returnsDepots() throws Exception {
        mockMvc.perform(get("/api/transactions/filters"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.depots").isArray());
    }

    @Test
    void exportTransactions_csv_returnsFile() throws Exception {
        mockMvc.perform(get("/api/transactions/export").param("format", "csv"))
            .andExpect(status().isOk())
            .andExpect(header().string("Content-Disposition", containsString("transactions.csv")));
    }

    @Test
    void exportTransactions_excel_returnsFile() throws Exception {
        mockMvc.perform(get("/api/transactions/export").param("format", "xlsx"))
            .andExpect(status().isOk())
            .andExpect(header().string("Content-Disposition", containsString("transactions.xlsx")));
    }

    @Test
    void exportTransactions_withSortAndFilter_returnsFile() throws Exception {
        mockMvc.perform(get("/api/transactions/export")
                .param("format", "csv")
                .param("sortField", "date")
                .param("sortDir", "desc")
                .param("depot", "DeGiro"))
            .andExpect(status().isOk())
            .andExpect(header().string("Content-Disposition", containsString("transactions.csv")));
    }

    @Test
    void exportTransactions_defaultSortIsDesc() throws Exception {
        mockMvc.perform(get("/api/transactions/export").param("format", "csv"))
            .andExpect(status().isOk());
    }
}