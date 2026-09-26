package com.meufreela.api.config;

import com.meufreela.api.entity.CategoriaServico;
import com.meufreela.api.entity.Usuario;
import com.meufreela.api.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;

@Configuration
@RequiredArgsConstructor
@Profile("!prod")
public class DataSeeder implements CommandLineRunner {

    private final UsuarioRepository repository;
    private final PasswordEncoder encoder;

    @Override
    public void run(String... args) {
        if (repository.count() > 0) return;

        List<Usuario> freelancers = List.of(
                criar("Carlos Eletricista", "carlos@teste.com", "11111111111",
                        CategoriaServico.REFORMAS_REPAROS,
                        "Eletricista com 10 anos de experiência.", 80.0),
                criar("Ana Diarista", "ana@teste.com", "22222222222",
                        CategoriaServico.SERVICOS_DOMESTICOS,
                        "Diarista detalhista, atendo toda a região.", 50.0),
                criar("Téo TI", "teo@teste.com", "33333333333",
                        CategoriaServico.TECNOLOGIA,
                        "Formatação, redes, montagem de PC.", 100.0),
                criar("Pedro Bartender", "pedro@teste.com", "44444444444",
                        CategoriaServico.EVENTOS,
                        "Bartender para festas e eventos corporativos.", 120.0),
                criar("Ju Cabeleireira", "ju@teste.com", "55555555555",
                        CategoriaServico.BELEZA,
                        "Cortes, coloração e penteados em domicílio.", 70.0)
        );

        repository.saveAll(freelancers);
        System.out.println("✅ " + freelancers.size() + " freelancers de teste criados.");
    }

    private Usuario criar(String nome, String email, String cpf,
                          CategoriaServico cat, String descricao, double preco) {
        return Usuario.builder()
                .nome(nome)
                .email(email)
                .senha(encoder.encode("senha12345"))
                .cpf(cpf)
                .telefone("11999999999")
                .tipo(Usuario.TipoUsuario.FREELANCER)
                .categoria(cat)
                .descricao(descricao)
                .precoHora(preco)
                .avaliacaoMedia(4.5 + Math.random() * 0.5)
                .totalServicos((int) (Math.random() * 50) + 5)
                .verificado(Math.random() > 0.3)
                .build();
    }
}