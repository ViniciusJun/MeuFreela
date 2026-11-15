package com.meufreela.api.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Libera o console do H2 apenas em perfil de desenvolvimento.
 *
 * Usa @Order(1) para ser avaliado ANTES do SecurityFilterChain principal
 * — caso contrário, a regra "anyRequest().authenticated()" do
 * SecurityConfig interceptaria /h2-console/** antes do permitAll.
 *
 * Este bean NÃO é criado em produção (perfil "prod" ou padrão sem "dev").
 * Em produção, o console do H2 fica inacessível — correto, porque o banco
 * em produção é PostgreSQL, e o H2 nem estará no classpath efetivo.
 */
@Configuration
@Profile("dev")
public class H2SecurityConfig {

    @Bean
    @Order(1)
    public SecurityFilterChain h2ConsoleFilterChain(HttpSecurity http) throws Exception {
        http
                .securityMatcher("/h2-console/**")
                .csrf(csrf -> csrf.disable())
                .headers(headers -> headers.frameOptions(frame -> frame.sameOrigin()))
                .authorizeHttpRequests(auth -> auth.anyRequest().permitAll());

        return http.build();
    }
}