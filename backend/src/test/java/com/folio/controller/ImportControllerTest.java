package com.folio.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
final class ImportControllerTest {

    @Autowired
    private MockMvc mockMvc;

    // branches.csv format: ISIN;Name;Branch (no header row)
    @Test
    void importBranches_validCsv_returnsSuccess() throws Exception {
        String csv = "DE000BASF111;BASF SE;Chemicals\nUS0378331005;Apple Inc.;Technology";
        MockMultipartFile file = new MockMultipartFile("file", "branches.csv", "text/csv", csv.getBytes());

        mockMvc.perform(multipart("/api/import/branches").file(file))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success", is(true)))
            .andExpect(jsonPath("$.imported", is(2)));
    }

    // countries.csv format: ISIN;Name;Country (no header row)
    @Test
    void importCountries_validCsv_returnsSuccess() throws Exception {
        String csv = "DE000BASF111;BASF SE;Germany\nUS0378331005;Apple Inc.;USA";
        MockMultipartFile file = new MockMultipartFile("file", "countries.csv", "text/csv", csv.getBytes());

        mockMvc.perform(multipart("/api/import/countries").file(file))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success", is(true)))
            .andExpect(jsonPath("$.imported", is(2)));
    }

    // dividende.csv format: ISIN;Name;Currency;DividendPerShare (no header row)
    @Test
    void importDividends_validCsv_returnsSuccess() throws Exception {
        String csv = "DE000BASF111;BASF SE;EUR;3,40\nUS0378331005;Apple Inc.;USD;0,96";
        MockMultipartFile file = new MockMultipartFile("file", "dividende.csv", "text/csv", csv.getBytes());

        mockMvc.perform(multipart("/api/import/dividends").file(file))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success", is(true)))
            .andExpect(jsonPath("$.imported", is(2)));
    }

    // ticker_symbol.csv format: ISIN;TickerSymbol;Name (no header row)
    @Test
    void importTickerSymbols_validCsv_returnsSuccess() throws Exception {
        String csv = "DE000BASF111;BAS.DE;BASF SE\nUS0378331005;AAPL;Apple Inc.";
        MockMultipartFile file = new MockMultipartFile("file", "ticker_symbol.csv", "text/csv", csv.getBytes());

        mockMvc.perform(multipart("/api/import/ticker-symbols").file(file))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success", is(true)))
            .andExpect(jsonPath("$.imported", is(2)));
    }

    @Test
    void importBranches_emptyCsv_returnsZeroImported() throws Exception {
        String csv = "";
        MockMultipartFile file = new MockMultipartFile("file", "branches.csv", "text/csv", csv.getBytes());

        mockMvc.perform(multipart("/api/import/branches").file(file))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success", is(true)))
            .andExpect(jsonPath("$.imported", is(0)));
    }

    @Test
    void importBranches_invalidFormat_returnsSuccessWithZeroImported() throws Exception {
        // Only 2 fields per row, needs 3 — rows are silently skipped
        String csv = "DE000BASF111;Chemicals";
        MockMultipartFile file = new MockMultipartFile("file", "branches.csv", "text/csv", csv.getBytes());

        mockMvc.perform(multipart("/api/import/branches").file(file))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success", is(true)))
            .andExpect(jsonPath("$.imported", is(0)));
    }
}
