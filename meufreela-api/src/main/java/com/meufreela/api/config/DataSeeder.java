package com.meufreela.api.config;

import com.meufreela.api.entity.CategoriaServico;
import com.meufreela.api.entity.Usuario;
import com.meufreela.api.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;

// IMPORTS ADICIONADOS (faltavam — causavam erro de compilação):
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Popula o banco com freelancers de teste.
 *
 * Executa apenas nos perfis "dev" e "seed". Nunca deve rodar em produção.
 * O perfil "postgres" foi REMOVIDO da lista: misturar nome de banco com
 * nome de ambiente é uma armadilha — se esse perfil for usado em staging
 * ou produção real com PostgreSQL, dados fake vazariam para o banco real.
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
@Profile({"dev", "seed"})
public class DataSeeder implements CommandLineRunner {

    private final UsuarioRepository repository;
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

        repository.saveAll(freelancers);
        log.info("DataSeeder: {} freelancers de teste criados (senha padrão: senha12345).",
                freelancers.size());
    }

    /**
     * Fábrica de Usuario para o seed.
     *
     * Recebe avaliação, total de serviços e flag "verificado" como parâmetros
     * explícitos — antes esses valores vinham de Math.random(), o que tornava
     * o seed não-determinístico. Agora cada restart com banco vazio produz
     * EXATAMENTE o mesmo estado inicial, o que é essencial para:
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
                // Hash BCrypt de "senha12345" (mesma senha para todos os seeds)
                .senha(encoder.encode("senha12345"))
                .cpf(cpf)
                .telefone("11999999999")
                // HashSet mutável: mais seguro se "papeis" for @ElementCollection
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
}