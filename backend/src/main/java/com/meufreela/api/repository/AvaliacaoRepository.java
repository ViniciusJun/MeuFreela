package com.meufreela.api.repository;

import com.meufreela.api.entity.Avaliacao;
import com.meufreela.api.entity.Solicitacao;
import com.meufreela.api.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AvaliacaoRepository extends JpaRepository<Avaliacao, String> {

    List<Avaliacao> findByAvaliadoOrderByCriadoEmDesc(Usuario avaliado);

    Optional<Avaliacao> findBySolicitacaoAndAutor(Solicitacao solicitacao, Usuario autor);

    boolean existsBySolicitacaoAndAutor(Solicitacao solicitacao, Usuario autor);

    @Query("SELECT AVG(a.nota) FROM Avaliacao a WHERE a.avaliado.id = :id")
    Double mediaPorAvaliado(@Param("id") String avaliadoId);

    @Query("SELECT COUNT(a) FROM Avaliacao a WHERE a.avaliado.id = :id")
    Long totalPorAvaliado(@Param("id") String avaliadoId);
}