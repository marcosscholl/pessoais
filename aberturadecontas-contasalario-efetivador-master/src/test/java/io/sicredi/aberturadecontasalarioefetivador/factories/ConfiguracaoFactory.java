package io.sicredi.aberturadecontasalarioefetivador.factories;

import io.sicredi.aberturadecontasalarioefetivador.entities.Configuracao;

public class ConfiguracaoFactory {


    public static Configuracao configuracaoValida(String porta) {
        return Configuracao.builder()
                .id(1L)
                .urlWebhook("http://localhost/webhook")
                .portaHttp(porta)
                .build();
    }

    public static Configuracao configuracaoComAutorizacaoRetorno(String porta) {
        Configuracao configuracao = configuracaoValida(porta);
        configuracao.setAutorizacaoRetorno("sWa1LnR4XqHrDDQp5WaRGrKXYDcyLBvk");
        return configuracao;
    }

    public static Configuracao configuracaoUrlInvalida(String porta) {
        Configuracao configuracao = configuracaoValida(porta);
        configuracao.setUrlWebhook("url_invalida");
        return configuracao;
    }

    public static Configuracao configuracaoPortaInvalida() {
        return configuracaoValida("porta_invalida");
    }

    public static Configuracao configuracaoValida() {
        return configuracaoValida("8080");
    }
}
