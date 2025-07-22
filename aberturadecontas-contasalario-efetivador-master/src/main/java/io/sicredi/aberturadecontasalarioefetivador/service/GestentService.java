package io.sicredi.aberturadecontasalarioefetivador.service;

import io.sicredi.aberturadecontasalarioefetivador.client.gestest.GestentConnectorApiClient;
import io.sicredi.aberturadecontasalarioefetivador.dto.GestentDTO;
import io.sicredi.aberturadecontasalarioefetivador.exceptions.AgenciaECooperativaNaoCorrespondentesException;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@AllArgsConstructor
@Slf4j
public class GestentService {
    public static final String CODIGO_TIPO_ENTIDADE = "AGENCIA";
    public static final String CODIGO_SITUACAO = "ATIVA";
    private final GestentConnectorApiClient gestentConnectorApiClient;

    public GestentDTO obterEntidadeSicredi(String codigoCooperativa, String codigoAgencia) {
        return gestentConnectorApiClient.getEntidadeSicredi(
                0,
                1,
                CODIGO_TIPO_ENTIDADE,
                codigoAgencia,
                codigoCooperativa,
                CODIGO_SITUACAO
        );
    }

    public String consultaCodigoEntidadeDeCooperativaEAgenciaCorrespondentes(String numCooperativa, String numAgencia) {
        Optional<String> branchCode = this.consultarCodigoEntidade(numCooperativa, numAgencia);
        return branchCode.orElseThrow(AgenciaECooperativaNaoCorrespondentesException::new);
    }

    public Optional<String> consultarCodigoEntidade(String codigoCooperativa, String codigoAgencia) {
        try {
            var gestentDTO = obterEntidadeSicredi(codigoCooperativa, codigoAgencia);
            return gestentDTO.content().isEmpty()
                    ? Optional.empty() : Optional.of(gestentDTO.content().getFirst().codigoEntidade());
        } catch (Exception e) {
            log.error("GestentService : Erro ao obter codigoEntidade para o COOP/UA "+ codigoCooperativa + "/"  + codigoAgencia + " : " + e.getMessage(), e);
            return Optional.empty();
        }
    }
}
