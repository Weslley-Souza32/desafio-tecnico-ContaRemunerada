package br.com.contaremunerada.conta.exception;

public class ValorInvalidoException extends RuntimeException {

  public ValorInvalidoException(){
    super("O valor informado é inválido.");
  }
}
