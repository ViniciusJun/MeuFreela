package com.meufreela.api.service;

import com.meufreela.api.dto.CriarServicoRequest;
import com.meufreela.api.entity.CategoriaServico;
import com.meufreela.api.entity.Servico;
import com.meufreela.api.entity.Usuario;
import com.meufreela.api.repository.ServicoRepository;
import com.meufreela.api.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ServicoServiceTest {

    @Mock private ServicoRepository servicoRepository;
    @Mock private UsuarioRepository usuarioRepository;

    @InjectMocks private ServicoService servicoService;

    private Usuario freelancer;
    private Usuario outroFreelancer;
    private Servico servicoDoFreelancer;

    @BeforeEach
    void setUp() {
        freelancer = Usuario.builder()
                .id("fre-1")
                .nome("Carlos")
                .email("carlos@test.com")
                .tipo(Usuario.TipoUsuario.FREELANCER)
                .papeis(new HashSet<>(Set.of(Usuario.TipoUsuario.FREELANCER)))
                .build();

        outroFreelancer = Usuario.builder()
                .id("fre-2")
                .nome("Ana")
                .email("ana@test.com")
                .tipo(Usuario.TipoUsuario.FREELANCER)
                .papeis(new HashSet<>(Set.of(Usuario.TipoUsuario.FREELANCER)))
                .build();

        servicoDoFreelancer = Servico.builder()
                .id("srv-1")
                .freelancer(freelancer)
                .titulo("Instalação elétrica")
                .descricao("Serviço completo com garantia")
                .categoria(CategoriaServico.REFORMAS_REPAROS)
                .preco(100.0)
                .ativo(true)
                .build();
    }

    private CriarServicoRequest requestValido() {
        return new CriarServicoRequest(
                "Instalação de chuveiro",
                "Instalação completa com garantia de 3 meses",
                CategoriaServico.REFORMAS_REPAROS,
                120.0
        );
    }

    @Test
    void criar_comoFreelancer_deveSalvarServico() {
        when(usuarioRepository.findByEmail("carlos@test.com"))
                .thenReturn(Optional.of(freelancer));
        when(servicoRepository.save(any(Servico.class))).thenAnswer(inv -> {
            Servico s = inv.getArgument(0);
            // Simula o id gerado pelo JPA sem expor setter público em produção.
            ReflectionTestUtils.setField(s, "id", "srv-novo");
            return s;
        });

        var resp = servicoService.criar("carlos@test.com", requestValido());

        // CORREÇÃO: remoção de ternário sem efeito (typo "chuvelo" acidental)
        assertThat(resp.titulo()).isEqualTo("Instalação de chuveiro");
        assertThat(resp.preco()).isEqualTo(120.0);
        assertThat(resp.ativo()).isTrue();
        assertThat(resp.freelancerId()).isEqualTo("fre-1");

        verify(servicoRepository).save(any(Servico.class));
    }

    @Test
    void criar_comoClienteSemPapelFreelancer_deveLancarExcecao() {
        Usuario cliente = Usuario.builder()
                .id("cli-1")
                .email("joao@test.com")
                .papeis(new HashSet<>(Set.of(Usuario.TipoUsuario.CLIENTE)))
                .tipo(Usuario.TipoUsuario.CLIENTE)
                .build();

        when(usuarioRepository.findByEmail("joao@test.com"))
                .thenReturn(Optional.of(cliente));

        assertThatThrownBy(() -> servicoService.criar("joao@test.com", requestValido()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Ative seu perfil de freelancer");

        verify(servicoRepository, never()).save(any());
    }

    @Test
    void atualizar_servicoDeOutroFreelancer_deveLancarExcecao() {
        when(usuarioRepository.findByEmail("ana@test.com"))
                .thenReturn(Optional.of(outroFreelancer));
        when(servicoRepository.findById("srv-1"))
                .thenReturn(Optional.of(servicoDoFreelancer));

        assertThatThrownBy(() ->
                servicoService.atualizar("ana@test.com", "srv-1", requestValido()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("não é dono");

        verify(servicoRepository, never()).save(any());
    }

    @Test
    void atualizar_servicoProprio_deveSalvar() {
        when(usuarioRepository.findByEmail("carlos@test.com"))
                .thenReturn(Optional.of(freelancer));
        when(servicoRepository.findById("srv-1"))
                .thenReturn(Optional.of(servicoDoFreelancer));
        when(servicoRepository.save(any(Servico.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        var resp = servicoService.atualizar(
                "carlos@test.com", "srv-1", requestValido());

        assertThat(resp.titulo()).isEqualTo("Instalação de chuveiro");
        verify(servicoRepository).save(any(Servico.class));
    }

    @Test
    void alternarAtivo_deveInverterOStatus() {
        when(usuarioRepository.findByEmail("carlos@test.com"))
                .thenReturn(Optional.of(freelancer));
        when(servicoRepository.findById("srv-1"))
                .thenReturn(Optional.of(servicoDoFreelancer));
        when(servicoRepository.save(any(Servico.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        var resp = servicoService.alternarAtivo("carlos@test.com", "srv-1");

        // antes: ativo=true → depois: ativo=false
        assertThat(resp.ativo()).isFalse();
    }

    @Test
    void alternarAtivo_servicoDeOutro_deveLancarExcecao() {
        when(usuarioRepository.findByEmail("ana@test.com"))
                .thenReturn(Optional.of(outroFreelancer));
        when(servicoRepository.findById("srv-1"))
                .thenReturn(Optional.of(servicoDoFreelancer));

        assertThatThrownBy(() ->
                servicoService.alternarAtivo("ana@test.com", "srv-1"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("não é dono");
    }

    @Test
    void excluir_servicoDeOutro_deveLancarExcecao() {
        when(usuarioRepository.findByEmail("ana@test.com"))
                .thenReturn(Optional.of(outroFreelancer));
        when(servicoRepository.findById("srv-1"))
                .thenReturn(Optional.of(servicoDoFreelancer));

        assertThatThrownBy(() ->
                servicoService.excluir("ana@test.com", "srv-1"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("não é dono");

        verify(servicoRepository, never()).delete(any());
    }

    @Test
    void listarSemCategoria_deveChamarFindByAtivoTrue() {
        when(servicoRepository.findByAtivoTrueOrderByCriadoEmDesc())
                .thenReturn(List.of(servicoDoFreelancer));

        var resp = servicoService.listar(null);

        assertThat(resp).hasSize(1);
        verify(servicoRepository).findByAtivoTrueOrderByCriadoEmDesc();
        verify(servicoRepository, never())
                .findByAtivoTrueAndCategoriaOrderByCriadoEmDesc(any());
    }

    @Test
    void listarComCategoria_deveFiltrar() {
        when(servicoRepository
                .findByAtivoTrueAndCategoriaOrderByCriadoEmDesc(
                        CategoriaServico.REFORMAS_REPAROS))
                .thenReturn(List.of(servicoDoFreelancer));

        var resp = servicoService.listar(CategoriaServico.REFORMAS_REPAROS);

        assertThat(resp).hasSize(1);
        verify(servicoRepository)
                .findByAtivoTrueAndCategoriaOrderByCriadoEmDesc(
                        CategoriaServico.REFORMAS_REPAROS);
    }

    @Test
    void detalhe_servicoInexistente_deveLancarExcecao() {
        when(servicoRepository.findById("nao-existe"))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> servicoService.detalhe("nao-existe"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Serviço não encontrado");
    }
}