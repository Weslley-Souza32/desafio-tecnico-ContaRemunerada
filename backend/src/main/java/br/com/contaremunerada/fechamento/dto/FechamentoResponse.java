package br.com.contaremunerada.fechamento.dto;

import br.com.contaremunerada.fechamento.domain.Fechamento;
import br.com.contaremunerada.fechamento.domain.StatusFechamento;

public record FechamentoResponse(StatusFechamento status, int contasProcessadas) {

  public static FechamentoResponse from(Fechamento fechamento) {

    return new FechamentoResponse(
        fechamento.getStatus(),
        fechamento.getContasProcessadas()
    );
  }
}
