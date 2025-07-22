package io.sicredi.aberturadecontasalarioefetivador.mapper;

import io.sicredi.aberturadecontasalarioefetivador.dto.*;
import io.sicredi.aberturadecontasalarioefetivador.entities.*;
import org.apache.logging.log4j.util.Strings;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.ReportingPolicy;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface SolicitacaoMapper {

    Solicitacao map(SolicitacaoRequestDTO dto);

    @Mapping(source = "solicitacao", target = "cadastros", qualifiedByName = "mapListaCadastroResponseDTO")
    SolicitacaoResponseDTO map(Solicitacao solicitacao);

    @Mapping(source = "tipoConta", target = "tipoConta", qualifiedByName = "mapTipoConta")
    Portabilidade mapPortabilidade(PortabilidadeRequestDTO dto);

    @Mapping(source = "tipoConta", target = "tipoConta", qualifiedByName = "mapCodigoTipoConta")
    PortabilidadeRequestDTO mapPortabilidadeRequestDTO(Portabilidade portabilidade);

    @Mapping(target = "dataNascimento", dateFormat = "dd/MM/yyyy")
    Cadastro mapCadastro(CadastroRequestDTO cadastroRequestDTO);

    @Mapping(target = "dataEmissaoDoc", dateFormat = "dd/MM/yyyy")
    Documento mapDocumento(DocumentoDTO documentoDTO);

    SolicitacaoRequestDTO mapToRequest(Solicitacao solicitacaoOriginal);

    @Mapping(target = "dataNascimento", dateFormat = "dd/MM/yyyy")
    CadastroRequestDTO mapCadastro(Cadastro cadastro);

    @Mapping(target = "dataEmissaoDoc", dateFormat = "dd/MM/yyyy")
    DocumentoDTO mapDocumento(Documento documento);

    @Named("mapListaCadastroResponseDTO")
    default List<CadastroResponseDTO> mapListaCadastroResponseDTO(Solicitacao solicitacao) {
        ArrayList<CadastroResponseDTO> cadastroResponseDTOs = new ArrayList<>();

        solicitacao.getCadastros().forEach(cadastro -> {
            CadastroResponseDTO.CadastroResponseDTOBuilder builder = CadastroResponseDTO.builder();

            if(Objects.nonNull(cadastro.getDadosRF()) && Strings.isNotBlank(cadastro.getDadosRF().getNome())) {
                builder.nome(cadastro.getDadosRF().getNome());
            }
            else {
                builder.nome(cadastro.getNome());
            }

            CadastroResponseDTO cadastroResponseDTO = builder
                    .cpf(cadastro.getCpf())
                    .conta(cadastro.getConta())
                    .situacao(cadastro.getSituacao().name())
                    .criticas(cadastro.getCriticas())
                    .build();

            cadastroResponseDTOs.add(cadastroResponseDTO);
        });
        return cadastroResponseDTOs;
    }

    @Named("mapTipoConta")
    default TipoConta mapTipoConta(String codigo) {
        return TipoConta.map(codigo);
    }

    @Named("mapCodigoTipoConta")
    default String mapCodigoTipoConta(TipoConta tipoConta) {
        return tipoConta.codigo;
    }
}