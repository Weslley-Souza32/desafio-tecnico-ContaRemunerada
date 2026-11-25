
package br.com.contaremunerada.conta.service;

import br.com.contaremunerada.conta.exception.ValorInvalidoException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class ValidadorValorMonetarioTest {

  private ValidadorValorMonetario validador;

  @BeforeEach
  void configurar() {
    validador = new ValidadorValorMonetario();
  }

  @ParameterizedTest
  @CsvSource({
      "150.00, 150.00",
      "10.5, 10.50",
      "100, 100.00",
      "0.01, 0.01"
  })
  void deveAceitarValoresValidos(
      String entrada,
      String esperado
  ) {
    BigDecimal resultado = validador.validar(entrada);

    assertEquals(new BigDecimal(esperado), resultado);
    assertEquals(2, resultado.scale());
  }

  @ParameterizedTest
  @ValueSource(strings = {
      "0",
      "0.00",
      "-10.00",
      "10.123",
      "abc",
      "1E3",
      "10,50",
      " 10.50",
      "10.50 "
  })
  void deveRejeitarValoresInvalidos(String entrada) {
    assertThrows(
        ValorInvalidoException.class,
        () -> validador.validar(entrada)
    );
  }

  @ParameterizedTest
  @NullAndEmptySource
  void deveRejeitarValoresNulosOuVazios(String entrada) {
    assertThrows(
        ValorInvalidoException.class,
        () -> validador.validar(entrada)
    );
  }

  @Test
  void devePreservarPrecisaoDecimal() {
    BigDecimal resultado = validador.validar("0.10");

    assertEquals(new BigDecimal("0.10"), resultado);
    assertEquals("0.10", resultado.toPlainString());
  }
}
