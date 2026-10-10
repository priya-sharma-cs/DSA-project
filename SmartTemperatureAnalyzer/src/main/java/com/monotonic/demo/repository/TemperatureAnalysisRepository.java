package com.monotonic.demo.repository;

import com.monotonic.demo.entity.TemperatureAnalysis;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TemperatureAnalysisRepository
        extends JpaRepository<TemperatureAnalysis, Long> {

    List<TemperatureAnalysis> findAllByOrderByIdDesc();

    List<TemperatureAnalysis> findTop20ByOrderByIdDesc();
}