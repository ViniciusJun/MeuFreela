package com.meufreela.api.repository;

import com.meufreela.api.entity.Mensagem;
import com.meufreela.api.entity.Solicitacao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MensagemRepository extends JpaRepository<Mensagem, String> {
    List<Mensagem> findBySolicitacaoOrderByEnviadaEmAsc(Solicitacao s);
}