package com.meufreela.api.dto;

import jakarta.validation.constraints.NotBlank;

public record SalvarFcmTokenRequest(
        @NotBlank(message = "Token é obrigatório")
        String token
) {}