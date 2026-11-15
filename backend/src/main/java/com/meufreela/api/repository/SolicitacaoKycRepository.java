package com.meufreela.api.repository;

import com.meufreela.api.entity.SolicitacaoKyc;
import com.meufreela.api.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SolicitacaoKycRepository extends JpaRepository<SolicitacaoKyc, String> {
    Optional<SolicitacaoKyc> findFirstByUsuarioOrderByDataEnvioDesc(Usuario usuario);
}
