package io.sicredi.aberturadecontasalarioefetivador.service;

import br.com.sicredi.contasalario.ejb.ConsultarSaldoContaSalarioResponse;
import io.sicredi.aberturadecontasalarioefetivador.client.contasalariocontasservice.ContaSalarioContasServiceClient;
import io.sicredi.aberturadecontasalarioefetivador.exceptions.WebserviceException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Service
public class ContaSalarioContasService {

    private final ContaSalarioContasServiceClient contaSalarioContasServiceClient;

    public ConsultarSaldoContaSalarioResponse consultarContaSalario(String cooperativa, String numeroConta) {
        try {
            return contaSalarioContasServiceClient.consultarContaSalario(cooperativa, numeroConta);
        } catch (Exception e){
            log.error("Erro ao acessar serviço ContaSalarioContasService para a coop {} : {}", cooperativa, e);
            throw new WebserviceException("Erro ao acessar serviço ContaSalarioContasService: " + e.getMessage(), e);
        }
    }
}
