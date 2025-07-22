package io.sicredi.aberturadecontasalarioefetivador.factories;

import io.sicredi.aberturadecontasalarioefetivador.entities.Canal;

import java.time.LocalDateTime;

public class CanalFactory {

    public static final String CANAL_VALIDO = "CANAL_VALIDO";
    public static final String CANAL_INATIVO = "CANAL_INATIVO";

    public static Canal canalValido() {
        return Canal.builder()
                .id(1L)
                .codigo(1234L)
                .nome(CANAL_VALIDO)
                .documento("11111111111")
                .ativo(true)
                .dataCriacao(LocalDateTime.now())
                .dataAtualizacao(LocalDateTime.now())
                .build();
    }

    public static Canal canalInativo() {
        return Canal.builder()
                .id(4L)
                .codigo(3456L)
                .nome(CANAL_INATIVO)
                .ativo(false)
                .dataCriacao(LocalDateTime.now())
                .dataAtualizacao(LocalDateTime.now())
                .build();
    }
}
