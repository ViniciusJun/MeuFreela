package com.meufreela.api.repository;

import com.meufreela.api.entity.CategoriaServico;
import com.meufreela.api.entity.Servico;
import com.meufreela.api.entity.Usuario;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ServicoRepository extends JpaRepository<Servico, String> {

    List<Servico> findByAtivoTrueOrderByCriadoEmDesc();

    List<Servico> findByAtivoTrueAndCategoriaOrderByCriadoEmDesc(CategoriaServico categoria);

    List<Servico> findByFreelancerOrderByCriadoEmDesc(Usuario freelancer);
}