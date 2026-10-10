package com.monotonic.demo.dto;

public class TemperatureResponse {

    private int[] temperatures;
    private int[] waitingDays;
    private String algorithm;
    private String timeComplexity;

    public TemperatureResponse() {
    }

    public TemperatureResponse(
            int[] temperatures,
            int[] waitingDays,
            String algorithm,
            String timeComplexity) {
        this.temperatures = temperatures;
        this.waitingDays = waitingDays;
        this.algorithm = algorithm;
        this.timeComplexity = timeComplexity;
    }

    public int[] getTemperatures() {
        return temperatures;
    }

    public void setTemperatures(int[] temperatures) {
        this.temperatures = temperatures;
    }

    public int[] getWaitingDays() {
        return waitingDays;
    }

    public void setWaitingDays(int[] waitingDays) {
        this.waitingDays = waitingDays;
    }

    public String getAlgorithm() {
        return algorithm;
    }

    public void setAlgorithm(String algorithm) {
        this.algorithm = algorithm;
    }

    public String getTimeComplexity() {
        return timeComplexity;
    }

    public void setTimeComplexity(String timeComplexity) {
        this.timeComplexity = timeComplexity;
    }
}