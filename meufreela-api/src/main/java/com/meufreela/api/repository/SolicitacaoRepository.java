package com.meufreela.api.repository;

import com.meufreela.api.entity.Solicitacao;
import com.meufreela.api.entity.StatusSolicitacao;
import com.meufreela.api.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SolicitacaoRepository extends JpaRepository<Solicitacao, String> {

    List<Solicitacao> findByClienteOrderByCriadoEmDesc(Usuario cliente);

    List<Solicitacao> findByFreelancerOrderByCriadoEmDesc(Usuario freelancer);

    List<Solicitacao> findByFreelancerAndStatusOrderByCriadoEmDesc(
            Usuario freelancer, StatusSolicitacao status);
}