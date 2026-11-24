package br.com.contaremunerada.auth.dto;

public record LoginResponse(
    String token,
    Long contaId
) {
}
