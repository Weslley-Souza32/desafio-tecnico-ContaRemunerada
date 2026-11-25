
package br.com.contaremunerada.conta.dto;

import br.com.contaremunerada.conta.exception.ValorInvalidoException;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class MovimentacaoRequestTest {

  @Test
  void deveAceitarValorDoTipoString() {
    MovimentacaoRequest request =
        new MovimentacaoRequest("150.00");

    assertEquals(
        "150.00",
        request.obterValorComoTexto()
    );
  }

  @Test
  void deveRejeitarValorNumerico() {
    MovimentacaoRequest request =
        new MovimentacaoRequest(150.00);

    assertThrows(
        ValorInvalidoException.class,
        request::obterValorComoTexto
    );
  }

  @Test
  void deveRejeitarValorBooleano() {
    MovimentacaoRequest request =
        new MovimentacaoRequest(true);

    assertThrows(
        ValorInvalidoException.class,
        request::obterValorComoTexto
    );
  }

  @Test
  void deveRejeitarValorNulo() {
    MovimentacaoRequest request =
        new MovimentacaoRequest(null);

    assertThrows(
        ValorInvalidoException.class,
        request::obterValorComoTexto
    );
  }
}
