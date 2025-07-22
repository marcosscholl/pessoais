package io.sicredi.aberturadecontasalarioefetivador.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
@ConfigurationProperties(prefix = "efetivador.oracle.sql")
@Getter
@Setter
public class DatabaseProcessorSQLPropertiesConfig {

    private boolean criartabelasEnabled;
    private boolean atualizarporscriptEnabled;
    private Comandos comandos;

    @Getter
    @Setter
    public static class Comandos {
        private int quantidade;
        private List<String> querys;
    }
}
