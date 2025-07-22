package io.sicredi.aberturadecontaslegadooriginacao.mapper;

import br.com.sicredi.framework.web.spring.exception.NotFoundException;
import io.sicredi.aberturadecontaslegadooriginacao.config.DisableDataSourceConfig;
import io.sicredi.aberturadecontaslegadooriginacao.dto.*;
import io.sicredi.aberturadecontaslegadooriginacao.dto.CondicaoPessoal;
import io.sicredi.aberturadecontaslegadooriginacao.entities.*;
import org.instancio.Instancio;
import org.instancio.Model;
import org.instancio.Select;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Import(DisableDataSourceConfig.class)
class OriginacaoLegadoMapperTest {

    @Autowired
    OriginacaoLegadoMapper originacaoLegadoMapper;
    private static final String HOLDER = "HOLDER";
    private static final String ACCOUNT_LEGACY = "ACCOUNT_LEGACY";
    private static final String CAPITAL_LEGACY = "CAPITAL_LEGACY";
    private static final String INVESTMENT_LEGACY = "INVESTMENT_LEGACY";
    private static final String CAPITAL_COMMERCIAL_PLAN_LEGACY = "CAPITAL_COMMERCIAL_PLAN_LEGACY";

    @Test
    @DisplayName("Deve mapear AcquisitionOrdersDTO para OriginacaoLegado")
    void deveMapearAcquisitionOrdersDTOParaOriginacaoLegado() {
        List<CadastroDTO> cadastroCustomizado = List.of(
                Instancio.of(CadastroDTO.class).create()
        );

        List<ProdutoDTO> produtosCustomizados = List.of(
                Instancio.of(ProdutoDTO.class)
                        .set(Select.field(ProdutoDTO::cadastros), cadastroCustomizado)
                        .set(Select.field(ProdutoDTO::tipoProduto), CAPITAL_LEGACY)
                        .create()
        );

        Model<AcquisitionOrdersDTO> model = Instancio.of(AcquisitionOrdersDTO.class)
                .supply(Select.field(AcquisitionOrdersDTO::produtos), () -> produtosCustomizados)
                .toModel();

        var acquisitionOrdersDTO = Instancio.of(model).create();

        var originacaoLegado = originacaoLegadoMapper.mapAcquisitionOrdersDTOParaOriginacaoLegado(acquisitionOrdersDTO);

        validaMapeamentoDeAcquisitionOrdersDTOParaOriginacaoLegado(acquisitionOrdersDTO, originacaoLegado);
    }

    @Test
    @DisplayName("Deve mapear CustomerDataDTO para OriginacaoLegado")
    void deveMapearCustomerDataDTOParaOriginacaoLegado() {
        List<CadastroDTO> cadastroCustomizado = List.of(
                Instancio.of(CadastroDTO.class).create()
        );

        List<ProdutoDTO> produtosCustomizados = List.of(
                Instancio.of(ProdutoDTO.class)
                        .set(Select.field(ProdutoDTO::cadastros), cadastroCustomizado)
                        .set(Select.field(ProdutoDTO::tipoProduto), CAPITAL_COMMERCIAL_PLAN_LEGACY)
                        .create()
        );

        Model<AcquisitionOrdersDTO> model = Instancio.of(AcquisitionOrdersDTO.class)
                .supply(Select.field(AcquisitionOrdersDTO::produtos), () -> produtosCustomizados)
                .toModel();

        var acquisitionOrdersDTO = Instancio.of(model)
                .withMaxDepth(10).create();
        var customerDataDTOList = getCustomerDataDTOList(acquisitionOrdersDTO);

        var originacaoLegado = originacaoLegadoMapper.mapAcquisitionOrdersDTOParaOriginacaoLegado(acquisitionOrdersDTO);

        originacaoLegadoMapper.mapListaCustomerDataDTOParaOriginacaoLegado(customerDataDTOList, originacaoLegado);

        validaMapeamentoDeAcquisitionOrdersDTOParaOriginacaoLegado(acquisitionOrdersDTO, originacaoLegado);

        validaMapeamentoDeListaDeCustomerDataDTOParaOriginacaoLegado(customerDataDTOList, originacaoLegado);
    }

