package br.com.contaremunerada.conta.dto;

import br.com.contaremunerada.conta.exception.ValorInvalidoException;

public record MovimentacaoRequest(Object valor) {

  public String obterValorComoTexto() {
    if (!(valor instanceof String texto)){
      throw new ValorInvalidoException();
    }

    return texto;
  }
}
