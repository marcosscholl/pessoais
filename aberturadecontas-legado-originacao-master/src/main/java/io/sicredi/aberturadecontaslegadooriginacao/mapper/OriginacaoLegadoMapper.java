package io.sicredi.aberturadecontaslegadooriginacao.mapper;

import io.sicredi.aberturadecontaslegadooriginacao.dto.*;
import io.sicredi.aberturadecontaslegadooriginacao.dto.CondicaoPessoal;
import io.sicredi.aberturadecontaslegadooriginacao.entities.*;
import io.sicredi.aberturadecontaslegadooriginacao.util.Sanitizador;
import org.mapstruct.*;

import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring", imports = {LocalDateTime.class}, unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface OriginacaoLegadoMapper {

    @Mapping(target = "idPedido", source = "id")
    @Mapping(target = "produtos", source = "produtos", qualifiedByName = "mapListaProdutoDTOParaSetTipoProduto")
    @Mapping(target = "detalheProduto", source = "produtos", qualifiedByName = "mapListaProdutoDTOParaMapDetalheProduto")
    @Mapping(target = "origem", constant = "ACQUISITION_ORDERS")
    @Mapping(target = "status", source = "status", qualifiedByName = "converterStatusPedido")
    @Mapping(target = "dataCriacao", expression = "java(LocalDateTime.now())")
    @Mapping(target = "dataAtualizacao", expression = "java(LocalDateTime.now())")
    @Mapping(target = "cadastros", expression = "java(mapListaCadastroDTOParaListaCadastro(acquisitionOrdersDTO.produtos().stream().flatMap(produto -> produto.cadastros().stream()).toList()))")
    OriginacaoLegado mapAcquisitionOrdersDTOParaOriginacaoLegado(AcquisitionOrdersDTO acquisitionOrdersDTO);

    void merge(OriginacaoLegado originacaoLegadoOrigem, @MappingTarget OriginacaoLegado originacaoLegadoAlvo);

    OriginacaoLegado merge(OriginacaoLegado originacaoLegadoOrigem);

    @Mapping(target = "idItemPedido", source = "id")
    @Mapping(target = "relacionamento", source = "cadastros", qualifiedByName = "mapListaCadastrosParaListaDadosRelacionamento")
    @Mapping(target = "status", source = "status", qualifiedByName = "converterStatusProduto")
    DetalheProduto mapProdutoDTOParaDetalheProduto(ProdutoDTO produtoDTO);

    @Named("converterStatusProduto")
    default String converterStatusProduto(StatusProduto statusProduto) {
        return statusProduto != null ? statusProduto.getDescricao() : "Desconhecido";
    }

    @Mapping(target = "id", source = "idCadastro")
    Cadastro mapCadastroDTOParaCadastro(CadastroDTO cadastroDTO);

    @IterableMapping(elementTargetType = Cadastro.class)
    List<Cadastro> mapListaCadastroDTOParaListaCadastro(List<CadastroDTO> listaCadastros);

    @Named("mapListaProdutoDTOParaSetTipoProduto")
    default List<String> mapListaProdutoDTOParaSetTipoProduto(List<ProdutoDTO> produtos) {
        var produtosLegado = filtrarProdutosLegado(produtos);
        return produtosLegado.stream().map(ProdutoDTO::tipoProduto).toList();
    }

    @Named("mapListaProdutoDTOParaMapDetalheProduto")
    default Map<String, DetalheProduto> mapListaProdutoDTOParaMapDetalheProduto(List<ProdutoDTO> produtos) {
        if (produtos == null) {
            return Collections.emptyMap();
        }
        var produtosLegado = filtrarProdutosLegado(produtos);
        return produtosLegado.stream()
                .collect(Collectors.toMap(ProdutoDTO::tipoProduto, this::mapProdutoDTOParaDetalheProduto));
    }

    @AfterMapping
    default void mapListaCustomerDataDTOParaOriginacaoLegado(List<CustomerDataDTO> customerDataDTOList, @MappingTarget OriginacaoLegado originacaoLegado) {
        if (Objects.isNull(originacaoLegado.getCadastros())) {
            originacaoLegado.setCadastros(new ArrayList<>());
        }
        mapListaCustomerDataDTOParaListaCadastro(customerDataDTOList, originacaoLegado.getCadastros());
    }

    default void mapListaCustomerDataDTOParaListaCadastro(List<CustomerDataDTO> customerDataDTOList, @MappingTarget List<Cadastro> cadastros) {
        var cadastroMap = cadastros.stream()
                .collect(Collectors.toMap(
                        Cadastro::getId,
                        Function.identity(),
                        (existing, replacement) -> existing
                ));

        for (var customerDataDTO : customerDataDTOList) {
            var cadastro = cadastroMap.get(customerDataDTO.id());
            if (Objects.nonNull(cadastro)) {
                atualizaCadastroComCustomerDataDTO(customerDataDTO, cadastro);
            } else {
                var novoCadastro = new Cadastro();
                atualizaCadastroComCustomerDataDTO(customerDataDTO, novoCadastro);
                if (!cadastroMap.containsKey(novoCadastro.getId())) {
                    cadastros.add(novoCadastro);
                    cadastroMap.put(novoCadastro.getId(), novoCadastro);
                }
            }
        }
    }


    @Mapping(target = "nome", source = "dadosPessoais.nomeCompleto.nome")
    @Mapping(target = "cpf", source = "dadosPessoais.cpf")
    @Mapping(target = "dataNascimento", source = "dadosPessoais.dataNascimento")
    @Mapping(target = "dadosCadastro.dataAtualizacao", source = "dataAtualizacao")
    @Mapping(target = "dadosCadastro.origemLegado", source = "origemLegado")
    @Mapping(target = "dadosCadastro.condicaoPessoal", source = "condicaoPessoal", qualifiedByName = "mapCondicaoPessoalDTOParaCondicaoPessoal")
    @Mapping(target = "dadosCadastro.dadosPessoais", source = "dadosPessoais", qualifiedByName = "mapDadosPessoaisDTOParaDadosPessoais")
    @Mapping(target = "dadosCadastro.dadosProfissionais", source = "dadosProfissionais", qualifiedByName = "mapDadosProfissionaisDTOParaDadosProfissionais")
    void atualizaCadastroComCustomerDataDTO(CustomerDataDTO customerDataDTO, @MappingTarget Cadastro cadastro);

    @Named("mapDadosPessoaisDTOParaDadosPessoais")
    @Mapping(target = "nome", source = "nomeCompleto.nome", qualifiedByName = "sanitizarValor")
    @Mapping(target = "nomeSocial", source = "nomeCompleto.nomeSocial", qualifiedByName = "sanitizarValor")
    @Mapping(target = "conjuge.nome", source = "conjuge.nome", qualifiedByName = "sanitizarValor")
    @Mapping(target = "naturalidadeCidade", source = "naturalidadeCidade", qualifiedByName = "sanitizarValor")
    @Mapping(target = "identificacao.origem", constant = "CUSTOMER")
    @Mapping(target = "genero", source = "genero", qualifiedByName = "converterGenero")
    @Mapping(target = "estadoCivil", source = "estadoCivil", qualifiedByName = "converterEstadoCivil")
    @Mapping(target = "regimeCasamento", source = "regimeCasamento", qualifiedByName = "converterRegistroCasamento")
    @Mapping(target = "canalComunicacaoPreferencial", source = "canalComunicacaoPreferencial", qualifiedByName = "converterPreferenciaComunicacao")
    @Mapping(target = "parentes", source = "parentes", qualifiedByName = "mapListaParentesDTOParaParentes")
    @Mapping(target = "enderecos", source = "enderecos", qualifiedByName = "mapListaEnderecosDTOParaEnderecos")
    void mapDadosPessoaisDTOParaDadosPessoais(DadosPessoaisDTO dadosPessoaisDTO, @MappingTarget DadosPessoais dadosPessoais);

    @Named("mapListaEnderecosDTOParaEnderecos")
    default List<Endereco> mapListaEnderecosDTOParaEnderecos(List<EnderecoDTO> enderecos) {
        if (enderecos == null) {
            return Collections.emptyList();
        }
        return enderecos.stream()
                .map(this::mapEnderecoDTOParaEndereco)
                .collect(Collectors.toList());
    }

    @Mapping(source = "tipo", target = "tipo", qualifiedByName = "converterTipoEndereco")
    @Mapping(source = "cidade", target = "cidade", qualifiedByName = "sanitizarValor")
    @Mapping(source = "bairro", target = "bairro", qualifiedByName = "sanitizarValor")
    @Mapping(source = "logradouro", target = "logradouro", qualifiedByName = "sanitizarValor")
    @Mapping(source = "complemento", target = "complemento", qualifiedByName = "sanitizarValor")
    Endereco mapEnderecoDTOParaEndereco(EnderecoDTO enderecoDTO);

    @Named("converterTipoEndereco")
    default String converterTipoEndereco(TipoEndereco tipoEndereco) {
        return tipoEndereco != null ? tipoEndereco.getDescricao() : "Desconhecido";
    }

    @Named("sanitizarValor")
    default String sanitizarValor(String valor) {
        return Sanitizador.sanitizar(valor);
    }

    @Named("converterPreferenciaComunicacao")
    default String converterPreferenciaComunicacao(PreferenciaComunicacao preferenciaComunicacao) {
        return preferenciaComunicacao != null ? preferenciaComunicacao.getDescricao() : "Desconhecido";
    }

    @Named("mapListaParentesDTOParaParentes")
    default List<Parente> mapListaParentesDTOParaParentes(List<ParenteDTO> parentes) {
        if (parentes == null) {
            return Collections.emptyList();
        }
        return parentes.stream()
                .map(this::mapParenteDTOParaParente)
                .collect(Collectors.toList());
    }

    @Mapping(target = "tipo", qualifiedByName = "converterTipoParente")
    @Mapping(target = "nome", source = "nome", qualifiedByName = "sanitizarValor")
    Parente mapParenteDTOParaParente(ParenteDTO parenteDTO);

    @Named("converterTipoParente")
    default String converterTipoParente(TipoParente tipo) {
        return tipo != null ? tipo.getDescricao() : "Desconhecido";
    }

    @Mapping(target = "tipo", qualifiedByName = "converterTipoTelefone")
    Telefone mapTelefoneDTOParaTelefone(TelefoneDTO telefoneDTO);

    @Named("converterTipoTelefone")
    default String converterTipoTelefone(TipoTelefone tipo) {
        return tipo != null ? tipo.getDescricao() : "Desconhecido";
    }

    @Named("converterGenero")
    default String converterGenero(Genero genero) {
        return genero != null ? genero.getDescricao() : "Desconhecido";
    }

    @Named("converterEstadoCivil")
    default String converterEstadoCivil(EstadoCivil estadoCivil) {
        return estadoCivil != null ? estadoCivil.getDescricao() : "Desconhecido";
    }

    @Named("converterRegistroCasamento")
    default String converterRegistroCasamento(RegimeCasamento regimeCasamento) {
        return regimeCasamento != null ? regimeCasamento.getDescricao() : "Desconhecido";
    }

    @Named("mapCondicaoPessoalDTOParaCondicaoPessoal")
    @Mapping(target = "capacidadeCivil", qualifiedByName = "converterCapacidadeCivil")
    @Mapping(target = "condicao", qualifiedByName = "converterCondicaoPessoal")
    void mapCondicaoPessoalDTOParaCondicaoPessoal(CondicaoPessoalDTO condicaoPessoalDTO, @MappingTarget io.sicredi.aberturadecontaslegadooriginacao.entities.CondicaoPessoal condicaoPessoal);

    @Named("converterCapacidadeCivil")
    default String converterCapacidadeCivil(CapacidadeCivil capacidadeCivil) {
        return capacidadeCivil != null ? capacidadeCivil.getDescricao() : "Desconhecido";
    }

    @Named("converterCondicaoPessoal")
    default String converterCondicaoPessoal(CondicaoPessoal capacidadeCivil) {
        return capacidadeCivil != null ? capacidadeCivil.getDescricao() : "Desconhecido";
    }

    @Named("mapDadosProfissionaisDTOParaDadosProfissionais")
    @Mapping(target = "ocupacao.dataAdmissao", source = "ocupacao.dataCriacao")
    @Mapping(target = "rendas", source = "rendas", qualifiedByName = "mapListaRendasDTOParaRendas")
    void mapDadosProfissionaisDTOParaDadosProfissionais(DadosProfissionaisDTO dadosProfissionaisDTO, @MappingTarget DadosProfissionais dadosProfissionais);

    @Named("mapListaRendasDTOParaRendas")
    default List<Renda> mapListaRendasDTOParaRendas(List<RendaDTO> rendas) {
        if (rendas == null) {
            return Collections.emptyList();
        }
        return rendas.stream()
                .map(this::mapRendaDTOParaRenda)
                .collect(Collectors.toList());
    }

    @Mapping(source = "tipo", target = "tipo", qualifiedByName = "converterTipoRenda")
    Renda mapRendaDTOParaRenda(RendaDTO renda);

    @Named("converterTipoRenda")
    default String converterTipoRenda(TipoRenda tipoRenda) {
        return tipoRenda != null ? tipoRenda.getDescricao() : "Desconhecido";
    }

    @Named("converterStatusDocumento")
    default String converterStatusDocumento(StatusDocumento statusDocumento) {
        return statusDocumento != null ? statusDocumento.getDescricao() : "Desconhecido";
    }

    @Named("converterStatusPedido")
    default String converterStatusPedido(StatusPedido statusPedido) {
        return statusPedido != null ? statusPedido.getDescricao() : "Desconhecido";
    }

    @AfterMapping
    default void mapListaRegisterDataDTOParaOriginacaoLegado(List<RegisterDataDTO> registerDataDTOList, @MappingTarget OriginacaoLegado originacaoLegado) {
        if (Objects.isNull(originacaoLegado.getDocumentos())) {
            originacaoLegado.setDocumentos(new ArrayList<>());
        }
        mapListaRegisterDataDTOParaListaDocumento(registerDataDTOList, originacaoLegado.getDocumentos());
    }

    @IterableMapping(elementTargetType = Documento.class)
    default void mapListaRegisterDataDTOParaListaDocumento(List<RegisterDataDTO> registerDataDTOList, @MappingTarget List<Documento> documentos) {
        var documentoMap = documentos.stream()
                .collect(Collectors.toMap(Documento::getId, Function.identity()));

        for (var registerDataDTO : registerDataDTOList) {
            var documento = documentoMap.get(registerDataDTO.id());
            if (Objects.nonNull(documento)) {
                atualizaDocumentoComRegisterDataDTO(registerDataDTO, documento);
            } else {
                var novoDocumento = new Documento();
                atualizaDocumentoComRegisterDataDTO(registerDataDTO, novoDocumento);
                documentos.add(novoDocumento);
            }
        }
    }

    @Mapping(target = "status", qualifiedByName = "converterStatusDocumento")
    void atualizaDocumentoComRegisterDataDTO(RegisterDataDTO registerDataDTO, @MappingTarget Documento documento);

    @Mapping(target = "codigoEntidade")
    void mapCodigoEntidadeParaOriginacaoLegado(String codigoEntidade, @MappingTarget OriginacaoLegado originacaoLegado);

    @AfterMapping
    default void mapConfiguracaoDTOParaOriginacaoLegado(ConfiguracaoDTO configuracaoDTO, @MappingTarget OriginacaoLegado originacaoLegado) {
        mapConfiguracaoDTOParaDetalheProduto(configuracaoDTO, originacaoLegado.getDetalheProduto());
    }

    default void mapConfiguracaoDTOParaDetalheProduto(ConfiguracaoDTO configuracaoDTO, @MappingTarget Map<String, DetalheProduto> detalheProduto) {
        for (DetalheProduto detalhe : detalheProduto.values()) {
            if (detalhe.getTipoProduto().equalsIgnoreCase(configuracaoDTO.tipoProduto())) {
                mapConfiguracaoDTOParaConfiguracaoDetalhe(configuracaoDTO, detalhe);
            }
        }
    }

    @Mapping(target = "configuracao.capital.id", source = "id")
    @Mapping(target = "configuracao.capital.valor", source = "valor")
    @Mapping(target = "configuracao.capital.diaPagamento", source = "diaPagamento")
    void mapConfiguracaoDTOParaConfiguracaoDetalhe(ConfiguracaoDTO configuracaoDTO, @MappingTarget DetalheProduto configuracao);

    @Named("mapListaCadastrosParaListaDadosRelacionamento")
    default List<DadosRelacionamento> mapListaCadastrosParaListaDadosRelacionamento(List<CadastroDTO> cadastros) {
        if (cadastros == null) {
            return Collections.emptyList();
        }
        var temMultiplosCadastros = cadastros
                                            .stream()
                                            .map(CadastroDTO::idCadastro)
                                            .distinct()
                                            .toList()
                                            .size() > 1;

        return cadastros.stream()
                .map(cadastroDTO -> mapCadastroDTOParaDadosRelacionamento(cadastroDTO, temMultiplosCadastros))
                .collect(Collectors.toList());
    }

    default DadosRelacionamento mapCadastroDTOParaDadosRelacionamento(CadastroDTO cadastro, boolean temMultiploCadastros) {
        boolean titular = temMultiploCadastros ? cadastro.titularPrincipal() : Boolean.TRUE;
        return new DadosRelacionamento(
                cadastro.cpf(),
                converterPapel(cadastro.papel()),
                cadastro.semPermissao(),
                titular
        );
    }

    default List<ProdutoDTO> filtrarProdutosLegado(List<ProdutoDTO> produtos) {
        return produtos.stream().filter(produtoDTO -> "ACCOUNT_LEGACY".equalsIgnoreCase(produtoDTO.tipoProduto())
                                                      || "INVESTMENT_LEGACY".equalsIgnoreCase(produtoDTO.tipoProduto())
                                                      || "CAPITAL_LEGACY".equalsIgnoreCase(produtoDTO.tipoProduto())
                                                      || "CAPITAL_COMMERCIAL_PLAN_LEGACY".equalsIgnoreCase(produtoDTO.tipoProduto())).toList();

    }

    @Named("converterPapel")
    default String converterPapel(Papel role) {
        return role != null ? role.getDescricao() : "Desconhecido";
    }
}