    @Test
    @DisplayName("Deve mapear RegisterDataDTO para OriginacaoLegado")
    void deveMapearRegisterDataDTOParaOriginacaoLegado() {
        List<CadastroDTO> cadastroCustomizado = List.of(
                Instancio.of(CadastroDTO.class).create()
        );

        List<ProdutoDTO> produtosCustomizados = List.of(
                Instancio.of(ProdutoDTO.class)
                        .set(Select.field(ProdutoDTO::cadastros), cadastroCustomizado)
                        .set(Select.field(ProdutoDTO::tipoProduto), INVESTMENT_LEGACY)
                        .create()
        );

        Model<AcquisitionOrdersDTO> model = Instancio.of(AcquisitionOrdersDTO.class)
                .supply(Select.field(AcquisitionOrdersDTO::produtos), () -> produtosCustomizados)
                .toModel();

        var acquisitionOrdersDTO = Instancio.of(model).create();
        var customerDataDTOList = getCustomerDataDTOList(acquisitionOrdersDTO);
        var registerDataDTOList = new ArrayList<RegisterDataDTO>();

        customerDataDTOList.forEach(customerDataDTO -> {
            var registerDataDTO = Instancio.of(RegisterDataDTO.class)
                    .withMaxDepth(10)
                    .set(Select.field(RegisterDataDTO::idPedido), acquisitionOrdersDTO.id())
                    .set(Select.field(RegisterDataDTO::idCadastro), customerDataDTO.id())
                    .set(Select.field(RegisterDataDTO::status), StatusDocumento.APPROVED)
                    .create();
            registerDataDTOList.add(registerDataDTO);
        });

        var originacaoLegado = originacaoLegadoMapper.mapAcquisitionOrdersDTOParaOriginacaoLegado(acquisitionOrdersDTO);
        originacaoLegadoMapper.mapListaCustomerDataDTOParaOriginacaoLegado(customerDataDTOList, originacaoLegado);
        originacaoLegadoMapper.mapListaRegisterDataDTOParaOriginacaoLegado(registerDataDTOList, originacaoLegado);

        validaMapeamentoDeAcquisitionOrdersDTOParaOriginacaoLegado(acquisitionOrdersDTO, originacaoLegado);
        validaMapeamentoDeListaDeCustomerDataDTOParaOriginacaoLegado(customerDataDTOList, originacaoLegado);
        validaMapeamentoDeListaDeRegisterDataDTOParaOriginacaoLegado(registerDataDTOList, originacaoLegado);
    }

    @Test
    @DisplayName("Deve mapear código da entidade para OriginacaoLegado")
    void mapCodigoEntidadeParaOriginacaoLegado() {
        var acquisitionOrdersDTO = Instancio.of(AcquisitionOrdersDTO.class).create();
        var originacaoLegado = originacaoLegadoMapper.mapAcquisitionOrdersDTOParaOriginacaoLegado(acquisitionOrdersDTO);
        var codigoEntidade = "ABC";

        originacaoLegadoMapper.mapCodigoEntidadeParaOriginacaoLegado(codigoEntidade, originacaoLegado);

        assertThat(originacaoLegado.getCodigoEntidade()).isEqualTo(codigoEntidade);
    }

