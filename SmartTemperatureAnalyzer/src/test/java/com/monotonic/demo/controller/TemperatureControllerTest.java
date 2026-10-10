package com.monotonic.demo.controller;

import com.monotonic.demo.dto.TemperatureResponse;
import com.monotonic.demo.entity.TemperatureAnalysis;
import com.monotonic.demo.service.TemperatureService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = {TemperatureController.class, GlobalExceptionHandler.class})
class TemperatureControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TemperatureService temperatureService;

    @Test
    @DisplayName("POST /api/temperatures/analyze returns 200 with valid input")
    void testAnalyzeValidInput() throws Exception {
        int[] input = {73, 74, 75};
        int[] waitingDays = {1, 1, 0};
        TemperatureResponse mockResponse = new TemperatureResponse(input, waitingDays, "Monotonic Stack", "O(n)");

        when(temperatureService.analyze(any(int[].class))).thenReturn(mockResponse);

        String jsonPayload = "{\"temperatures\": [73, 74, 75]}";

        mockMvc.perform(post("/api/temperatures/analyze")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonPayload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.algorithm").value("Monotonic Stack"))
                .andExpect(jsonPath("$.timeComplexity").value("O(n)"))
                .andExpect(jsonPath("$.temperatures", hasSize(3)))
                .andExpect(jsonPath("$.waitingDays", hasSize(3)))
                .andExpect(jsonPath("$.waitingDays[0]").value(1))
                .andExpect(jsonPath("$.waitingDays[1]").value(1))
                .andExpect(jsonPath("$.waitingDays[2]").value(0));
    }

    @Test
    @DisplayName("POST /api/temperatures/analyze returns 400 Bad Request when temperatures array is null")
    void testAnalyzeNullTemperatures() throws Exception {
        String jsonPayload = "{}";

        mockMvc.perform(post("/api/temperatures/analyze")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonPayload))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message", containsString("Temperatures array must not be null")));
    }

    @Test
    @DisplayName("POST /api/temperatures/analyze returns 400 Bad Request on malformed JSON payload")
    void testAnalyzeMalformedJson() throws Exception {
        String malformedJson = "{\"temperatures\": [\"not\", \"numbers\"]}";

        mockMvc.perform(post("/api/temperatures/analyze")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(malformedJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"));
    }

    @Test
    @DisplayName("GET /api/temperatures/history returns 200 and list of historical analyses")
    void testGetHistory() throws Exception {
        TemperatureAnalysis item = new TemperatureAnalysis("[30, 32]", "[1, 0]", "Monotonic Stack", "O(n)");
        item.setId(1L);

        when(temperatureService.getHistory()).thenReturn(List.of(item));

        mockMvc.perform(get("/api/temperatures/history"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].temperatures").value("[30, 32]"))
                .andExpect(jsonPath("$[0].waitingDays").value("[1, 0]"))
                .andExpect(jsonPath("$[0].algorithm").value("Monotonic Stack"));
    }
}
