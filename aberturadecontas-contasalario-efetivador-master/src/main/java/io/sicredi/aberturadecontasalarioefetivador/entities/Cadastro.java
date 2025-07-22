package io.sicredi.aberturadecontasalarioefetivador.entities;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@ToString(exclude = "solicitacao")
@Entity
@Table(name = "CADASTRO")
public class Cadastro {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "OID_PESSOA")
    private Long oidPessoa;

    @Column(nullable = false)
    private String cpf;

    @Column(nullable = false)
    private String nome;

    @Column(name = "DATA_NASCIMENTO", nullable = false)
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate dataNascimento;

    @Column(name = "FLG_SEXO", nullable = false)
    private String flgSexo;
    private String email;
    private String telefone;

    @ManyToOne
    @JoinColumn(name = "solicitacao_id")
    @JsonBackReference
    private Solicitacao solicitacao;

    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinColumn(name = "documento_id")
    private Documento documento;

    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinColumn(name = "endereco_id")
    private Endereco endereco;

    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinColumn(name = "portabilidade_id")
    private Portabilidade portabilidade;

    @Embedded
    private Representante representante;

    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private DadosRF dadosRF;

    private boolean processado;
    private boolean efetivado;
    @Enumerated(EnumType.STRING)
    private Resultado situacao;
    private String conta;

    @ElementCollection(fetch = FetchType.EAGER)
    private Set<Critica> criticas = new HashSet<>();

}