    @Test
    @DisplayName("Deve mapear configurações dos produtos para OriginacaoLegado")
    void deveMapearConfiguracoesDosProdutosParaOriginacaoLegado() {
        var acquisitionOrdersDTO = Instancio.of(AcquisitionOrdersDTO.class).create();
        ProdutoDTO produtoDTO = acquisitionOrdersDTO.produtos().stream().findFirst().orElseThrow(NotFoundException::new);

        var configuracaoDTO = Instancio.of(ConfiguracaoDTO.class)
                .set(Select.field(ConfiguracaoDTO::tipoProduto), produtoDTO.tipoProduto())
                .set(Select.field(ConfiguracaoDTO::diaPagamento), "2025-04-25")
                .create();

        var originacaoLegado = originacaoLegadoMapper.mapAcquisitionOrdersDTOParaOriginacaoLegado(acquisitionOrdersDTO);
        originacaoLegadoMapper.mapConfiguracaoDTOParaOriginacaoLegado(configuracaoDTO, originacaoLegado);

        for (DetalheProduto detalheProduto : originacaoLegado.getDetalheProduto().values()) {
            if (produtoDTO.tipoProduto().equalsIgnoreCase(detalheProduto.getTipoProduto())) {
                assertThat(detalheProduto.getConfiguracao().getCapital().getValor()).isEqualTo(configuracaoDTO.valor());
                assertThat(detalheProduto.getConfiguracao().getCapital().getId()).isEqualTo(configuracaoDTO.id());
                assertThat(detalheProduto.getConfiguracao().getCapital().getDiaPagamento()).isEqualTo(configuracaoDTO.diaPagamento());
            }
        }

    }

    private void validaMapeamentoDeListaDeRegisterDataDTOParaOriginacaoLegado(Collection<RegisterDataDTO> registerDataDTOList, OriginacaoLegado originacaoLegado) {
        var registerDataDTOMap = registerDataDTOList.stream()
                .collect(Collectors.toMap(RegisterDataDTO::id, Function.identity()));

        assertThat(originacaoLegado.getDocumentos()).hasSize(registerDataDTOList.size());

        assertThat(originacaoLegado.getDocumentos()).allSatisfy(documentoAtual -> {
            assertThat(documentoAtual.getId()).isIn(registerDataDTOMap.keySet());

            var documentoEsperado = registerDataDTOMap.get(documentoAtual.getId());

            assertThat(documentoAtual.getId()).isEqualTo(documentoEsperado.id());
            assertThat(documentoAtual.getChave()).isEqualTo(documentoEsperado.chave());
            assertThat(documentoAtual.getIdPedido()).isEqualTo(documentoEsperado.idPedido());
            assertThat(documentoAtual.getIdCadastro()).isEqualTo(documentoEsperado.idCadastro());
            assertThat(documentoAtual.getTipo()).isEqualTo(documentoEsperado.tipo());
            assertThat(documentoAtual.getVersao()).isEqualTo(documentoEsperado.versao());
            assertThat(documentoAtual.getStatus()).isEqualTo(documentoEsperado.status().getDescricao());
            assertThat(documentoAtual.getDataCriacao()).isEqualTo(documentoEsperado.dataCriacao());
        });
    }

    private @NotNull List<CustomerDataDTO> getCustomerDataDTOList(AcquisitionOrdersDTO acquisitionOrdersDTO) {
        var customerDataDTOList = new ArrayList<CustomerDataDTO>();
        acquisitionOrdersDTO.produtos().forEach(produto ->
                produto.cadastros().forEach(cadastroDTO -> {
                    var condicaoPessoalDTO = Instancio.of(CondicaoPessoalDTO.class)
                            .set(Select.field(CondicaoPessoalDTO::condicao), CondicaoPessoal.EMANCIPATED)
                            .set(Select.field(CondicaoPessoalDTO::capacidadeCivil), CapacidadeCivil.FULL)
                            .create();

                    var dadosPessoais = Instancio.of(DadosPessoaisDTO.class)
                            .set(Select.field(DadosPessoaisDTO::estadoCivil), EstadoCivil.MARRIED)
                            .set(Select.field(DadosPessoaisDTO::genero), Genero.MALE)
                            .supply(Select.field(DadosPessoaisDTO::cpf), cadastroDTO::cpf)
                            .create();

                    var customerDataDTO = Instancio.of(CustomerDataDTO.class)
                            .withMaxDepth(10)
                            .set(Select.field(CustomerDataDTO::id), cadastroDTO.idCadastro())
                            .set(Select.field(CustomerDataDTO::condicaoPessoal), condicaoPessoalDTO)
                            .set(Select.field(CustomerDataDTO::dadosPessoais), dadosPessoais)
                            .create();
                    customerDataDTOList.add(customerDataDTO);
                }));
        return customerDataDTOList;
    }

