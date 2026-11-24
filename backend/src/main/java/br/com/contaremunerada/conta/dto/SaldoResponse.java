package br.com.contaremunerada.conta.dto;

public record SaldoResponse(
    Long contaId,
    String numero,
    String titular,
    String saldo
) {
}
