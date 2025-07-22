package io.sicredi.aberturadecontasalarioefetivador.factories;

import br.com.sicredi.mua.cada.business.server.ejb.*;
import io.sicredi.aberturadecontasalarioefetivador.client.aberturacontacoexistenciaservice.dto.GetContaSalarioBuilder;
import io.sicredi.aberturadecontasalarioefetivador.dto.ConsultarInstituicaoFinanceiraResponseDTO;
import io.sicredi.aberturadecontasalarioefetivador.entities.Cadastro;
import io.sicredi.aberturadecontasalarioefetivador.utils.DateUtils;

import javax.xml.datatype.XMLGregorianCalendar;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class AberturaContaCoexistenciaServiceFactory {

    private static final String BRANCH_CODE = "3AO";
    private static final String CONTA = "90367-7";
    private static final String CONTA_ENCERRADA = "90367-6";
    private static final String COOPERATIVA = "0810";

    public static GetContaSalario consultaContaSalario(Cadastro cadastro) {
        return GetContaSalarioBuilder.builder()
                .branchCode(BRANCH_CODE)
                .conta(CONTA)
                .cooperativa(COOPERATIVA)
                .documento(cadastro.getCpf())
                .oidPessoa(cadastro.getOidPessoa())
                .build();
    }
    public static GetContaSalarioResponse consultaContaSalarioEncontrada() {
        ContaSalario contaSalario = new ContaSalario();

        contaSalario.setAgenciaConvenio(COOPERATIVA);
        contaSalario.setCnpjEmpresaConvenio("18523110000101");
        contaSalario.setCodEmpresaConvenio(BRANCH_CODE);
        contaSalario.setCodigoISPB("04814563");
        contaSalario.setConta(CONTA);
        contaSalario.setContaConvenio("801606");
        contaSalario.setCpf("20643481400");
        contaSalario.setDestinoCredCS("A");
        contaSalario.setEmbossamento("SCHOLL CONTA SALARIO");
        contaSalario.setEndereco("- - /");
        contaSalario.setEnderecoCorrespondencia("- - /");
        contaSalario.setNomeEmpresaConvenio("ELLETRISA ENGENHARI");
        contaSalario.setNucleo("586");
        contaSalario.setPosto("17");
        contaSalario.setTarifacao("S");
        contaSalario.setTipoContaDestino("");
        contaSalario.setTitular("SCHOLL CONTA SALARIO");

        GetContaSalarioResponse response = new GetContaSalarioResponse();
        response.setReturn(contaSalario);

        return response;
    }

    public static GetContaSalarioResponse getContaSalarioResponse() {
        ContaSalario contaSalario = new ContaSalario();
        contaSalario.setAgenciaConvenio("0810");
        contaSalario.setAgenciaDestino("1590");
        contaSalario.setBancoDestino("336");
        contaSalario.setCnpjEmpresaConvenio("18523110000101");
        contaSalario.setCodEmpresaConvenio("3AO");
        contaSalario.setCodigoISPB("00000000");
        contaSalario.setConta(CONTA);
        contaSalario.setContaConvenio("801606");
        contaSalario.setContaDestino("502553");
        contaSalario.setCpf("36185900084");
        contaSalario.setDataAbertura(DateUtils.converterLocalDateParaXMLGregorian(LocalDate.of(2024,11, 7)));
        contaSalario.setDestinoCredCS("C");
        contaSalario.setEmbossamento("VALOR DEFAULT MOCK");
        contaSalario.setNomeEmpresaConvenio("ELLETRISA ENGENHARI");
        contaSalario.setPosto("04");
        contaSalario.setTarifacao("S");
        contaSalario.setTipoContaDestino("01");
        contaSalario.setTitular("VALOR DEFAULT MOCK");

        GetContaSalarioResponse response = new GetContaSalarioResponse();
        response.setReturn(contaSalario);
        return response;
    }

    public static GetContaSalarioResponse getContaSalarioEncerradaResponse() {
        GetContaSalarioResponse contaSalarioResponse = getContaSalarioResponse();
        contaSalarioResponse.getReturn().setConta(CONTA_ENCERRADA);
        contaSalarioResponse.getReturn().setDataEncerramento(DateUtils.converterLocalDateParaXMLGregorian(LocalDate.of(2024,10, 7)));
        return contaSalarioResponse;
    }


    public static GetInstituicaoFinanceiraResponse criarGetInstituicaoFinanceiraResponse() {
        GetInstituicaoFinanceiraResponse response = new GetInstituicaoFinanceiraResponse();

        List<BancosAutorizadosDTO> bancosAutorizadosList = new ArrayList<>();

        BancosAutorizadosDTO banco1 = new BancosAutorizadosDTO();
        banco1.setCodigoBanco("299");
        banco1.setIspbBanco("04814563");
        banco1.setNomeBanco("BCO AFINZ S.A. - BM");

        BancosAutorizadosDTO banco2 = new BancosAutorizadosDTO();
        banco2.setCodigoBanco("121");
        banco2.setIspbBanco("10664513");
        banco2.setNomeBanco("BCO AGIBANK S.A.");

        bancosAutorizadosList.add(banco1);
        bancosAutorizadosList.add(banco2);

        response.getReturn().addAll(bancosAutorizadosList);

        return response;
    }

    public static List<ConsultarInstituicaoFinanceiraResponseDTO> criarListaInstituicoesFinanceirasDTO() {
        List<ConsultarInstituicaoFinanceiraResponseDTO> lista = new ArrayList<>();

        ConsultarInstituicaoFinanceiraResponseDTO instituicao1 = ConsultarInstituicaoFinanceiraResponseDTO.builder()
                .codigo("299")
                .nomeBanco("BCO AFINZ S.A. - BM")
                .build();

        ConsultarInstituicaoFinanceiraResponseDTO instituicao2 = ConsultarInstituicaoFinanceiraResponseDTO.builder()
                .codigo("121")
                .nomeBanco("BCO AGIBANK S.A.")
                .build();

        lista.add(instituicao1);
        lista.add(instituicao2);

        return lista;
    }
}
