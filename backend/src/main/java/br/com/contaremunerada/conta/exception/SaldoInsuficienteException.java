package br.com.contaremunerada.conta.exception;

public class SaldoInsuficienteException extends RuntimeException{

  public SaldoInsuficienteException() {
    super("Saldo insuficiente para realizar o saque.");
  }
}