    private void validaMapeamentoDeListaDeCustomerDataDTOParaOriginacaoLegado(Collection<CustomerDataDTO> customerDataDTOList, OriginacaoLegado originacaoLegado) {
        var customerDataDTOmap = customerDataDTOList.stream()
                .collect(Collectors.toMap(CustomerDataDTO::id, Function.identity()));

        assertThat(originacaoLegado.getCadastros()).hasSize(customerDataDTOList.size());
        assertThat(originacaoLegado.getCadastros()).allSatisfy(cadastro -> {
            assertThat(cadastro.getId()).isIn(customerDataDTOmap.keySet());

            var customerDataDTOEsperado = customerDataDTOmap.get(cadastro.getId());

            assertThat(cadastro.getNome()).isEqualTo(customerDataDTOEsperado.dadosPessoais().nomeCompleto().nome());
            assertThat(cadastro.getCpf()).isEqualTo(customerDataDTOEsperado.dadosPessoais().cpf());
            assertThat(cadastro.getDataNascimento()).isEqualTo(customerDataDTOEsperado.dadosPessoais().dataNascimento());
            assertThat(cadastro.getDadosCadastro().getDataAtualizacao()).isEqualTo(customerDataDTOEsperado.dataAtualizacao());
            assertThat(cadastro.getDadosCadastro().getOrigemLegado()).isEqualTo(customerDataDTOEsperado.origemLegado());

            validaCondicaoPessoal(cadastro.getDadosCadastro().getCondicaoPessoal(), customerDataDTOEsperado.condicaoPessoal());
            validaDadosPessoais(cadastro.getDadosCadastro().getDadosPessoais(), customerDataDTOEsperado.dadosPessoais());
            validaDadosProfissionais(cadastro.getDadosCadastro().getDadosProfissionais(), customerDataDTOEsperado.dadosProfissionais());
        });
    }

    private void validaDadosPessoais(DadosPessoais dadosPessoais, DadosPessoaisDTO dadosPessoaisDTO) {
        assertThat(dadosPessoais.getNome()).isEqualTo(dadosPessoaisDTO.nomeCompleto().nome());
        assertThat(dadosPessoais.getNomeSocial()).isEqualTo(dadosPessoaisDTO.nomeCompleto().nomeSocial());
        assertThat(dadosPessoais.getCpf()).isEqualTo(dadosPessoaisDTO.cpf());
        assertThat(dadosPessoais.getNaturalidadeCidade()).isEqualTo(dadosPessoaisDTO.naturalidadeCidade());
        assertThat(dadosPessoais.getNaturalidadeEstado()).isEqualTo(dadosPessoaisDTO.naturalidadeEstado());
        assertThat(dadosPessoais.getNacionalidade()).isEqualTo(dadosPessoaisDTO.nacionalidade());
        assertThat(dadosPessoais.getDataNascimento()).isEqualTo(dadosPessoaisDTO.dataNascimento());
        assertThat(dadosPessoais.getGenero()).isEqualTo(dadosPessoaisDTO.genero().getDescricao());
        assertThat(dadosPessoais.getCanalComunicacaoPreferencial()).isEqualTo(dadosPessoaisDTO.canalComunicacaoPreferencial().getDescricao());
        assertThat(dadosPessoais.getEstadoCivil()).isEqualTo(dadosPessoaisDTO.estadoCivil().getDescricao());
        assertThat(dadosPessoais.getUniaoEstavel()).isEqualTo(dadosPessoaisDTO.uniaoEstavel());
        assertThat(dadosPessoais.getRegimeCasamento()).isEqualTo(dadosPessoaisDTO.regimeCasamento().getDescricao());
        assertThat(dadosPessoais.getResidenciaExterior()).isEqualTo(dadosPessoaisDTO.residenciaExterior());
        validaConjuge(dadosPessoais.getConjuge(), dadosPessoaisDTO.conjuge());
        validaIdentificacao(dadosPessoais.getIdentificacao(), dadosPessoaisDTO.identificacao());
        validaResidenciasExterior(dadosPessoais.getResidenciasExterior(), dadosPessoaisDTO.residenciasExterior());
        validaEnderecos(dadosPessoais.getEnderecos(), dadosPessoaisDTO.enderecos());
        validaEmails(dadosPessoais.getEmails(), dadosPessoaisDTO.emails());
        validaParentes(dadosPessoais.getParentes(), dadosPessoaisDTO.parentes());
        validaTelefones(dadosPessoais.getTelefones(), dadosPessoaisDTO.telefones());
        validaReferencias(dadosPessoais.getReferencias(), dadosPessoaisDTO.referencias());
    }

