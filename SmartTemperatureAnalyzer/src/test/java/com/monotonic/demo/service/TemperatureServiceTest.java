package com.monotonic.demo.service;

import com.monotonic.demo.dto.TemperatureResponse;
import com.monotonic.demo.entity.TemperatureAnalysis;
import com.monotonic.demo.repository.TemperatureAnalysisRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TemperatureServiceTest {

    @Mock
    private TemperatureAnalysisRepository repository;

    private TemperatureService service;

    @BeforeEach
    void setUp() {
        service = new TemperatureService(repository);
    }

    @Test
    @DisplayName("Algorithm handles standard LeetCode 739 Daily Temperatures example correctly")
    void testStandardDailyTemperatures() {
        int[] input = {73, 74, 75, 71, 69, 72, 76, 73};
        int[] expected = {1, 1, 4, 2, 1, 1, 0, 0};

        TemperatureResponse response = service.analyze(input);

        assertThat(response.getTemperatures()).containsExactly(input);
        assertThat(response.getWaitingDays()).containsExactly(expected);
        assertThat(response.getAlgorithm()).isEqualTo("Monotonic Stack");
        assertThat(response.getTimeComplexity()).isEqualTo("O(n)");
        verify(repository, times(1)).save(any(TemperatureAnalysis.class));
    }

    @Test
    @DisplayName("Algorithm handles empty input array correctly")
    void testEmptyInput() {
        int[] input = {};
        TemperatureResponse response = service.analyze(input);

        assertThat(response.getTemperatures()).isEmpty();
        assertThat(response.getWaitingDays()).isEmpty();
        assertThat(response.getAlgorithm()).isEqualTo("Monotonic Stack");
        assertThat(response.getTimeComplexity()).isEqualTo("O(n)");
    }

    @Test
    @DisplayName("Algorithm handles single element correctly")
    void testSingleElement() {
        int[] input = {73};
        int[] expected = {0};

        TemperatureResponse response = service.analyze(input);

        assertThat(response.getWaitingDays()).containsExactly(expected);
        verify(repository, times(1)).save(any(TemperatureAnalysis.class));
    }

    @Test
    @DisplayName("Algorithm handles strictly increasing values correctly")
    void testStrictlyIncreasingValues() {
        int[] input = {30, 40, 50, 60};
        int[] expected = {1, 1, 1, 0};

        TemperatureResponse response = service.analyze(input);

        assertThat(response.getWaitingDays()).containsExactly(expected);
    }

    @Test
    @DisplayName("Algorithm handles strictly decreasing values correctly")
    void testStrictlyDecreasingValues() {
        int[] input = {60, 50, 40, 30};
        int[] expected = {0, 0, 0, 0};

        TemperatureResponse response = service.analyze(input);

        assertThat(response.getWaitingDays()).containsExactly(expected);
    }

    @Test
    @DisplayName("Algorithm handles duplicate values correctly (equal temperatures do not count as warmer)")
    void testDuplicateValues() {
        int[] allSame = {30, 30, 30};
        int[] expectedAllSame = {0, 0, 0};
        TemperatureResponse res1 = service.analyze(allSame);
        assertThat(res1.getWaitingDays()).containsExactly(expectedAllSame);

        int[] duplicatesWithWarmer = {30, 30, 32, 32, 35};
        int[] expectedDuplicates = {2, 1, 2, 1, 0};
        TemperatureResponse res2 = service.analyze(duplicatesWithWarmer);
        assertThat(res2.getWaitingDays()).containsExactly(expectedDuplicates);
    }

    @Test
    @DisplayName("Algorithm handles negative and sub-zero temperatures correctly")
    void testNegativeTemperatures() {
        int[] input = {-5, -2, -10, -1};
        int[] expected = {1, 2, 1, 0};

        TemperatureResponse response = service.analyze(input);

        assertThat(response.getWaitingDays()).containsExactly(expected);
    }

    @Test
    @DisplayName("Null temperatures array throws IllegalArgumentException")
    void testNullInputThrowsException() {
        assertThatThrownBy(() -> service.analyze(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("cannot be null");

        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("Analysis entity is correctly persisted with string representations")
    void testPersistenceContent() {
        int[] input = {20, 25};
        service.analyze(input);

        ArgumentCaptor<TemperatureAnalysis> captor = ArgumentCaptor.forClass(TemperatureAnalysis.class);
        verify(repository).save(captor.capture());

        TemperatureAnalysis saved = captor.getValue();
        assertThat(saved.getTemperatures()).isEqualTo("[20, 25]");
        assertThat(saved.getWaitingDays()).isEqualTo("[1, 0]");
        assertThat(saved.getAlgorithm()).isEqualTo("Monotonic Stack");
        assertThat(saved.getTimeComplexity()).isEqualTo("O(n)");
    }

    @Test
    @DisplayName("getHistory retrieves top recent records from repository")
    void testGetHistory() {
        TemperatureAnalysis mockAnalysis = new TemperatureAnalysis("[30, 35]", "[1, 0]", "Monotonic Stack", "O(n)");
        when(repository.findTop20ByOrderByIdDesc()).thenReturn(List.of(mockAnalysis));

        List<TemperatureAnalysis> history = service.getHistory();

        assertThat(history).hasSize(1);
        assertThat(history.get(0).getTemperatures()).isEqualTo("[30, 35]");
        verify(repository, times(1)).findTop20ByOrderByIdDesc();
    }
}
