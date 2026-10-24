package com.meufreela.api.dto;

import com.meufreela.api.entity.Usuario;

import java.util.List;

public record AuthResponse(
        String token,
        String refreshToken,
        UsuarioDto usuario
) {
    public record UsuarioDto(
            String id,
            String nome,
            String email,
            String tipo,
            boolean verificado,
            List<String> papeis
    ) {
        public static UsuarioDto from(Usuario u) {
            return new UsuarioDto(
                    u.getId(),
                    u.getNome(),
                    u.getEmail(),
                    u.getTipo().name(),
                    u.isVerificado(),
                    u.getPapeis().stream().map(Enum::name).toList()
            );
        }
    }
}