    private void validaReferencias(Collection<Referencia> referenciasAtuais, Collection<ReferenciaDTO> referenciasEsperadas) {
        var referenciaDTOMap = referenciasEsperadas.stream()
                .collect(Collectors.toMap(ReferenciaDTO::id, Function.identity()));

        assertThat(referenciasAtuais).hasSize(referenciasEsperadas.size());
        assertThat(referenciasAtuais).allSatisfy(referencia -> {
            assertThat(referencia.getId()).isIn(referenciaDTOMap.keySet());

            var referenciaDTOEsperado = referenciaDTOMap.get(referencia.getId());

            assertThat(referencia.getId()).isEqualTo(referenciaDTOEsperado.id());
            assertThat(referencia.getNome()).isEqualTo(referenciaDTOEsperado.nome());
            assertThat(referencia.getTelefone()).isEqualTo(referenciaDTOEsperado.telefone());
        });
    }

    private void validaMapeamentoDeAcquisitionOrdersDTOParaOriginacaoLegado(AcquisitionOrdersDTO acquisitionOrdersDTO, OriginacaoLegado originacaoLegado) {
        var listaTipoProdutoDoPedido = acquisitionOrdersDTO.produtos().stream().map(ProdutoDTO::tipoProduto).toList();

        assertThat(originacaoLegado.getIdPedido()).contains(acquisitionOrdersDTO.id());
        assertThat(originacaoLegado.getCooperativa()).contains(acquisitionOrdersDTO.cooperativa());
        assertThat(originacaoLegado.getAgencia()).contains(acquisitionOrdersDTO.agencia());
        assertThat(originacaoLegado.getStatus()).contains(acquisitionOrdersDTO.status().getDescricao());
        assertThat(originacaoLegado.getDataCriacao()).isNotNull();
        assertThat(originacaoLegado.getOrigem()).isEqualTo("ACQUISITION_ORDERS");
        assertThat(originacaoLegado.getProdutos()).hasSize(acquisitionOrdersDTO.produtos().size());
        assertThat(originacaoLegado.getDetalheProduto()).hasSize(acquisitionOrdersDTO.produtos().size());
        assertThat(originacaoLegado.getProdutos()).allSatisfy(tipoProduto ->
                assertThat(tipoProduto).isIn(listaTipoProdutoDoPedido));
        assertThat(originacaoLegado.getDetalheProduto()).allSatisfy((key, detalheProduto) -> {
            assertThat(key).isIn(listaTipoProdutoDoPedido);
            assertThat(detalheProduto.getIdSimulacao()).isIn(acquisitionOrdersDTO.produtos().stream().map(ProdutoDTO::idSimulacao).toList());
            assertThat(detalheProduto.getIdCatalogoProduto()).isIn(acquisitionOrdersDTO.produtos().stream().map(ProdutoDTO::idCatalogoProduto).toList());
            assertThat(detalheProduto.getTipoProduto()).isIn(listaTipoProdutoDoPedido);
            assertThat(detalheProduto.getCodigoProduto()).isIn(acquisitionOrdersDTO.produtos().stream().map(ProdutoDTO::codigoProduto).toList());
            assertThat(detalheProduto.getMarca()).isIn(acquisitionOrdersDTO.produtos().stream().map(ProdutoDTO::marca).toList());
            assertThat(detalheProduto.getStatus()).isIn(acquisitionOrdersDTO.produtos().stream().map(produtoDTO -> produtoDTO.status().getDescricao()).toList());
        });
        assertThat(originacaoLegado.getCadastros()).allSatisfy(cadastro -> {
            assertThat(cadastro.getCpf()).isIn(acquisitionOrdersDTO.produtos().stream().flatMap(produtoDTO -> produtoDTO.cadastros().stream().map(CadastroDTO::cpf)).toList());
            assertThat(cadastro.getId()).isIn(acquisitionOrdersDTO.produtos().stream().flatMap(produtoDTO -> produtoDTO.cadastros().stream().map(CadastroDTO::idCadastro)).toList());
        });
    }

