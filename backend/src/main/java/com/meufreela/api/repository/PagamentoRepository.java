package com.meufreela.api.repository;

import com.meufreela.api.entity.Pagamento;
import com.meufreela.api.entity.Solicitacao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PagamentoRepository extends JpaRepository<Pagamento, String> {
    Optional<Pagamento> findBySolicitacao(Solicitacao solicitacao);
}