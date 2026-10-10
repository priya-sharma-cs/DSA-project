package com.monotonic.demo.controller;

import com.monotonic.demo.dto.TemperatureRequest;
import com.monotonic.demo.dto.TemperatureResponse;
import com.monotonic.demo.entity.TemperatureAnalysis;
import com.monotonic.demo.service.TemperatureService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/temperatures")
public class TemperatureController {

    private final TemperatureService temperatureService;

    public TemperatureController(TemperatureService temperatureService) {
        this.temperatureService = temperatureService;
    }

    /**
     * Endpoint to analyze daily temperatures and calculate days until next warmer temperature.
     *
     * @param request TemperatureRequest containing array of daily temperatures
     * @return TemperatureResponse with waiting days and algorithm details
     */
    @PostMapping("/analyze")
    public ResponseEntity<TemperatureResponse> analyze(
            @Valid @RequestBody TemperatureRequest request) {

        if (request == null || request.getTemperatures() == null) {
            throw new IllegalArgumentException("Temperatures array must not be null");
        }

        TemperatureResponse response = temperatureService.analyze(request.getTemperatures());
        return ResponseEntity.ok(response);
    }

    /**
     * Endpoint to retrieve recent temperature analysis history from the database.
     *
     * @return List of recent TemperatureAnalysis entities
     */
    @GetMapping("/history")
    public ResponseEntity<List<TemperatureAnalysis>> getHistory() {
        List<TemperatureAnalysis> history = temperatureService.getHistory();
        return ResponseEntity.ok(history);
    }
}