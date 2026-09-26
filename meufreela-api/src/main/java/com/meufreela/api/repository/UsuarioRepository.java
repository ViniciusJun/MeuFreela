package com.meufreela.api.repository;

import com.meufreela.api.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.meufreela.api.entity.CategoriaServico;
import com.meufreela.api.entity.Usuario.TipoUsuario;
import java.util.List;
import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, String> {

    Optional<Usuario> findByEmail(String email);

    boolean existsByEmail(String email);

    boolean existsByCpf(String cpf);
    List<Usuario> findByTipoAndCategoria(TipoUsuario tipo, CategoriaServico categoria);

    List<Usuario> findByTipo(TipoUsuario tipo);
}