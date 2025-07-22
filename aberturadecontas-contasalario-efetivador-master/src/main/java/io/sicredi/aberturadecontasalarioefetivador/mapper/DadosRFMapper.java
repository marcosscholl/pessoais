package io.sicredi.aberturadecontasalarioefetivador.mapper;

import io.sicredi.aberturadecontasalarioefetivador.dto.BureauRFDTO;
import io.sicredi.aberturadecontasalarioefetivador.entities.DadosRF;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface DadosRFMapper {
    DadosRF map(BureauRFDTO dto);
}