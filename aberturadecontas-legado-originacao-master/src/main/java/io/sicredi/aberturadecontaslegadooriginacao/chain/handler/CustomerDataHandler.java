package io.sicredi.aberturadecontaslegadooriginacao.chain.handler;

import io.sicredi.aberturadecontaslegadooriginacao.chain.AbstractHandler;
import io.sicredi.aberturadecontaslegadooriginacao.client.CustomerDataClient;
import io.sicredi.aberturadecontaslegadooriginacao.dto.AcquisitionEngineManagerItemsEventDTO;
import io.sicredi.aberturadecontaslegadooriginacao.dto.CustomerDataDTO;
import io.sicredi.aberturadecontaslegadooriginacao.entities.Cadastro;
import io.sicredi.aberturadecontaslegadooriginacao.entities.Critica;
import io.sicredi.aberturadecontaslegadooriginacao.entities.OriginacaoLegado;
import io.sicredi.aberturadecontaslegadooriginacao.mapper.OriginacaoLegadoMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Objects;

import static io.sicredi.aberturadecontaslegadooriginacao.entities.EtapaProcessoOriginacao.*;

@Component
@Slf4j
@RequiredArgsConstructor
public class CustomerDataHandler extends AbstractHandler {

    private final CustomerDataClient customerDataClient;
    private final OriginacaoLegadoMapper originacaoLegadoMapper;

    @Override
    public OriginacaoLegado processarProximo(final AcquisitionEngineManagerItemsEventDTO event, final OriginacaoLegado originacaoLegado) {
        log.info("[{}] - Iniciando busca dos cadastros do pedido.", originacaoLegado.getIdPedido());
        OriginacaoLegado novaOriginacaoLegado = new OriginacaoLegado();
        try {
            if (!originacaoLegado.temCritica(CUSTOMER_DATA) && originacaoLegado.customerDataProcessado()) {
                log.info("[{}] - Consulta dos dados do cliente já processados com sucesso.", originacaoLegado.getIdPedido());
                return originacaoLegado;
            }

            var dadosCadastro = originacaoLegado.getCadastros()
                    .stream()
                    .map(Cadastro::getId)
                    .distinct()
                    .map(idCadastro -> buscarDadosCliente(idCadastro, originacaoLegado))
                    .toList();

            int dadosClienteConsultados = (int) dadosCadastro.stream()
                    .filter(Objects::nonNull)
                    .count();

            originacaoLegadoMapper.merge(originacaoLegado, novaOriginacaoLegado);
            novaOriginacaoLegado.setCadastros(null);
            originacaoLegadoMapper.mapListaCustomerDataDTOParaOriginacaoLegado(dadosCadastro, novaOriginacaoLegado);

            if (dadosClienteConsultados != dadosCadastro.size()) {
                novaOriginacaoLegado.adicionarCritica(new Critica(CUSTOMER_DATA, "Erro ao processar os dados do customer-data para o pedido [ " + originacaoLegado.getIdPedido() + " ]"));
            }

            if(novaOriginacaoLegado.customerDataProcessado()){
                novaOriginacaoLegado.removerCritica(CUSTOMER_DATA);
            }

            log.info("[{}] - Dados do cliente recuperado e processado com sucesso. {}", originacaoLegado.getIdPedido(), novaOriginacaoLegado);

        } catch (Exception e) {
            log.error("[{}] - Erro ao processar os dados de de cadastro de cliente: {}", originacaoLegado.getIdPedido(), e.getMessage());
            originacaoLegadoMapper.merge(originacaoLegado, novaOriginacaoLegado);
            novaOriginacaoLegado.adicionarCritica(new Critica(CUSTOMER_DATA, "Erro ao processar os dados de cadastro do pedido [ " + originacaoLegado.getIdPedido() + " ]."));
        }

        return novaOriginacaoLegado;
    }

    private CustomerDataDTO buscarDadosCliente(String idCadastro, OriginacaoLegado originacaoLegado) {
        try {
            var customerData = customerDataClient.buscarDadosCliente(idCadastro);
            log.debug("[{}] - Dados de cadastro do cliente retornados com sucesso do customer-data. {}", idCadastro, customerData);
            return customerData;
        } catch (Exception e) {
            log.error("[{}] - Erro ao buscar dados do cadastro no serviço customer-data. error: {}", idCadastro, e.getMessage());
            originacaoLegado.adicionarCritica(new Critica(CUSTOMER_DATA, "Erro ao buscar dados do cadastro [ " + idCadastro + " ] no serviço customer-data"));
            return null;
        }
    }
}

