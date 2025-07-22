package io.sicredi.aberturadecontasalarioefetivador.service;

import br.com.sicredi.mua.cada.business.server.ejb.GetContaSalarioResponse;
import io.sicredi.aberturadecontasalarioefetivador.client.aberturacontacoexistenciaservice.AberturaContaCoexistenciaServiceClient;
import io.sicredi.aberturadecontasalarioefetivador.client.aberturacontacoexistenciaservice.dto.GetContaSalarioBuilder;
import io.sicredi.aberturadecontasalarioefetivador.dto.ConsultarInstituicaoFinanceiraResponseDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AberturaContaCoexistenciaService {

    private final AberturaContaCoexistenciaServiceClient client;

    public GetContaSalarioResponse consultarContaSalario(String branchCode, String conta, String cooperativa, String documento, long oidPessoa) {
        return client.consultarContaSalario(GetContaSalarioBuilder.builder()
                .branchCode(branchCode)
                .conta(conta)
                .cooperativa(cooperativa)
                .documento(documento)
                .oidPessoa(oidPessoa)
                .build());
    }

    public GetContaSalarioResponse consultarContaSalario(String conta, String cooperativa) {
        return client.consultarContaSalario(GetContaSalarioBuilder.builder()
                .conta(conta)
                .cooperativa(cooperativa)
                .oidPessoa(0L)
                .build());
    }

    public List<ConsultarInstituicaoFinanceiraResponseDTO> consultarInstituicoesFinanceiraAutorizadas() {
        try {
            var getInstituicaoFinanceiraResponse = client.consultarInstituicaoFinanceira();
            return getInstituicaoFinanceiraResponse.getReturn()
                    .stream()
                    .map(bancosAutorizadosDTO -> ConsultarInstituicaoFinanceiraResponseDTO.builder()
                            .codigo(bancosAutorizadosDTO.getCodigoBanco())
                            .nomeBanco(bancosAutorizadosDTO.getNomeBanco())
                            .build())
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Erro ao executar a consulta de Instituicoes Financeiras : ", e);
            return List.of();
        }


    }
}
