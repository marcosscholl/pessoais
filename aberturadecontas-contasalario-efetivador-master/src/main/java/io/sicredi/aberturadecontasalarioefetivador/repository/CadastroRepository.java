package io.sicredi.aberturadecontasalarioefetivador.repository;

import io.sicredi.aberturadecontasalarioefetivador.entities.Cadastro;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CadastroRepository extends JpaRepository<Cadastro, Long> {
    Long countBySolicitacaoIdAndProcessado(Long solicitacaoId, Boolean aTrue);
}
