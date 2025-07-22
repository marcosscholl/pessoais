package io.sicredi.aberturadecontasalarioefetivador.service;

import br.com.sicredi.mua.cadastro.business.server.ws.v1.aberturacontaservice.GetFontesPagadoras;
import br.com.sicredi.mua.cadastro.business.server.ws.v1.aberturacontaservice.GetFontesPagadorasResponse;
import io.sicredi.aberturadecontasalarioefetivador.client.aberturacontaservice.AberturaContaServiceClient;
import io.sicredi.aberturadecontasalarioefetivador.client.aberturacontaservice.dto.GetFontesPagadorasBuilder;
import io.sicredi.aberturadecontasalarioefetivador.entities.Solicitacao;
import io.sicredi.aberturadecontasalarioefetivador.exceptions.WebserviceException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Slf4j
@RequiredArgsConstructor
@Service
public class AberturaContaService {

    private final AberturaContaServiceClient client;

    public boolean validarFontePagadora(Solicitacao solicitacao) {
        try {
            String documento =
                    Objects.nonNull(solicitacao.getCnpjFontePagadora())
                            ? solicitacao.getCnpjFontePagadora() : solicitacao.getCpfFontePagadora();

            GetFontesPagadoras getFontesPagadoras = GetFontesPagadorasBuilder.builder()
                    .codigo(solicitacao.getCodConvenioFontePagadora())
                    .cnpj(documento)
                    .build();

            GetFontesPagadorasResponse getFontesPagadorasResponse = client.consultarFontesPagadoras(getFontesPagadoras);

            if (Objects.nonNull(getFontesPagadorasResponse.getFontesPagadoras())
                    && !getFontesPagadorasResponse.getFontesPagadoras().getFontePagadora().isEmpty()) {
                return Objects.nonNull(getFontesPagadorasResponse.getFontesPagadoras().getFontePagadora());
            } else {
                return false;
            }
        } catch (Exception e) {
            log.error("Erro ao acessar serviço AberturaContaService para a solicitação {}", solicitacao.getIdTransacao(), e);
            throw new WebserviceException("Erro ao acessar serviço AberturaContaService: " + e.getMessage(), e);
        }
    }
}
