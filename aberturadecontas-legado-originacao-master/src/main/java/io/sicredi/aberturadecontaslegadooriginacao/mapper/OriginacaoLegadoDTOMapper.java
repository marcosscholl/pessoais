package io.sicredi.aberturadecontaslegadooriginacao.mapper;

import io.sicredi.aberturadecontaslegadooriginacao.dto.CadastroDTO;
import io.sicredi.aberturadecontaslegadooriginacao.dto.bpel.OriginacaoLegadoDTO;
import io.sicredi.aberturadecontaslegadooriginacao.entities.Cadastro;
import io.sicredi.aberturadecontaslegadooriginacao.entities.OriginacaoLegado;
import org.mapstruct.*;

import java.time.LocalDateTime;
import java.util.List;

@Mapper(componentModel = "spring", imports = {LocalDateTime.class}, unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface OriginacaoLegadoDTOMapper {

    OriginacaoLegadoDTO map(OriginacaoLegado originacaoLegado);

    OriginacaoLegado map(OriginacaoLegadoDTO originacaoLegadoDTO, @MappingTarget OriginacaoLegado originacaoLegado);
}