package com.meufreela.api.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

@Service
public class JwtService {

    private static final String ISSUER = "meufreela-api";
    private static final String AUDIENCE = "meufreela-app";
    private static final String CLAIM_TYPE = "type";
    private static final String TYPE_ACCESS = "access";
    private static final String TYPE_REFRESH = "refresh";

    @Value("${jwt.secret}")
    private String secretKey;

    @Value("${jwt.expiration}")
    private long expirationMs;

    @Value("${jwt.refresh-expiration}")
    private long refreshExpirationMs;

    // ==================== Geração ====================

    public String gerarToken(UserDetails userDetails, Map<String, Object> claims) {
        return construir(userDetails, claims, expirationMs, TYPE_ACCESS);
    }

    public String gerarRefreshToken(UserDetails userDetails) {
        return construir(userDetails, new HashMap<>(), refreshExpirationMs, TYPE_REFRESH);
    }

    private String construir(UserDetails userDetails, Map<String, Object> claims,
                             long ttlMs, String type) {
        Date agora = new Date();
        return Jwts.builder()
                .claims(claims)
                .claim(CLAIM_TYPE, type)                          // ← distingue access vs refresh
                .subject(userDetails.getUsername())
                .issuer(ISSUER)                                   // ← iss
                .audience().add(AUDIENCE).and()                   // ← aud
                .id(UUID.randomUUID().toString())                 // ← jti (base para revogação)
                .issuedAt(agora)
                .expiration(new Date(agora.getTime() + ttlMs))
                .signWith(getSigningKey(), Jwts.SIG.HS256)        // ← algoritmo explícito
                .compact();
    }

    // ==================== Validação ====================

    /**
     * Valida access token. Rejeita refresh tokens.
     *
     * Retorna false (em vez de lançar) para permitir que o filtro trate
     * como "não autenticado" e devolva 401 em vez de 500.
     */
    public boolean tokenValido(String token, UserDetails userDetails) {
        try {
            Claims claims = extrairTodasClaims(token);
            return claims.getSubject().equals(userDetails.getUsername())
                    && TYPE_ACCESS.equals(claims.get(CLAIM_TYPE, String.class))
                    && claims.getExpiration().after(new Date());
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Valida refresh token. Uso exclusivo do endpoint /auth/refresh.
     * Rejeita access tokens — simetria de segurança.
     */
    public boolean refreshTokenValido(String token, UserDetails userDetails) {
        try {
            Claims claims = extrairTodasClaims(token);
            return claims.getSubject().equals(userDetails.getUsername())
                    && TYPE_REFRESH.equals(claims.get(CLAIM_TYPE, String.class))
                    && claims.getExpiration().after(new Date());
        } catch (Exception e) {
            return false;
        }
    }

    public String extrairUsername(String token) {
        return extrairClaim(token, Claims::getSubject);
    }

    public <T> T extrairClaim(String token, Function<Claims, T> resolver) {
        return resolver.apply(extrairTodasClaims(token));
    }

    private Claims extrairTodasClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .requireIssuer(ISSUER)                  // ← rejeita tokens sem iss correto
                .requireAudience(AUDIENCE)              // ← rejeita tokens sem aud correto
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private SecretKey getSigningKey() {
        // O secret é Base64 (gerado por `openssl rand -base64 64`).
        // Nada de encode+decode no-op: Decoders.BASE64 faz o trabalho direto.
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}