    private void validaCondicaoPessoal(io.sicredi.aberturadecontaslegadooriginacao.entities.CondicaoPessoal condicaoPessoalAtual, CondicaoPessoalDTO condicaoPessoalEsperada) {
        assertThat(condicaoPessoalAtual.getCapacidadeCivil()).isEqualTo(condicaoPessoalEsperada.capacidadeCivil().getDescricao());
        assertThat(condicaoPessoalAtual.getCondicao()).isEqualTo(condicaoPessoalEsperada.condicao().getDescricao());
    }

    private void validaParentes(Collection<Parente> parentesAtuais, Collection<ParenteDTO> parentesEsperados) {
        var parenteDTOMap = parentesEsperados.stream()
                .collect(Collectors.toMap(ParenteDTO::id, Function.identity()));

        assertThat(parentesAtuais).hasSize(parentesEsperados.size());
        assertThat(parentesAtuais).allSatisfy(parente -> {
            assertThat(parente.getId()).isIn(parenteDTOMap.keySet());

            var parenteDTOEsperado = parenteDTOMap.get(parente.getId());

            assertThat(parente.getId()).isEqualTo(parenteDTOEsperado.id());
            assertThat(parente.getNome()).isEqualTo(parenteDTOEsperado.nome());
            assertThat(parente.getTipo()).isEqualTo(parenteDTOEsperado.tipo().getDescricao());
        });
    }

    private void validaEmails(Collection<EmailInfo> emailsAtuais, Collection<EmailDTO> emailsEsperados) {
        var emailDTOMap = emailsEsperados.stream()
                .collect(Collectors.toMap(EmailDTO::id, Function.identity()));

        assertThat(emailsAtuais).hasSize(emailsEsperados.size());
        assertThat(emailsAtuais).allSatisfy(emailInfo -> {
            assertThat(emailInfo.getId()).isIn(emailDTOMap.keySet());

            var emailEsperado = emailDTOMap.get(emailInfo.getId());

            assertThat(emailInfo.getId()).isEqualTo(emailEsperado.id());
            assertThat(emailInfo.getEmail()).isEqualTo(emailEsperado.email());
            assertThat(emailInfo.getOrdem()).isEqualTo(emailEsperado.ordem());
            assertThat(emailInfo.getVerificado()).isEqualTo(emailEsperado.verificado());
        });
    }

