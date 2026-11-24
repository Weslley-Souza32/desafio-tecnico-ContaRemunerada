package br.com.contaremunerada.auth.exception;

public class CredenciaisInvalidasException extends RuntimeException {

  public CredenciaisInvalidasException(){
    super("Número da conta ou senha inválidos.");
  }
}
