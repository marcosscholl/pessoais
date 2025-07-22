package io.sicredi.aberturadecontasalarioefetivador.service;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Metrics;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MetricServiceTest {

    @InjectMocks
    private MetricService metricService;

    @Test
    @DisplayName("Deve enviar métrica")
    void deveEnviarMetrica(){
        MockedStatic<Metrics> metricsMockedStatic = mockStatic(Metrics.class);
        Counter counter = mock(Counter.class);
        metricsMockedStatic.when(() -> Metrics.counter("metrica")).thenReturn(counter);
        doNothing().when(counter).increment();

        metricService.incrementCounter("metrica");

        Mockito.verify(counter, timeout(1)).increment();
    }
}