    private void validaEnderecos(Collection<Endereco> enderecosAtuais, Collection<EnderecoDTO> enderecosEsperados) {
        var enderecoDTOMap = enderecosEsperados.stream()
                .collect(Collectors.toMap(EnderecoDTO::id, Function.identity()));

        assertThat(enderecosAtuais).hasSize(enderecosEsperados.size());
        assertThat(enderecosAtuais).allSatisfy(endereco -> {
            assertThat(endereco.getId()).isIn(enderecoDTOMap.keySet());

            var enderecoEsperado = enderecoDTOMap.get(endereco.getId());

            assertThat(endereco.getId()).isEqualTo(enderecoEsperado.id());
            assertThat(endereco.getTipo()).isEqualTo(enderecoEsperado.tipo().getDescricao());
            assertThat(endereco.getLogradouro()).isEqualTo(enderecoEsperado.logradouro());
            assertThat(endereco.getTipoLogradouro()).isEqualTo(enderecoEsperado.tipoLogradouro());
            assertThat(endereco.getSemNumero()).isEqualTo(enderecoEsperado.semNumero());
            assertThat(endereco.getNumero()).isEqualTo(enderecoEsperado.numero());
            assertThat(endereco.getComplemento()).isEqualTo(enderecoEsperado.complemento());
            assertThat(endereco.getBairro()).isEqualTo(enderecoEsperado.bairro());
            assertThat(endereco.getCidade()).isEqualTo(enderecoEsperado.cidade());
            assertThat(endereco.getEstado()).isEqualTo(enderecoEsperado.estado());
            assertThat(endereco.getCep()).isEqualTo(enderecoEsperado.cep());
            assertThat(endereco.getCodigoPais()).isEqualTo(enderecoEsperado.codigoPais());
            assertThat(endereco.getDescricaoPais()).isEqualTo(enderecoEsperado.descricaoPais());
            assertThat(endereco.getEnderecoPrincipal()).isEqualTo(enderecoEsperado.enderecoPrincipal());
            assertThat(endereco.getPermiteCorrespondencia()).isEqualTo(enderecoEsperado.permiteCorrespondencia());
            assertThat(endereco.getOrigem()).isEqualTo(enderecoEsperado.origem());
            assertThat(endereco.getDataCriacao()).isEqualTo(enderecoEsperado.dataCriacao());
            assertThat(endereco.getDataAtualizacao()).isEqualTo(enderecoEsperado.dataAtualizacao());
        });
    }

    private void validaIdentificacao(Identificacao identificacaoAtual, IdentificacaoDTO identificacaoEsperada) {
        assertThat(identificacaoAtual.getDocumento()).isEqualTo(identificacaoEsperada.documento());
        assertThat(identificacaoAtual.getTipo()).isEqualTo(identificacaoEsperada.tipo());
        assertThat(identificacaoAtual.getDataEmissao()).isEqualTo(identificacaoEsperada.dataEmissao());
        assertThat(identificacaoAtual.getDataValidade()).isEqualTo(identificacaoEsperada.dataValidade());
        assertThat(identificacaoAtual.getOrgaoEmissor()).isEqualTo(identificacaoEsperada.orgaoEmissor());
        assertThat(identificacaoAtual.getEstadoEmissao()).isEqualTo(identificacaoEsperada.estadoEmissao());
        assertThat(identificacaoAtual.getOrigem()).isEqualTo("CUSTOMER");
    }

    private void validaConjuge(Conjuge conjugeAtual, ConjugeDTO conjugeEsperado) {
        assertThat(conjugeAtual.getCpf()).isEqualTo(conjugeEsperado.cpf());
        assertThat(conjugeAtual.getNome()).isEqualTo(conjugeEsperado.nome());
    }

    private void validaResidenciasExterior(Collection<ResidenciaExterior> residenciasExteriorAtuais,
                                           Collection<ResidenciaExteriorDTO> residenciasExteriorEsperadas) {
        var residenciasExteriorMap = residenciasExteriorEsperadas.stream()
                .collect(Collectors.toMap(ResidenciaExteriorDTO::id, Function.identity()));

        assertThat(residenciasExteriorAtuais).hasSize(residenciasExteriorEsperadas.size());
        assertThat(residenciasExteriorAtuais).allSatisfy(residenciaExterior -> {
            assertThat(residenciaExterior.getId()).isIn(residenciasExteriorMap.keySet());

            var residenciaExteriorEsperado = residenciasExteriorMap.get(residenciaExterior.getId());

            assertThat(residenciaExterior.getId()).isEqualTo(residenciaExteriorEsperado.id());
            assertThat(residenciaExterior.getCodigoPais()).isEqualTo(residenciaExteriorEsperado.codigoPais());
            assertThat(residenciaExterior.getDescricaoPais()).isEqualTo(residenciaExteriorEsperado.descricaoPais());
            assertThat(residenciaExterior.getNif()).isEqualTo(residenciaExteriorEsperado.nif());
        });
    }

