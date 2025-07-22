package io.sicredi.aberturadecontasalarioefetivador.dto;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "criarContaSalarioResponse", namespace = "http://www.sicredi.com.br/mua/cadastro/business/server/ws/v1/ContaSalarioService")
public class CriarContaSalarioResponseCustomizadoDTO {

    @XmlElement(name = "codConvenioFontePagadora")
    protected String codConvenioFontePagadora;

    @XmlElement(name = "numCPF")
    protected String numCPF;

    @XmlElement(name = "numCooperativa")
    protected String numCooperativa;

    @XmlElement(name = "numAgencia")
    protected String numAgencia;

    @XmlElement(name = "numConta")
    protected String numConta;

    @XmlElement(name = "codStatus")
    protected String codStatus;

    @XmlElement(name = "desStatus")
    protected String desStatus;
}
