package io.sicredi.aberturadecontasalarioefetivador.factories;

import br.com.sicredi.mua.cadastro.business.server.ws.v1.contasalarioservice.ContaSalario;
import br.com.sicredi.mua.cadastro.business.server.ws.v1.contasalarioservice.CriarContaSalario;
import io.sicredi.aberturadecontasalarioefetivador.entities.Cadastro;
import io.sicredi.aberturadecontasalarioefetivador.entities.TipoConta;
import io.sicredi.aberturadecontasalarioefetivador.utils.DateUtils;
import lombok.experimental.UtilityClass;
import org.apache.commons.lang3.StringUtils;

import javax.xml.datatype.XMLGregorianCalendar;
import java.time.LocalDate;
import java.util.Objects;

@UtilityClass
public class CriarContaSalarioFactory {

    private static final String UID_USUARIO = "APP_API_CTA_SALARIO";
    private static final String REGEX_ZEROS_PREFIXO = "^0+(?!$)";
    private static final String BANCO_SICREDI = "748";

    public static CriarContaSalario toCriarContaSalario(Cadastro cadastro, boolean isPessoaDigital) {
        return criarContaSalarioModalidadeSaque(cadastro, isPessoaDigital);
    }

    private static CriarContaSalario criarContaSalarioModalidadeSaque(Cadastro cadastro, boolean isPessoaDigital) {
        CriarContaSalario criarContaSalario = new CriarContaSalario();
        ContaSalario contaSalario = new ContaSalario();
        contaSalario.setNumCooperativa(cadastro.getSolicitacao().getNumCooperativa());
        contaSalario.setNumAgencia(cadastro.getSolicitacao().getNumAgencia());
        contaSalario.setNumCPF(cadastro.getCpf());
        contaSalario.setNomTitular(extrairNome(cadastro));
        contaSalario.setDatNascimento(extrairDataNascimento(cadastro));
        contaSalario.setFlgSexo(extrairFlgSexo(cadastro));
        contaSalario.setCodConvenioFontePagadora(cadastro.getSolicitacao().getCodConvenioFontePagadora());
        if (!isPessoaDigital) criarContaSalarioComDocumentoIdentificacao(cadastro, contaSalario);
        if (!isPessoaDigital) criarContaSalarioComEndereco(cadastro, contaSalario);
        criarContaSalarioComPortabilidade(cadastro, contaSalario, isPessoaDigital);
        criarContaSalarioComRepresentante(cadastro, contaSalario);
        contaSalario.setUidUsuario(UID_USUARIO);
        criarContaSalario.setContaSalario(contaSalario);
        return criarContaSalario;
    }

    private static void criarContaSalarioComRepresentante(Cadastro cadastro, ContaSalario contaSalario) {
        if(LocalDate.now().minusYears(18).isBefore(cadastro.getDadosRF().getDataNascimento()) &&
                Objects.nonNull(cadastro.getRepresentante())){
            contaSalario.setNomRepresentante(cadastro.getRepresentante().getNome());
            contaSalario.setNumCpfRepresentante(cadastro.getRepresentante().getCpf());
        }
    }

    private static void criarContaSalarioComPortabilidade(Cadastro cadastro, ContaSalario contaSalario, boolean isPessoaDigital) {
        if(Objects.nonNull(cadastro.getPortabilidade())){
            contaSalario.setCodBancoDestino(cadastro.getPortabilidade().getCodBancoDestino());
            contaSalario.setNumAgDestino(cadastro.getPortabilidade().getNumAgDestino());
            if (isPessoaDigital
                    && BANCO_SICREDI.equals(cadastro.getPortabilidade().getCodBancoDestino())
                    && TipoConta.CONTA_DIGITAL_SICREDI_OU_CONTA_PAGAMENTO_OUTRA_IF.equals(cadastro.getPortabilidade().getTipoConta())
                        && cadastro.getPortabilidade().getNumContaDestino().startsWith("0")) {
                contaSalario.setNumContaDestino(removeLeadingZeros(cadastro.getPortabilidade().getNumContaDestino()));
            } else {
                contaSalario.setNumContaDestino(cadastro.getPortabilidade().getNumContaDestino());

            }
            contaSalario.setTpoConta(cadastro.getPortabilidade().getTipoConta().getCodigo());
        }
    }

    public static String removeLeadingZeros(String number) {
        return number.replaceFirst(REGEX_ZEROS_PREFIXO, "");
    }

    private static void criarContaSalarioComDocumentoIdentificacao(Cadastro cadastro, ContaSalario contaSalario) {
        if (Objects.nonNull(cadastro.getDocumento())) {
            contaSalario.setNumDocumento(cadastro.getDocumento().getNumDocumento());
            XMLGregorianCalendar dataEmissaoDoc = DateUtils.converterLocalDateParaXMLGregorian(LocalDate.of(cadastro.getDocumento().getDataEmissaoDoc().getYear(), cadastro.getDocumento().getDataEmissaoDoc().getMonth(), cadastro.getDocumento().getDataEmissaoDoc().getDayOfMonth()));
            contaSalario.setDatEmissaoDoc(dataEmissaoDoc);
            contaSalario.setNomOrgaoEmissorDoc(cadastro.getDocumento().getNomeOrgaoEmissorDoc());
            contaSalario.setSglUfEmissorDoc(cadastro.getDocumento().getSglUfEmissorDoc());
        }
    }

    private static void criarContaSalarioComEndereco(Cadastro cadastro, ContaSalario contaSalario) {
        if (Objects.nonNull(cadastro.getEndereco())) {
            contaSalario.setTpoLogradouro(cadastro.getEndereco().getTipoLogradouro());
            contaSalario.setNomLogradouro(cadastro.getEndereco().getNomeLogradouro());
            contaSalario.setNumEndereco(cadastro.getEndereco().getNumEndereco());
            if (Objects.nonNull(cadastro.getEndereco().getTxtComplemento()) && (!cadastro.getEndereco().getTxtComplemento().isBlank())) {
                contaSalario.setTxtComplemento(cadastro.getEndereco().getTxtComplemento());
            } else {
                contaSalario.setTxtComplemento(null);
            }
            contaSalario.setNomBairro(cadastro.getEndereco().getNomeBairro());
            contaSalario.setNumCep(Long.valueOf(cadastro.getEndereco().getNumCep()));
            contaSalario.setNomCidade(cadastro.getEndereco().getNomeCidade());
            contaSalario.setSglUf(cadastro.getEndereco().getSglUf());
        }
    }

    private static String extrairNome(Cadastro cadastro) {
        if(Objects.nonNull(cadastro.getDadosRF()) && StringUtils.isNotBlank(cadastro.getDadosRF().getNome())){
            return cadastro.getDadosRF().getNome();
        }
        return cadastro.getNome();
    }

    private static XMLGregorianCalendar extrairDataNascimento(Cadastro cadastro) {
        if(Objects.nonNull(cadastro.getDadosRF()) && Objects.nonNull(cadastro.getDadosRF().getDataNascimento())){
            return DateUtils.converterLocalDateParaXMLGregorian(cadastro.getDadosRF().getDataNascimento());
        }
        return DateUtils.converterLocalDateParaXMLGregorian(cadastro.getDataNascimento());
    }

    private static String extrairFlgSexo(Cadastro cadastro) {
        if(Objects.nonNull(cadastro.getDadosRF()) && Objects.nonNull(cadastro.getDadosRF().getDataNascimento())){
            return cadastro.getDadosRF().getSexo().substring(0,1);
        }
        return cadastro.getFlgSexo();
    }
}
