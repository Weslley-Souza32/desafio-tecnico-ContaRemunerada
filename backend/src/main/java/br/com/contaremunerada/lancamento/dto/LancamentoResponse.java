package br.com.contaremunerada.lancamento.dto;

import br.com.contaremunerada.lancamento.domain.TipoLancamento;

import java.time.OffsetDateTime;

public record LancamentoResponse(
    TipoLancamento tipo,
    OffsetDateTime dataHora,
    String valor
) {
}