    private void validaTelefones(Collection<Telefone> telefonesAtuais, Collection<TelefoneDTO> telefonesEsperados) {
        var telefoneDTOMap = telefonesEsperados.stream()
                .collect(Collectors.toMap(TelefoneDTO::id, Function.identity()));

        assertThat(telefonesAtuais).hasSize(telefonesEsperados.size());
        assertThat(telefonesAtuais).allSatisfy(telefone -> {
            assertThat(telefone.getId()).isIn(telefoneDTOMap.keySet());

            var telefoneDTOEsperado = telefoneDTOMap.get(telefone.getId());

            assertThat(telefone.getId()).isEqualTo(telefoneDTOEsperado.id());
            assertThat(telefone.getTipo()).isEqualTo(telefoneDTOEsperado.tipo().getDescricao());
            assertThat(telefone.getDdd()).isEqualTo(telefoneDTOEsperado.ddd());
            assertThat(telefone.getNumero()).isEqualTo(telefoneDTOEsperado.numero());
            assertThat(telefone.getCodigoPais()).isEqualTo(telefoneDTOEsperado.codigoPais());
        });
    }

    private void validaDadosProfissionais(DadosProfissionais dadosProfissionaisAtuais, DadosProfissionaisDTO dadosProfissionaisEsperados) {
        assertThat(dadosProfissionaisAtuais.getRendaNaoInformada()).isEqualTo(dadosProfissionaisEsperados.rendaNaoInformada());
        validaEmpregador(dadosProfissionaisAtuais.getEmpregador(), dadosProfissionaisEsperados.empregador());
        validaOcupacao(dadosProfissionaisAtuais.getOcupacao(), dadosProfissionaisEsperados.ocupacao());
        validaRenda(dadosProfissionaisAtuais.getRendas(), dadosProfissionaisEsperados.rendas());
    }

    private void validaEmpregador(Empregador empregadorAtual, EmpregadorDTO empregadorEsperado) {
        assertThat(empregadorAtual.getDocumento()).isEqualTo(empregadorEsperado.documento());
        assertThat(empregadorAtual.getNome()).isEqualTo(empregadorEsperado.nome());
    }

    private void validaOcupacao(Ocupacao ocupacaoAtual, OcupacaoDTO ocupacaoEsperada) {
        assertThat(ocupacaoAtual.getCodigo()).isEqualTo(ocupacaoEsperada.codigo());
        assertThat(ocupacaoAtual.getDescricao()).isEqualTo(ocupacaoEsperada.descricao());
        assertThat(ocupacaoAtual.getDataCriacao()).isEqualTo(ocupacaoEsperada.dataCriacao());
        assertThat(ocupacaoAtual.getDataAdmissao()).isEqualTo(ocupacaoEsperada.dataCriacao());
        assertThat(ocupacaoAtual.getDataAtualizacao()).isEqualTo(ocupacaoEsperada.dataAtualizacao());
    }

    private void validaRenda(Collection<Renda> rendasAtuais, Collection<RendaDTO> rendasEsperadas) {
        var rendaDTOMap = rendasEsperadas.stream()
                .collect(Collectors.toMap(RendaDTO::id, Function.identity()));

        assertThat(rendasAtuais).hasSize(rendasEsperadas.size());
        assertThat(rendasAtuais).allSatisfy(renda -> {
            assertThat(renda.getId()).isIn(rendaDTOMap.keySet());

            var rendaDTOEsperada = rendaDTOMap.get(renda.getId());

            assertThat(renda.getId()).isEqualTo(rendaDTOEsperada.id());
            assertThat(renda.getValor()).isEqualTo(rendaDTOEsperada.valor());
            assertThat(renda.getTipo()).isEqualTo(rendaDTOEsperada.tipo().getDescricao());
            assertThat(renda.getDataCriacao()).isEqualTo(rendaDTOEsperada.dataCriacao());
            assertThat(renda.getDataAtualizacao()).isEqualTo(rendaDTOEsperada.dataAtualizacao());
        });
    }
}