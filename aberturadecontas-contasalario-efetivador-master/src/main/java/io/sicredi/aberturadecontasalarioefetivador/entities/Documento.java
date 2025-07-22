package io.sicredi.aberturadecontasalarioefetivador.entities;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
@Entity
@Table(name = "DOCUMENTO")
public class Documento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "NUM_DOCUMENTO")
    private String numDocumento;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    @Column(name = "DATA_EMISSAO_DOC")
    private LocalDate dataEmissaoDoc;

    @Column(name = "NOME_ORGAO_EMISSOR_DOC")
    private String nomeOrgaoEmissorDoc;
    @Column(name = "SGL_UF_EMISSOR_DOC")
    private String sglUfEmissorDoc;
}
