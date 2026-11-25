package br.com.contaremunerada.lancamento.dto;

import java.util.List;

public record ExtratoResponse(
    List<LancamentoResponse> content,
    int page,
    int size,
    long totalElements,
    int totalPages
) {
}
