package com.monotonic.demo.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "temperature_analysis")
public class TemperatureAnalysis {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(columnDefinition = "TEXT")
    private String temperatures;

    @Column(columnDefinition = "TEXT")
    private String waitingDays;

    private String algorithm;

    private String timeComplexity;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    public TemperatureAnalysis() {
    }

    public TemperatureAnalysis(
            String temperatures,
            String waitingDays,
            String algorithm,
            String timeComplexity) {
        this.temperatures = temperatures;
        this.waitingDays = waitingDays;
        this.algorithm = algorithm;
        this.timeComplexity = timeComplexity;
        this.createdAt = LocalDateTime.now();
    }

    @PrePersist
    protected void onCreate() {
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTemperatures() {
        return temperatures;
    }

    public void setTemperatures(String temperatures) {
        this.temperatures = temperatures;
    }

    public String getWaitingDays() {
        return waitingDays;
    }

    public void setWaitingDays(String waitingDays) {
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

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}