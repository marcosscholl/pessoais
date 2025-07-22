package io.sicredi.aberturadecontasalarioefetivador.service;

import io.sicredi.aberturadecontasalarioefetivador.entities.Canal;
import io.sicredi.aberturadecontasalarioefetivador.repository.CanalRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.Objects;
import java.util.Optional;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class CanalService {

    private final CanalRepository canalRepository;
    private final Random random;

    public Canal criarCanal(Canal canal) {
        if (canalRepository.existsByNome(canal.getNome())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Nome do canal já existe.");
        }

        if (Objects.isNull(canal.getCodigo())) {
            canal.setCodigo(gerarCodigoUnico());
        }

        return canalRepository.save(canal);
    }

    public Optional<Canal> consultarCanal(Long codigo) {
        return canalRepository.findByCodigo(codigo);
    }

    public Canal desativarCanal(Long codigo) {
        var canal = canalRepository.findByCodigo(codigo)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Canal não encontrado."));

        canal.setAtivo(false);
        return canalRepository.save(canal);
    }

    public Optional<Canal> consultarCanalAtivo(Long codigo) {
        return canalRepository.findByCodigoAndAtivoTrue(codigo);
    }

    public Optional<Canal> consultarCanalAtivoPorCodigoEDocumento(Long codigo, String documento) {
        return canalRepository.findByCodigoAndDocumentoAndAtivoIsTrue(codigo, documento);
    }

    private Long gerarCodigoUnico() {
        Long codigo;
        do {
            codigo = 1111L + random.nextInt(8889);
        } while (canalRepository.existsByCodigo(codigo));
        return codigo;
    }
}