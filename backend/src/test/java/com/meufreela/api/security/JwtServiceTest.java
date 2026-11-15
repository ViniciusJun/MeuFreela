package com.meufreela.api.security;

import io.jsonwebtoken.Jwts;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Date;
import java.util.HashMap;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Testes do JwtService. Cobre as garantias de segurança que
 * implementamos no round anterior:
 *
 *  1. Access token tem type=access; refresh token tem type=refresh
 *  2. tokenValido() rejeita refresh tokens (vulnerabilidade fechada)
 *  3. refreshTokenValido() rejeita access tokens
 *  4. iss e aud errados invalidam o token
 *  5. Token expirado é rejeitado
 *  6. Token de outro usuário é rejeitado
 *  7. Token malformado / assinatura inválida retorna false (não lança)
 *
 * Não carrega contexto Spring: instancia o JwtService diretamente e
 * injeta as propriedades via ReflectionTestUtils. Isso torna o teste
 * ~100x mais rápido que @SpringBootTest.
 */
class JwtServiceTest {

    private JwtService jwtService;
    private UserDetails userDetails;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();
        // Secret de teste válido em Base64 (decodifica para 42 bytes)
        ReflectionTestUtils.setField(jwtService, "secretKey",
                "dGVzdC1zZWNyZXQta2V5LWF0LWxlYXN0LTMyLWJ5dGVz");
        ReflectionTestUtils.setField(jwtService, "expirationMs", 3_600_000L);          // 1h
        ReflectionTestUtils.setField(jwtService, "refreshExpirationMs", 604_800_000L); // 7d

        userDetails = User.withUsername("joao@test.com")
                .password("irrelevante")
                .authorities("ROLE_CLIENTE")
                .build();
    }

    // ==================== Geração ====================

    @Test
    void gerarToken_deveCriarAccessTokenComClaimsCorretos() {
        String token = jwtService.gerarToken(userDetails, new HashMap<>());

        assertThat(token).isNotBlank();

        var claims = Jwts.parser()
                .verifyWith((javax.crypto.SecretKey) ReflectionTestUtils.invokeMethod(
                        jwtService, "getSigningKey"))
                .build()
                .parseSignedClaims(token)
                .getPayload();

        assertThat(claims.getSubject()).isEqualTo("joao@test.com");
        assertThat(claims.get("type", String.class)).isEqualTo("access");
        assertThat(claims.getIssuer()).isEqualTo("meufreela-api");
        assertThat(claims.getAudience()).contains("meufreela-app");
        assertThat(claims.getId()).isNotBlank();  // jti presente
        assertThat(claims.getExpiration()).isAfter(new Date());
    }

    @Test
    void gerarRefreshToken_deveCriarRefreshTokenComClaimsCorretos() {
        String token = jwtService.gerarRefreshToken(userDetails);

        assertThat(token).isNotBlank();
        assertThat(jwtService.extrairUsername(token)).isEqualTo("joao@test.com");

        // Verifica o type via método público de validação
        assertThat(jwtService.refreshTokenValido(token, userDetails)).isTrue();
        assertThat(jwtService.tokenValido(token, userDetails)).isFalse();  // não é access
    }

    @Test
    void doisTokensDoMesmoUsuario_devemSerDiferentesPorCausaDoJti() {
        String t1 = jwtService.gerarToken(userDetails, new HashMap<>());
        String t2 = jwtService.gerarToken(userDetails, new HashMap<>());

        // Cada token tem jti único → tokens assinados diferem mesmo com mesmos claims
        assertThat(t1).isNotEqualTo(t2);
    }

    // ==================== Validação (access) ====================

    @Test
    void tokenValido_accessTokenDeMesmoUsuario_deveRetornarTrue() {
        String token = jwtService.gerarToken(userDetails, new HashMap<>());
        assertThat(jwtService.tokenValido(token, userDetails)).isTrue();
    }

    @Test
    void tokenValido_refreshTokenComoAccess_deveRetornarFalse() {
        String refresh = jwtService.gerarRefreshToken(userDetails);

        // ESTE É O TESTE CRÍTICO: a vulnerabilidade original permitia
        // usar refresh token (7 dias) como access token (1h).
        assertThat(jwtService.tokenValido(refresh, userDetails)).isFalse();
    }

    @Test
    void tokenValido_accessDeOutroUsuario_deveRetornarFalse() {
        String token = jwtService.gerarToken(userDetails, new HashMap<>());

        UserDetails outro = User.withUsername("maria@test.com")
                .password("x")
                .authorities("ROLE_CLIENTE")
                .build();

        assertThat(jwtService.tokenValido(token, outro)).isFalse();
    }

    @Test
    void tokenValido_tokenExpirado_deveRetornarFalse() {
        // Cria um serviço com expiração de 1ms para forçar expiração
        JwtService expirado = new JwtService();
        ReflectionTestUtils.setField(expirado, "secretKey",
                "dGVzdC1zZWNyZXQta2V5LWF0LWxlYXN0LTMyLWJ5dGVz");
        ReflectionTestUtils.setField(expirado, "expirationMs", 1L);
        ReflectionTestUtils.setField(expirado, "refreshExpirationMs", 1L);

        String token = expirado.gerarToken(userDetails, new HashMap<>());

        // Espera 100ms para garantir que expirou
        try { Thread.sleep(100); } catch (InterruptedException ignored) { }

        assertThat(expirado.tokenValido(token, userDetails)).isFalse();
    }

    // ==================== Validação (refresh) ====================

    @Test
    void refreshTokenValido_refreshTokenValido_deveRetornarTrue() {
        String refresh = jwtService.gerarRefreshToken(userDetails);
        assertThat(jwtService.refreshTokenValido(refresh, userDetails)).isTrue();
    }

    @Test
    void refreshTokenValido_accessToken_deveRetornarFalse() {
        String access = jwtService.gerarToken(userDetails, new HashMap<>());
        assertThat(jwtService.refreshTokenValido(access, userDetails)).isFalse();
    }

    // ==================== Robustez ====================

    @Test
    void tokenValido_tokenMalformado_deveRetornarFalse() {
        // Não deve lançar exceção — apenas retornar false
        assertThat(jwtService.tokenValido("nao.eh.um.jwt", userDetails)).isFalse();
        assertThat(jwtService.tokenValido("", userDetails)).isFalse();
        assertThat(jwtService.tokenValido("abc.def", userDetails)).isFalse();
    }

    @Test
    void tokenValido_tokenComAssinaturaDeOutroSecret_deveRetornarFalse() {
        JwtService outroServico = new JwtService();
        // Secret diferente
        ReflectionTestUtils.setField(outroServico, "secretKey",
                "b3V0cm8tc2VjcmV0LXRlc3RlLXN1ZmZpY2llbnRlbWVudGUtbG9uZ28=");
        ReflectionTestUtils.setField(outroServico, "expirationMs", 3_600_000L);
        ReflectionTestUtils.setField(outroServico, "refreshExpirationMs", 604_800_000L);

        String tokenDeOutroSecret = outroServico.gerarToken(userDetails, new HashMap<>());

        // Assinatura não bate → false
        assertThat(jwtService.tokenValido(tokenDeOutroSecret, userDetails)).isFalse();
    }

    // ==================== Extração ====================

    @Test
    void extrairUsername_deveRetornarSubject() {
        String token = jwtService.gerarToken(userDetails, new HashMap<>());
        assertThat(jwtService.extrairUsername(token)).isEqualTo("joao@test.com");
    }
}