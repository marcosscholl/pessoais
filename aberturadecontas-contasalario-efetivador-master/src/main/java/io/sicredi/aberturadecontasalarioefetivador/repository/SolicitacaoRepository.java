package io.sicredi.aberturadecontasalarioefetivador.repository;

import io.sicredi.aberturadecontasalarioefetivador.entities.Solicitacao;
import jakarta.persistence.LockModeType;
import jakarta.persistence.QueryHint;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.data.repository.query.Param;

import java.math.BigInteger;
import java.util.Optional;

public interface SolicitacaoRepository extends JpaRepository<Solicitacao, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @QueryHints({@QueryHint(name = "jakarta.persistence.lock.timeout", value = "10000")})
    @Query("SELECT s FROM Solicitacao s WHERE s.idTransacao = :idTransacao")
    Optional<Solicitacao> findByIdTransacaoLock(@Param("idTransacao") BigInteger idTransacao);

    Optional<Solicitacao> findByIdTransacao(BigInteger idTransacao);
}
