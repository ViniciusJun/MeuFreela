package com.meufreela.api.dto;

import com.meufreela.api.entity.Usuario;
import jakarta.validation.constraints.*;

public record CadastroRequest(
        @NotBlank(message = "Nome é obrigatório")
        @Size(min = 3, message = "Nome deve ter pelo menos 3 caracteres")
        String nome,

        @NotBlank(message = "E-mail é obrigatório")
        @Email(message = "E-mail inválido")
        String email,

        @NotBlank(message = "Senha é obrigatória")
        @Size(min = 8, message = "Senha deve ter pelo menos 8 caracteres")
        String senha,

        @NotBlank(message = "CPF é obrigatório")
        @Pattern(regexp = "\\d{11}", message = "CPF deve ter 11 dígitos")
        String cpf,

        @NotBlank(message = "Telefone é obrigatório")
        @Pattern(regexp = "\\d{10,11}", message = "Telefone inválido")
        String telefone,

        @NotNull(message = "Tipo de usuário é obrigatório")
        Usuario.TipoUsuario tipo
) {}