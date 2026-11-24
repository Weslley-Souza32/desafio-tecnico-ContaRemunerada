package br.com.contaremunerada.conta.service;

import br.com.contaremunerada.conta.domain.Conta;
import br.com.contaremunerada.conta.dto.SaldoResponse;
import br.com.contaremunerada.conta.repository.ContaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

public class ConsultarSaldoServiceTest {

  private ContaRepository contaRepository;
  private ConsultarSaldoService consultarSaldoService;

  @BeforeEach
  void configurar(){
    contaRepository = mock(ContaRepository.class);
    consultarSaldoService = new ConsultarSaldoService(contaRepository);
  }

  @Test
  void deveConsultarContaComSaldoZero(){
    Conta conta = criarConta(1L, BigDecimal.ZERO);

    when(contaRepository.findById(1L))
        .thenReturn(Optional.of(conta));

    SaldoResponse response = consultarSaldoService.consultarSaldo(1L);

    assertEquals(1L, response.contaId());
    assertEquals("CR123", response.numero());
    assertEquals("Maria Silva", response.titular());
    assertEquals("0.00", response.saldo());

    verify(contaRepository).findById(1L);
  }

  @Test
  void deveConsultarContaComSaldoPositivo() {

    Conta conta = criarConta(
        2L,
        new BigDecimal("1000.30")
    );

    when(contaRepository.findById(2L))
        .thenReturn(Optional.of(conta));

    SaldoResponse response = consultarSaldoService.consultarSaldo(2L);

    assertEquals(2L, response.contaId());
    assertEquals("1000.30", response.saldo());
  }

  @Test
  void deveRetornarNotFoundQuandoContaNaoExiste() {

    when(contaRepository.findById(999L))
        .thenReturn(Optional.empty());

    ResponseStatusException exception = assertThrows(
        ResponseStatusException.class,
        () -> consultarSaldoService.consultarSaldo(999L)
    );

    assertEquals(
        HttpStatus.NOT_FOUND,
        exception.getStatusCode()
    );
  }

  private Conta criarConta(Long id, BigDecimal saldo) {
    Conta conta = mock(Conta.class);

    when(conta.getId()).thenReturn(id);
    when(conta.getNumero()).thenReturn("CR123");
    when(conta.getTitular()).thenReturn("Maria Silva");
    when(conta.getSaldo()).thenReturn(saldo);

    return conta;
  }
}
