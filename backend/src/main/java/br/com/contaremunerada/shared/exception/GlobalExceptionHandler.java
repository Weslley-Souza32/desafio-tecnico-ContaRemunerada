package br.com.contaremunerada.shared.exception;

import br.com.contaremunerada.auth.exception.CredenciaisInvalidasException;
import br.com.contaremunerada.conta.exception.SaldoInsuficienteException;
import br.com.contaremunerada.conta.exception.ValorInvalidoException;
import br.com.contaremunerada.shared.exception.*;

import br.com.contaremunerada.shared.dto.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ErrorResponse> tratarValidacao(MethodArgumentNotValidException ex) {
    ErrorResponse erro = new ErrorResponse(
        "DADOS_INVALIDOS",
        "Os dados informados são inválidos."
    );

    return ResponseEntity
        .status(HttpStatus.BAD_REQUEST)
        .body(erro);
  }

  @ExceptionHandler(IllegalArgumentException.class)
  public ResponseEntity<ErrorResponse> tratarArgumentoInvalido(IllegalArgumentException ex) {

    ErrorResponse erro = new ErrorResponse(
        "DADOS_INVALIDOS",
        "Os dados informados são inválidos."
    );

    return ResponseEntity
        .status(HttpStatus.BAD_REQUEST)
        .body(erro);
  }

  @ExceptionHandler(HttpMessageNotReadableException.class)
  public ResponseEntity<ErrorResponse> tratarCorpoInvalido(HttpMessageNotReadableException ex) {

    ErrorResponse erro = new ErrorResponse(
        "DADOS_INVALIDOS",
        "O corpo da requisição é inválido."
    );

    return ResponseEntity
        .status(HttpStatus.BAD_REQUEST)
        .body(erro);
  }

  @ExceptionHandler(CredenciaisInvalidasException.class)
  public ResponseEntity<ErrorResponse> tratarCredenciaisInvalidas(CredenciaisInvalidasException ex) {

    ErrorResponse erro = new ErrorResponse(
        "CREDENCIAIS_INVALIDAS",
        "Número da conta ou senha inválidos."
    );
    return ResponseEntity
        .status(HttpStatus.UNAUTHORIZED)
        .body(erro);
  }

  @ExceptionHandler(ValorInvalidoException.class)
  public ResponseEntity<ErrorResponse> tratarValorInvalido(
      ValorInvalidoException exception
  ) {
    ErrorResponse erro = new ErrorResponse(
        "VALOR_INVALIDO",
        exception.getMessage()
    );

    return ResponseEntity
        .status(HttpStatus.UNPROCESSABLE_ENTITY)
        .body(erro);
  }

  @ExceptionHandler(SaldoInsuficienteException.class)
  public ResponseEntity<ErrorResponse> trataSaldoInsuficiente(
      SaldoInsuficienteException exception
  ) {
    ErrorResponse erro = new ErrorResponse(
        "SALDO_INSUFICIENTE",
        exception.getMessage()
    );

    return ResponseEntity
        .status(HttpStatus.UNPROCESSABLE_ENTITY)
        .body(erro);
  }
}
