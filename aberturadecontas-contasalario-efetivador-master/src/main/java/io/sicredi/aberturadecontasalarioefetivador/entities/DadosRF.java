package io.sicredi.aberturadecontasalarioefetivador.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@Entity
@Table(name = "DADOS_RF")
public class DadosRF {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "ANO_OBITO")
    private String anoObito;
    @Column(name = "CODIGO_SITUACAO_CADASTRAL")
    private String codigoSituacaoCadastral;
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    @Column(name = "DATA_NASCIMENTO")
    private LocalDate dataNascimento;
    @Column(name = "DESC_SITUACAO_CADASTRAL")
    private String descSituacaoCadastral;
    private String nome;
    private String sexo;
    @Column(name = "SITUACAO_CADASTRAL")
    private String situacaoCadastral;

}
