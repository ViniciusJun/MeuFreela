package com.meufreela.api.config;

import com.meufreela.api.entity.CategoriaServico;
import com.meufreela.api.entity.Servico;
import com.meufreela.api.entity.Usuario;
import com.meufreela.api.repository.ServicoRepository;
import com.meufreela.api.repository.UsuarioRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Popula o banco com freelancers e serviços de exemplo.
 *
 * Executa apenas nos perfis "dev" e "seed". Nunca deve rodar em produção.
 * O perfil "postgres" foi deliberadamente REMOVIDO: misturar nome de banco
 * com nome de ambiente é uma armadilha — se esse perfil fosse usado em
 * staging ou produção, dados fake vazariam para o banco real.
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
@Profile({"dev", "seed"})
public class DataSeeder implements CommandLineRunner {

    private final UsuarioRepository repository;
    private final ServicoRepository servicoRepository;   // <-- CORREÇÃO: campo que faltava
    private final PasswordEncoder encoder;

    @Override
    public void run(String... args) {
        long existentes = repository.count();
        if (existentes > 0) {
            log.info("DataSeeder ignorado: já existem {} usuários no banco.", existentes);
            return;
        }

        List<Usuario> freelancers = List.of(
                criar("Carlos Eletricista", "carlos@teste.com", "11111111111",
                        CategoriaServico.REFORMAS_REPAROS,
                        "Eletricista com 10 anos de experiência.", 80.0,
                        4.8, 42, true),

                criar("Ana Diarista", "ana@teste.com", "22222222222",
                        CategoriaServico.SERVICOS_DOMESTICOS,
                        "Diarista detalhista, atendo toda a região.", 50.0,
                        4.6, 87, true),

                criar("Téo TI", "teo@teste.com", "33333333333",
                        CategoriaServico.TECNOLOGIA,
                        "Formatação, redes, montagem de PC.", 100.0,
                        4.9, 23, false),

                criar("Pedro Bartender", "pedro@teste.com", "44444444444",
                        CategoriaServico.EVENTOS,
                        "Bartender para festas e eventos corporativos.", 120.0,
                        4.2, 15, true),

                criar("Ju Cabeleireira", "ju@teste.com", "55555555555",
                        CategoriaServico.BELEZA,
                        "Cortes, coloração e penteados em domicílio.", 70.0,
                        5.0, 61, true)
        );

        // saveAll retorna as entidades persistidas já com ID gerado — necessário
        // para o relacionamento Servico.freelancer (que usa o ID do freelancer).
        List<Usuario> persistidos = repository.saveAll(freelancers);

        int servicosCriados = criarServicos(persistidos);

        log.info("DataSeeder: {} freelancers e {} serviços criados (senha padrão: senha12345).",
                persistidos.size(), servicosCriados);
    }

    /**
     * Fábrica de Usuario para o seed.
     *
     * Recebe avaliação, total de serviços e flag "verificado" como parâmetros
     * explícitos — antes esses valores vinham de Math.random(), o que tornava
     * o seed não-determinístico. Agora cada restart com banco vazio produz
     * EXATAMENTE o mesmo estado inicial, essencial para:
     *   - reproduzir bugs relatados por outros devs;
     *   - manter screenshots de demo consistentes;
     *   - escrever testes de integração que dependam do seed.
     */
    private Usuario criar(String nome, String email, String cpf,
                          CategoriaServico cat, String descricao, double preco,
                          double avaliacaoMedia, int totalServicos, boolean verificado) {
        return Usuario.builder()
                .nome(nome)
                .email(email)
                .senha(encoder.encode("senha12345"))
                .cpf(cpf)
                .telefone("11999999999")
                .papeis(new HashSet<>(Set.of(Usuario.TipoUsuario.FREELANCER)))
                .tipo(Usuario.TipoUsuario.FREELANCER)
                .categoria(cat)
                .descricao(descricao)
                .precoHora(preco)
                .avaliacaoMedia(avaliacaoMedia)
                .totalServicos(totalServicos)
                .verificado(verificado)
                .build();
    }

    /**
     * Cria serviços de exemplo para cada freelancer.
     *
     * Acumula em uma lista e persiste tudo de uma vez via saveAll — evita
     * um round-trip ao banco por serviço. Retorna a quantidade criada para
     * ser reportada no log.
     */
    private int criarServicos(List<Usuario> freelancers) {
        List<Servico> servicos = new ArrayList<>();

        for (Usuario f : freelancers) {
            if (f.getCategoria() == null) continue;

            servicos.add(Servico.builder()
                    .freelancer(f)
                    .categoria(f.getCategoria())
                    .titulo("Serviço padrão: " + f.getNome())
                    .descricao(f.getDescricao())
                    .preco(f.getPrecoHora())
                    .build());

            switch (f.getCategoria()) {
                case REFORMAS_REPAROS -> servicos.add(Servico.builder()
                        .freelancer(f)
                        .categoria(CategoriaServico.REFORMAS_REPAROS)
                        .titulo("Instalação de chuveiro elétrico")
                        .descricao("Instalação completa com garantia de 3 meses")
                        .preco(f.getPrecoHora() * 1.2)
                        .build());

                case TECNOLOGIA -> servicos.add(Servico.builder()
                        .freelancer(f)
                        .categoria(CategoriaServico.TECNOLOGIA)
                        .titulo("Formatação com backup")
                        .descricao("Formatação completa + backup dos arquivos")
                        .preco(f.getPrecoHora() * 0.9)
                        .build());

                case SERVICOS_DOMESTICOS -> servicos.add(Servico.builder()
                        .freelancer(f)
                        .categoria(CategoriaServico.SERVICOS_DOMESTICOS)
                        .titulo("Limpeza pós-obra")
                        .descricao("Limpeza pesada após reforma, inclui materiais")
                        .preco(f.getPrecoHora() * 1.5)
                        .build());

                default -> { /* categorias sem serviço extra por enquanto */ }
            }
        }

        servicoRepository.saveAll(servicos);
        return servicos.size();
    }
}