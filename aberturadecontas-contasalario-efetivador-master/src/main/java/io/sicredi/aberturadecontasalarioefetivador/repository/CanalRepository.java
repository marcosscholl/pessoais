package io.sicredi.aberturadecontasalarioefetivador.repository;

import io.sicredi.aberturadecontasalarioefetivador.entities.Canal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CanalRepository extends JpaRepository<Canal, Long> {

    Optional<Canal> findByCodigoAndAtivoTrue(Long codigo);

    Optional<Canal> findByCodigoAndDocumentoAndAtivoIsTrue(Long codigo, String documento);

    Optional<Canal> findByCodigo(Long codigo);

    Optional<Canal> findByNome(String nome);

    boolean existsByCodigo(Long codigo);

    boolean existsByNome(String nome);
}
