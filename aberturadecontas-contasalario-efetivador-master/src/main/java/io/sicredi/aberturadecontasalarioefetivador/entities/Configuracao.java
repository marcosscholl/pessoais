package io.sicredi.aberturadecontasalarioefetivador.entities;

import jakarta.persistence.*;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@ToString
@Table(name = "CONFIGURACAO")
public class Configuracao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "URL_WEBHOOK")
    private String urlWebhook;
    @Column(name = "PORTA_HTTP")
    private String portaHttp;

    @Column(name = "AUTORIZACAO_RETORNO")
    private String autorizacaoRetorno;
}