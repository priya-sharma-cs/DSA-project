package com.monotonic.demo.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class TemperatureRequest {

    @NotNull(message = "Temperatures array must not be null")
    @Size(max = 10000, message = "Temperatures array must not exceed 10,000 elements")
    private int[] temperatures;

    public TemperatureRequest() {
    }

    public TemperatureRequest(int[] temperatures) {
        this.temperatures = temperatures;
    }

    public int[] getTemperatures() {
        return temperatures;
    }

    public void setTemperatures(int[] temperatures) {
        this.temperatures = temperatures;
    }
}