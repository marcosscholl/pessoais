package io.sicredi.aberturadecontaslegadooriginacao.chain;

import feign.RetryableException;
import io.sicredi.aberturadecontaslegadooriginacao.chain.handler.CustomerDataHandler;
import io.sicredi.aberturadecontaslegadooriginacao.client.CustomerDataClient;
import io.sicredi.aberturadecontaslegadooriginacao.dto.AcquisitionEngineManagerItemsEventDTO;
import io.sicredi.aberturadecontaslegadooriginacao.dto.CadastroDTO;
import io.sicredi.aberturadecontaslegadooriginacao.dto.CustomerDataDTO;
import io.sicredi.aberturadecontaslegadooriginacao.entities.Critica;
import io.sicredi.aberturadecontaslegadooriginacao.entities.EtapaProcessoOriginacao;
import io.sicredi.aberturadecontaslegadooriginacao.entities.OriginacaoLegado;
import io.sicredi.aberturadecontaslegadooriginacao.mapper.OriginacaoLegadoMapper;
import org.instancio.Instancio;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.mapping.MappingException;

import java.util.List;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CustomerDataHandlerTest {

    @Mock
    CustomerDataClient customerDataClient;
    @InjectMocks
    CustomerDataHandler customerDataHandler;
    @Mock
    private OriginacaoLegadoMapper originacaoLegadoMapper;


    @Test
    @DisplayName("Deve buscar dados dos cadastros e processar com sucesso")
    void deveBuscarDadosDosCadastrosEProcessarEventoComSucesso() {
        var acquisitionEngineManagerItemsEventDTO = Instancio.of(AcquisitionEngineManagerItemsEventDTO.class).create();
        var originacaoLegado = Instancio.of(OriginacaoLegado.class).create();
        originacaoLegado.adicionarCritica(new Critica(EtapaProcessoOriginacao.CUSTOMER_DATA, "Erro"));

        acquisitionEngineManagerItemsEventDTO.item().cadastros().forEach(cadastroEsperado ->
                when(customerDataClient.buscarDadosCliente(cadastroEsperado.idCadastro()))
                        .thenReturn(Instancio.of(CustomerDataDTO.class).create())
        );

        doNothing().when(originacaoLegadoMapper).mapListaCustomerDataDTOParaOriginacaoLegado(anyList(), any(OriginacaoLegado.class));

        var novaOriginacaoLegado = customerDataHandler.processarProximo(acquisitionEngineManagerItemsEventDTO, originacaoLegado);

        originacaoLegado.getCadastros().forEach(cadastroEsperado ->
                verify(customerDataClient, times(1))
                        .buscarDadosCliente(cadastroEsperado.getId()));

        verify(originacaoLegadoMapper, times(1)).mapListaCustomerDataDTOParaOriginacaoLegado(anyList(), eq(novaOriginacaoLegado));
        verify(originacaoLegadoMapper, times(1)).merge(eq(originacaoLegado), any(OriginacaoLegado.class));
    }

    @Test
    @DisplayName("Deve buscar dados dos cadastros e falhar o processamento pois algum documento não foi encontrado no serviço customer-data")
    void deveBuscarDadosDosCadastrosEFalharProcessamentoDocumentoNaoEncontradoEmCustomerData() {
        var acquisitionEngineManagerItemsEventDTO = Instancio.of(AcquisitionEngineManagerItemsEventDTO.class).create();
        var originacaoLegado = Instancio.of(OriginacaoLegado.class).create();
        originacaoLegado.adicionarCritica(new Critica(EtapaProcessoOriginacao.CUSTOMER_DATA, "Erro"));

        List<CadastroDTO> cadastros = acquisitionEngineManagerItemsEventDTO.item().cadastros();
        IntStream.range(0, cadastros.size()).forEach(i -> {
            CadastroDTO cadastroEsperado = cadastros.get(i);
            if (i == 0) {
                when(customerDataClient.buscarDadosCliente(cadastroEsperado.idCadastro()))
                        .thenReturn(null);
            } else {
                when(customerDataClient.buscarDadosCliente(cadastroEsperado.idCadastro()))
                        .thenReturn(Instancio.of(CustomerDataDTO.class).create());
            }
        });

        doNothing().when(originacaoLegadoMapper).mapListaCustomerDataDTOParaOriginacaoLegado(anyList(), any(OriginacaoLegado.class));

        var novaOriginacaoLegado = customerDataHandler.processarProximo(acquisitionEngineManagerItemsEventDTO, originacaoLegado);

        assertFalse(novaOriginacaoLegado.customerDataProcessado());
        originacaoLegado.getCadastros().forEach(cadastroEsperado ->
                verify(customerDataClient, times(1))
                        .buscarDadosCliente(cadastroEsperado.getId()));

        verify(originacaoLegadoMapper, times(1)).mapListaCustomerDataDTOParaOriginacaoLegado(anyList(), eq(novaOriginacaoLegado));
    }

    @Test
    @DisplayName("Deve lançar exception quando ocorrer erro no processamento do evento")
    void deveLancarExceptionQuandoOcorrerErroNoProcessamentoDoEvento() {
        var acquisitionEngineManagerItemsEventDTO = Instancio.of(AcquisitionEngineManagerItemsEventDTO.class).create();
        var originacaoLegado = Instancio.of(OriginacaoLegado.class).create();
        originacaoLegado.adicionarCritica(new Critica(EtapaProcessoOriginacao.CUSTOMER_DATA, "Erro"));

        doThrow(MappingException.class).when(originacaoLegadoMapper).mapListaCustomerDataDTOParaOriginacaoLegado(anyList(), any(OriginacaoLegado.class));

        var novoOriginacaoLegado = customerDataHandler.processarProximo(acquisitionEngineManagerItemsEventDTO,originacaoLegado);

        assertEquals(1, novoOriginacaoLegado.getCriticas().size());
    }
}