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
    void getIsinNames_returnsJsonArray() throws Exception {
        mockMvc.perform(get("/api/isin-names"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isArray());
    }

    @Test
    void getIsinNames_entriesHaveRequiredFields() throws Exception {
        // If data exists, each entry must have isin and name
        mockMvc.perform(get("/api/isin-names"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[*].isin").exists())
            .andExpect(jsonPath("$[*].name").exists());
    }

    @Test
    void exportIsinNames_csv_returnsFile() throws Exception {
        mockMvc.perform(get("/api/isin-names/export").param("format", "csv"))
            .andExpect(status().isOk())
            .andExpect(header().string("Content-Disposition", containsString("isin-names.csv")));
    }

    @Test
    void exportIsinNames_withSort_returnsFile() throws Exception {
        mockMvc.perform(get("/api/isin-names/export")
                .param("format", "csv")
                .param("sortField", "isin")
                .param("sortDir", "desc"))
            .andExpect(status().isOk())
            .andExpect(header().string("Content-Disposition", containsString("isin-names.csv")));
    }
}
