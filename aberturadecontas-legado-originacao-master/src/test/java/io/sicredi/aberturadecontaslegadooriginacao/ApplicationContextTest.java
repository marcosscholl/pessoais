package io.sicredi.aberturadecontaslegadooriginacao;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
class ApplicationContextTest {

    @Autowired
    private MockMvc restClient;

    @Test
    @DisplayName("Deve retornar UP")
    void deveRetornarUp() throws Exception {
        this.restClient.perform(get("/actuator/health"))
                .andExpect(status().isOk())
                .andExpect(content().string("{\"status\":\"UP\"}"));
    }

    @Test
    @DisplayName("Deve executar a aplicação")
    void deveExecutarAplicacao() {
        Assertions.assertThatNoException().isThrownBy(() -> Application.main(new String[]{"--spring.profiles.active=test"}));
    }

}