
package br.com.contaremunerada.conta.service;

import br.com.contaremunerada.conta.domain.Conta;
import br.com.contaremunerada.conta.exception.SaldoInsuficienteException;
import br.com.contaremunerada.conta.exception.ValorInvalidoException;
import br.com.contaremunerada.conta.repository.ContaRepository;
import br.com.contaremunerada.lancamento.domain.Lancamento;
import br.com.contaremunerada.lancamento.domain.TipoLancamento;
import br.com.contaremunerada.lancamento.repository.LancamentoRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class SacarServiceTest {

  private ContaRepository contaRepository;
  private LancamentoRepository lancamentoRepository;
  private SacarService sacarService;

  @BeforeEach
  void configurar() {
    contaRepository = mock(ContaRepository.class);
    lancamentoRepository = mock(LancamentoRepository.class);

    ValidadorValorMonetario validador =
        new ValidadorValorMonetario();

    sacarService = new SacarService(
        contaRepository,
        lancamentoRepository,
        validador
    );
  }

  @Test
  void deveSacarERegistrarLancamentoNegativo() {
    // Arrange
    Conta conta = criarContaComSaldo("150.00");

    when(contaRepository.buscarPorIdComBloqueio(1L))
        .thenReturn(Optional.of(conta));

    // Act
    sacarService.sacar(1L, "50.00");

    // Assert: saldo
    assertEquals(
        new BigDecimal("100.00"),
        conta.getSaldo()
    );

    // Assert: lançamento
    ArgumentCaptor<Lancamento> captor =
        ArgumentCaptor.forClass(Lancamento.class);

    verify(lancamentoRepository, times(1))
        .save(captor.capture());

    Lancamento lancamento = captor.getValue();

    assertSame(conta, lancamento.getConta());

    assertEquals(
        TipoLancamento.SAQUE,
        lancamento.getTipo()
    );

    assertEquals(
        new BigDecimal("-50.00"),
        lancamento.getValor()
    );

    verify(contaRepository)
        .buscarPorIdComBloqueio(1L);
  }

  @Test
  void naoDeveSacarQuandoSaldoInsuficiente() {
    // Arrange
    Conta conta = criarContaComSaldo("100.00");

    when(contaRepository.buscarPorIdComBloqueio(1L))
        .thenReturn(Optional.of(conta));

    // Act + Assert
    assertThrows(
        SaldoInsuficienteException.class,
        () -> sacarService.sacar(1L, "150.00")
    );

    assertEquals(
        new BigDecimal("100.00"),
        conta.getSaldo()
    );

    verify(lancamentoRepository, never())
        .save(any(Lancamento.class));
  }

  @Test
  void naoDeveSacarValorInvalido() {
    assertThrows(
        ValorInvalidoException.class,
        () -> sacarService.sacar(1L, "0")
    );

    verifyNoInteractions(
        contaRepository,
        lancamentoRepository
    );
  }

  @Test
  void naoDeveSacarDeContaInexistente() {
    // Arrange
    when(contaRepository.buscarPorIdComBloqueio(999L))
        .thenReturn(Optional.empty());

    // Act + Assert
    ResponseStatusException exception = assertThrows(
        ResponseStatusException.class,
        () -> sacarService.sacar(999L, "50.00")
    );

    assertEquals(
        HttpStatus.NOT_FOUND,
        exception.getStatusCode()
    );

    verify(lancamentoRepository, never())
        .save(any(Lancamento.class));
  }

  private Conta criarContaComSaldo(String saldo) {
    Conta conta = new Conta(
        "CR123",
        "Maria Silva",
        "hash-senha-teste"
    );

    conta.creditar(new BigDecimal(saldo));

    return conta;
  }
}
