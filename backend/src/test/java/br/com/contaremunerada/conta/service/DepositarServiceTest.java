
package br.com.contaremunerada.conta.service;

import br.com.contaremunerada.conta.domain.Conta;
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

public class DepositarServiceTest {

  private ContaRepository contaRepository;
  private LancamentoRepository lancamentoRepository;
  private DepositarService depositarService;

  @BeforeEach
  void configurar() {
    contaRepository = mock(ContaRepository.class);
    lancamentoRepository = mock(LancamentoRepository.class);

    ValidadorValorMonetario validador =
        new ValidadorValorMonetario();

    depositarService = new DepositarService(
        contaRepository,
        lancamentoRepository,
        validador
    );
  }

  @Test
  void deveDepositarERegistrarLancamento() {
    // Arrange
    Conta conta = new Conta(
        "CR123",
        "Maria Silva",
        "hash-senha-teste"
    );

    when(contaRepository.buscarPorIdComBloqueio(1L))
        .thenReturn(Optional.of(conta));

    // Act
    depositarService.depositar(1L, "150.00");

    // Assert: saldo atualizado
    assertEquals(
        new BigDecimal("150.00"),
        conta.getSaldo()
    );

    // Assert: lançamento criado corretamente
    ArgumentCaptor<Lancamento> captor =
        ArgumentCaptor.forClass(Lancamento.class);

    verify(lancamentoRepository, times(1))
        .save(captor.capture());

    Lancamento lancamento = captor.getValue();

    assertSame(conta, lancamento.getConta());
    assertEquals(
        TipoLancamento.DEPOSITO,
        lancamento.getTipo()
    );
    assertEquals(
        new BigDecimal("150.00"),
        lancamento.getValor()
    );

    verify(contaRepository)
        .buscarPorIdComBloqueio(1L);
  }

  @Test
  void naoDeveDepositarValorInvalido() {
    // Act + Assert
    assertThrows(
        ValorInvalidoException.class,
        () -> depositarService.depositar(1L, "0")
    );

    verifyNoInteractions(
        contaRepository,
        lancamentoRepository
    );
  }

  @Test
  void naoDeveDepositarEmContaInexistente() {
    // Arrange
    when(contaRepository.buscarPorIdComBloqueio(999L))
        .thenReturn(Optional.empty());

    // Act + Assert
    ResponseStatusException exception = assertThrows(
        ResponseStatusException.class,
        () -> depositarService.depositar(999L, "150.00")
    );

    assertEquals(
        HttpStatus.NOT_FOUND,
        exception.getStatusCode()
    );

    verify(lancamentoRepository, never())
        .save(any(Lancamento.class));
  }
}
