package io.sicredi.aberturadecontaslegadooriginacao.service;

import io.micrometer.core.instrument.Metrics;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class MetricasService {

    public void incrementCounter(String name, String... tags ){
        Metrics.counter(name, tags).increment();
    }
}
