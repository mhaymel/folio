package com.folio.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
final class QuoteControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void getSettingsReturnsDefaultSettings() throws Exception {
        mockMvc.perform(get("/api/quotes/settings"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.enabled", is(false)))
            .andExpect(jsonPath("$.intervalMinutes", is(60)))
            .andExpect(jsonPath("$.lastFetchAt").doesNotExist());
    }

    @Test
    void updateEnabledTogglesQuoteFetching() throws Exception {
        mockMvc.perform(put("/api/quotes/settings/enabled")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"enabled\": true}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.enabled", is(true)));

        // verify persisted
        mockMvc.perform(get("/api/quotes/settings"))
            .andExpect(jsonPath("$.enabled", is(true)));

        // toggle back
        mockMvc.perform(put("/api/quotes/settings/enabled")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"enabled\": false}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.enabled", is(false)));
    }

    @Test
    void updateIntervalChangesInterval() throws Exception {
        mockMvc.perform(put("/api/quotes/settings/interval")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"intervalMinutes\": 30}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.intervalMinutes", is(30)));

        // verify persisted
        mockMvc.perform(get("/api/quotes/settings"))
            .andExpect(jsonPath("$.intervalMinutes", is(30)));

        // restore default
        mockMvc.perform(put("/api/quotes/settings/interval")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"intervalMinutes\": 60}"))
            .andExpect(status().isOk());
    }

    @Test
    void updateIntervalRejectsZeroOrNegative() throws Exception {
        mockMvc.perform(put("/api/quotes/settings/interval")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"intervalMinutes\": 0}"))
            .andExpect(status().is4xxClientError());

        mockMvc.perform(put("/api/quotes/settings/interval")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"intervalMinutes\": -5}"))
            .andExpect(status().is4xxClientError());
    }

    @Test
    void updateEnabledRejectsMissingField() throws Exception {
        mockMvc.perform(put("/api/quotes/settings/enabled")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
            .andExpect(status().is4xxClientError());
    }

    @Test
    void triggerFetchReturnsCompletedStatus() throws Exception {
        // With no held ISINs, should return 0 fetched
        mockMvc.perform(post("/api/quotes/fetch"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status", is("completed")))
            .andExpect(jsonPath("$.fetchedCount", is(0)));
    }
}
