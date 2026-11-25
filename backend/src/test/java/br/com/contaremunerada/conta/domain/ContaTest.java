
package br.com.contaremunerada.conta.domain;

import br.com.contaremunerada.conta.exception.SaldoInsuficienteException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

public class ContaTest {

  private Conta conta;

  @BeforeEach
  void configurar() {
    conta = new Conta(
        "CR123",
        "Maria Silva",
        "hash-senha-teste"
    );
  }

  @Test
  void deveIniciarComSaldoZero() {
    assertEquals(
        0,
        conta.getSaldo().compareTo(BigDecimal.ZERO)
    );
  }

  @Test
  void deveCreditarValorAoSaldo() {
    conta.creditar(new BigDecimal("150.00"));

    assertEquals(
        new BigDecimal("150.00"),
        conta.getSaldo()
    );
  }

  @Test
  void deveDebitarValorDoSaldo() {
    conta.creditar(new BigDecimal("150.00"));

    conta.debitar(new BigDecimal("50.00"));

    assertEquals(
        new BigDecimal("100.00"),
        conta.getSaldo()
    );
  }

  @Test
  void devePermitirDebitarSaldoTotal() {
    conta.creditar(new BigDecimal("100.00"));

    conta.debitar(new BigDecimal("100.00"));

    assertEquals(
        0,
        conta.getSaldo().compareTo(BigDecimal.ZERO)
    );
  }

  @Test
  void naoDevePermitirSaldoNegativo() {
    conta.creditar(new BigDecimal("100.00"));

    assertThrows(
        SaldoInsuficienteException.class,
        () -> conta.debitar(new BigDecimal("150.00"))
    );

    assertEquals(
        new BigDecimal("100.00"),
        conta.getSaldo()
    );
  }

  @ParameterizedTest
  @ValueSource(strings = {
      "0",
      "-10.00",
      "10.123"
  })
  void naoDeveCreditarValorInvalido(String valor) {
    assertThrows(
        IllegalArgumentException.class,
        () -> conta.creditar(new BigDecimal(valor))
    );

    assertEquals(
        0,
        conta.getSaldo().compareTo(BigDecimal.ZERO)
    );
  }

  @ParameterizedTest
  @ValueSource(strings = {
      "0",
      "-10.00",
      "10.123"
  })
  void naoDeveDebitarValorInvalido(String valor) {
    conta.creditar(new BigDecimal("100.00"));

    assertThrows(
        IllegalArgumentException.class,
        () -> conta.debitar(new BigDecimal(valor))
    );

    assertEquals(
        new BigDecimal("100.00"),
        conta.getSaldo()
    );
  }

  @ParameterizedTest
  @NullSource
  void naoDeveAceitarValorNulo(BigDecimal valor) {
    assertThrows(
        IllegalArgumentException.class,
        () -> conta.creditar(valor)
    );

    assertThrows(
        IllegalArgumentException.class,
        () -> conta.debitar(valor)
    );
  }
}
