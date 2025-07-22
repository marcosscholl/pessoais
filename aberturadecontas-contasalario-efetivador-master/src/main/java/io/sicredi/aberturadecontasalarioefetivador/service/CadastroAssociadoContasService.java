package io.sicredi.aberturadecontasalarioefetivador.service;

import io.sicredi.aberturadecontasalarioefetivador.client.cadastroassociadocontas.CadastroAssociadoContasClient;
import io.sicredi.aberturadecontasalarioefetivador.dto.CadastroAssociadoContasDTO;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
@Slf4j
public class CadastroAssociadoContasService {
    private static final String STATUS_CONTA = "ATIVA";
    private static final String CONTA_SALARIO = "CONTA_SALARIO";
    private static final String TITULAR = "TITULAR";
    private static final String CONTA_DIGITAL = "DIGITAL";

    private final CadastroAssociadoContasClient client;

    public List<CadastroAssociadoContasDTO> buscarContasAssociado(
            String documento,
            String cooperativa
    ) {
        try {
            return client.buscarContas(documento, cooperativa, STATUS_CONTA, CONTA_SALARIO, TITULAR);
        } catch (Exception e) {
            log.debug("[{}] Consulta de contas do associado sem correspondência : [{}]", documento, e);
            return List.of();
        }
    }

    public List<CadastroAssociadoContasDTO> buscarContasSalarioAssociado(String documento) {
        try {
            return client.buscarContasSalarioAssociado(documento, CONTA_SALARIO, TITULAR);
        } catch (Exception e) {
            log.debug("[{}] Consulta de contas do associado sem correspondência : [{}]", documento, e);
            return List.of();
        }
    }

    public List<CadastroAssociadoContasDTO> buscarContasAtivasAssociado(String documento) {
        try {
            return client.buscarContasAtivas(documento, STATUS_CONTA);
        } catch (Exception e) {
            log.debug("[{}] Consulta de contas ativas do associado sem correspondência : [{}]", documento, e);
            return List.of();
        }
    }

    public boolean isAssociadoDigital(String documento) {
        return buscarContasAtivasAssociado(documento)
                .stream()
                .anyMatch(conta -> CONTA_DIGITAL.equals(conta.originacao()));
    }
}
