package br.com.contaremunerada.auth.dto;

import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
    @NotBlank(message = "O número da conta é obrigatório.")
    String numero,

    @NotBlank(message = "A senha é obrigatória.")
    String senha
) {
}
