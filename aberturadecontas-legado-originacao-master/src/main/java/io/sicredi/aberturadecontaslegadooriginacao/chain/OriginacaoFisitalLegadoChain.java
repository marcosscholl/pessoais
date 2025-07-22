package io.sicredi.aberturadecontaslegadooriginacao.chain;

import io.sicredi.aberturadecontaslegadooriginacao.chain.handler.*;
import io.sicredi.aberturadecontaslegadooriginacao.dto.AcquisitionEngineManagerItemsEventDTO;
import io.sicredi.aberturadecontaslegadooriginacao.entities.OriginacaoLegado;
import io.sicredi.aberturadecontaslegadooriginacao.exception.ProcessamentoOriginacaoFisitalLegadoException;
import io.sicredi.aberturadecontaslegadooriginacao.repository.OriginacaoLegadoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
@Slf4j
@RequiredArgsConstructor
public class OriginacaoFisitalLegadoChain {

    private final AcquisitionOrdersHandler acquisitionOrdersHandler;
    private final CustomerDataHandler customerDataHandler;
    private final RegisterDataHandler registerDataHandler;
    private final GestentHandler gestentHandler;
    private final CapitalAccountAcquisitionHandler capitalAccountAcquisitionHandler;
    private final AccountHandler accountHandler;
    private final InvestimentHandler investimentHandler;
    private final OriginacaoFisitalLegadoHandler originacaoFisitalLegadoHandler;
    private final OriginacaoLegadoRepository originacaoLegadoRepository;

    public void processaOriginacaoFisitalLegado(AcquisitionEngineManagerItemsEventDTO event) {
        log.info("[{}] - Iniciando processamento da cadeia de responsabilidades de processamento da originação fisital legado.", event.idPedido());
        try {
            configurarExecucaoChain();
            var originacaoLegado = originacaoLegadoRepository.findByIdPedido(event.idPedido()).orElseGet(() -> new OriginacaoLegado(event.idPedido()));
            if (Objects.isNull(originacaoLegado.getId())) {
                originacaoLegado = originacaoLegadoRepository.save(originacaoLegado);
            }
            var originacaoLegadoProcessado = acquisitionOrdersHandler.processar(event, originacaoLegado);

            if (!originacaoLegadoProcessado.temProdutoLegado()) {
                log.info("[{}] - Processamento da cadeia de responsabilidades de processamento da originação fisital legado descartado pois não tem nenhum item no pedido que seja legado.", event.idPedido());
                originacaoLegadoRepository.delete(originacaoLegado);
            }
            log.info("[{}] - Processamento da cadeia de responsabilidades de processamento da originação fisital legado finalizado com sucesso.", event.idPedido());

        } catch (Exception ex) {
            var processamentoOriginacaoFisitalLegadoException = new ProcessamentoOriginacaoFisitalLegadoException(ex);

            log.error("[{}] - Erro no processamento de pedido fisital-legado. error: {}",
                    event.idPedido(),
                    processamentoOriginacaoFisitalLegadoException.getMessage(),
                    processamentoOriginacaoFisitalLegadoException);

            throw processamentoOriginacaoFisitalLegadoException;
        }
    }

    private void configurarExecucaoChain() {
        acquisitionOrdersHandler.setProximo(customerDataHandler);
        customerDataHandler.setProximo(registerDataHandler);
        registerDataHandler.setProximo(gestentHandler);
        gestentHandler.setProximo(accountHandler);
        accountHandler.setProximo(investimentHandler);
        investimentHandler.setProximo(capitalAccountAcquisitionHandler);
        capitalAccountAcquisitionHandler.setProximo(originacaoFisitalLegadoHandler);
    }
}
