package io.sicredi.aberturadecontasalarioefetivador.entities;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@ToString
@Entity
@Table(name = "CANAL",
        uniqueConstraints = {
            @UniqueConstraint(columnNames = {"codigo"}),
            @UniqueConstraint(columnNames = {"nome"})},
        indexes = {
            @Index(name = "idx_canal_codigo", columnList = "codigo"),
            @Index(name = "idx_canal_nome", columnList = "nome")
})
public class Canal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private Long codigo;

    @Column(nullable = false, unique = true)
    private String nome;

    private String documento;

    @Column(nullable = false)
    private boolean ativo = true;

    @Column(name = "DATA_CRIACAO", nullable = false, updatable = false)
    private LocalDateTime dataCriacao;

    @Column(name = "DATA_ATUALIZACAO")
    private LocalDateTime dataAtualizacao;


    @PrePersist
    public void onCreate() {
        dataCriacao = LocalDateTime.now();
        dataAtualizacao = LocalDateTime.now();
    }

    @PreUpdate
    public void onUpdate() {
        dataAtualizacao = LocalDateTime.now();
    }
}
