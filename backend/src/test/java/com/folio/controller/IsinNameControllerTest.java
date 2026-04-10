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
final class IsinNameControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void getIsinNamesReturnsPaginatedResponse() throws Exception {
        mockMvc.perform(get("/api/isin-names"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.items").isArray())
            .andExpect(jsonPath("$.page", is(1)))
            .andExpect(jsonPath("$.totalItems").isNumber());
    }

    @Test
    void getIsinNamesSupportsSortParams() throws Exception {
        mockMvc.perform(get("/api/isin-names")
                .param("sortField", "isin")
                .param("sortDir", "desc"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.items").isArray());
    }

    @Test
    void getIsinNamesSupportsPagination() throws Exception {
        mockMvc.perform(get("/api/isin-names")
                .param("page", "1")
                .param("pageSize", "20"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.pageSize", is(20)));
    }

    @Test
    void exportIsinNamesCsvReturnsFile() throws Exception {
        mockMvc.perform(get("/api/isin-names/export").param("format", "csv"))
            .andExpect(status().isOk())
            .andExpect(header().string("Content-Disposition", containsString("isin-names.csv")));
    }

    @Test
    void exportIsinNamesWithSortReturnsFile() throws Exception {
        mockMvc.perform(get("/api/isin-names/export")
                .param("format", "csv")
                .param("sortField", "isin")
                .param("sortDir", "desc"))
            .andExpect(status().isOk())
            .andExpect(header().string("Content-Disposition", containsString("isin-names.csv")));
    }
}