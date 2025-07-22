package io.sicredi.aberturadecontasalarioefetivador.entities;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@ToString
@Entity
@Table(name = "SOLICITACAO_CADASTRO_CONTA_SALARIO",
        indexes = {
                @Index(name = "idx_solicitacao_id_transacao", columnList = "id_transacao"),
                @Index(name = "idx_solicitacao_canal", columnList = "canal"),
                @Index(name = "idx_solicitacao_num_cooperativa", columnList = "num_cooperativa"),
                @Index(name = "idx_solicitacao_num_agencia", columnList = "num_agencia"),
                @Index(name = "idx_solicitacao_cod_convenio", columnList = "cod_convenio_fonte_pagadora")
        })
public class Solicitacao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "id_transacao", nullable = false, unique = true)
    private BigInteger idTransacao;

    @Column(nullable = false)
    private String canal;
    @Column(name = "NUM_COOPERATIVA", nullable = false)
    private String numCooperativa;

    @Column(name = "NUM_AGENCIA", nullable = false)
    private String numAgencia;

    @Column(name = "BRANCH_CODE", nullable = false)
    private String branchCode;

    @Column(name = "COD_CONVENIO_FONTE_PAGADORA", nullable = false)
    private String codConvenioFontePagadora;

    @Column(name = "CNPJ_FONTE_PAGADORA")
    private String cnpjFontePagadora;

    @Column(name = "CPF_FONTE_PAGADORA")
    private String cpfFontePagadora;

    @Builder.Default
    @OneToMany(mappedBy = "solicitacao", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @JsonManagedReference
    private List<Cadastro> cadastros = new ArrayList<>();

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "CONFIGURACAO_ID")
    private Configuracao configuracao;

    @Column(name = "DATA_CRIACAO", nullable = false, updatable = false)
    private LocalDateTime dataCriacao;

    @Column(name = "DATA_ATUALIZACAO")
    private LocalDateTime dataAtualizacao;

    @Enumerated(EnumType.STRING)
    private Status status;
    @Enumerated(EnumType.STRING)
    private Resultado resultado;
    private boolean critica;

    @Column(name = "WEBHOOK_HTTP_STATUS_CODIGO")
    private String webhookHttpStatusCodigo;

    @PrePersist
    protected void onCreate() {
        dataCriacao = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        dataAtualizacao = LocalDateTime.now();
    }
}