package io.sicredi.aberturadecontasalarioefetivador.service;

import br.com.sicredi.servicos.cadastros.enterprise.ejb.cadastro.ConsultarDadosAssociadoResponse;
import io.sicredi.aberturadecontasalarioefetivador.client.cadastroassociadoservice.CadastroAssociadoServiceClient;
import io.sicredi.aberturadecontasalarioefetivador.client.cadastroassociadoservice.dto.ConsultarDadosAssociadoBuilder;
import io.sicredi.aberturadecontasalarioefetivador.entities.Cadastro;
import io.sicredi.aberturadecontasalarioefetivador.exceptions.WebserviceException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
@Service
public class CadastroAssociadoService {

    private final CadastroAssociadoServiceClient client;

    public ConsultarDadosAssociadoResponse consultarDadosAssociado(String cpf) {

        ConsultarDadosAssociadoBuilder consultarDadosAssociado = ConsultarDadosAssociadoBuilder.builder()
                .cpf(cpf)
                .build();
        try {
            return client.consultarDadosAssociado(consultarDadosAssociado);
        } catch (Exception e) {
            log.error("Erro ao acessar serviço CadastroAssociadoService para o cadastro {}", cpf, e);
            throw new WebserviceException("Erro ao acessar serviço CadastroAssociadoService: " + e.getMessage(), e);
        }

    }

    public Optional<Long> consultarCadastroOidPessoa(Cadastro cadastro) {
        try {
            var response = consultarDadosAssociado(cadastro.getCpf());
            var elementos = response.getOutConsultarDadosAssociado().getElementos();

            return (!CollectionUtils.isEmpty(elementos))
                    ? Optional.of(elementos.getFirst().getOidPessoa())
                    : Optional.empty();
        } catch (Exception e) {
            log.error(String.format("Erro ao consultar os dados do CPF %s : %s", cadastro.getCpf(), e.getMessage()), e);
            return Optional.empty();
        }
    }
}
