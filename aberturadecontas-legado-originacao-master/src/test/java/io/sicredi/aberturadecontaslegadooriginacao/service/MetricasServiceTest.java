package io.sicredi.aberturadecontaslegadooriginacao.service;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Metrics;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.*;

import org.mockito.MockedStatic;

import org.junit.jupiter.api.Test;

@ExtendWith(MockitoExtension.class)
class MetricServiceTest {

    @InjectMocks
    private MetricasService metricasService;

    @Test
    @DisplayName("Deve enviar métrica")
    void deveEnviarMetrica() {
        try (MockedStatic<Metrics> metricsMockedStatic = mockStatic(Metrics.class)) {
            Counter counter = mock(Counter.class);
            metricsMockedStatic.when(() -> Metrics.counter("metrica")).thenReturn(counter);
            doNothing().when(counter).increment();

            metricasService.incrementCounter("metrica");

            Mockito.verify(counter, timeout(1)).increment();
        }
    }
}
