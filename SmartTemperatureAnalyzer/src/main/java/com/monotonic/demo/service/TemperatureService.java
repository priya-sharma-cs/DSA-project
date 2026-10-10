package com.monotonic.demo.service;

import com.monotonic.demo.dto.TemperatureResponse;
import com.monotonic.demo.entity.TemperatureAnalysis;
import com.monotonic.demo.repository.TemperatureAnalysisRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Deque;
import java.util.List;

@Service
public class TemperatureService {

    private final TemperatureAnalysisRepository repository;

    public TemperatureService(TemperatureAnalysisRepository repository) {
        this.repository = repository;
    }

    /**
     * Solves the Daily Temperatures problem using a Monotonic Decreasing Stack.
     * Computes the number of days until a warmer temperature for each day in O(n) time.
     *
     * @param temperatures array of daily temperatures
     * @return TemperatureResponse containing input, waiting days, algorithm, and complexity
     * @throws IllegalArgumentException if temperatures array is null
     */
    public TemperatureResponse analyze(int[] temperatures) {
        if (temperatures == null) {
            throw new IllegalArgumentException("Temperatures array cannot be null");
        }

        int n = temperatures.length;
        int[] answer = new int[n];

        if (n == 0) {
            return new TemperatureResponse(
                    new int[0],
                    new int[0],
                    "Monotonic Stack",
                    "O(n)"
            );
        }

        // Monotonic Stack storing indices of daily temperatures
        Deque<Integer> stack = new ArrayDeque<>();

        // Traverse backwards from last day to first day
        for (int i = n - 1; i >= 0; i--) {
            // Pop elements that are colder than or equal to current temperature
            while (!stack.isEmpty() && temperatures[stack.peek()] <= temperatures[i]) {
                stack.pop();
            }

            // Top of stack is the next warmer day
            if (!stack.isEmpty()) {
                answer[i] = stack.peek() - i;
            }

            // Push current day index onto stack
            stack.push(i);
        }

        // Persist analysis to database
        TemperatureAnalysis analysis = new TemperatureAnalysis(
                Arrays.toString(temperatures),
                Arrays.toString(answer),
                "Monotonic Stack",
                "O(n)"
        );
        repository.save(analysis);

        return new TemperatureResponse(
                temperatures,
                answer,
                "Monotonic Stack",
                "O(n)"
        );
    }

    /**
     * Retrieves recent temperature analysis history ordered from newest to oldest.
     *
     * @return list of recent TemperatureAnalysis records
     */
    public List<TemperatureAnalysis> getHistory() {
        return repository.findTop20ByOrderByIdDesc();
    }
}