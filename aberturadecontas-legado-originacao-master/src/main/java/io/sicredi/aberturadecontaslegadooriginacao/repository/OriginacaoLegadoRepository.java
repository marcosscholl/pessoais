package io.sicredi.aberturadecontaslegadooriginacao.repository;

import io.sicredi.aberturadecontaslegadooriginacao.entities.OriginacaoLegado;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OriginacaoLegadoRepository extends MongoRepository<OriginacaoLegado, String> {

    @Query("{ 'idPedido' : ?0 }")
    Optional<OriginacaoLegado> findByIdPedido(String idPedido);
}
