package br.com.contaremunerada.taxa.dto;

public record TaxaDiariaRequest(Object taxaDiaria) {

  public String obterTaxaComTexto() {
    if (taxaDiaria instanceof String valor) {
      return valor;
    }

    throw new IllegalArgumentException("A taxa diária informada é inválida.");
  }
}
