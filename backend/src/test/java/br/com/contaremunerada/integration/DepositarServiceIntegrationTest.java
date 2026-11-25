
package br.com.contaremunerada.integration;

import br.com.contaremunerada.conta.domain.Conta;
import br.com.contaremunerada.conta.repository.ContaRepository;
import br.com.contaremunerada.conta.service.DepositarService;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.jdbc.core.JdbcTemplate;

import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(properties = {
    "DB_URL=jdbc:postgresql://127.0.0.1:1/nao_usar",
    "DB_USERNAME=nao_usar",
    "DB_PASSWORD=nao_usar",
    "JWT_SECRET_BASE64=MDEyMzQ1Njc4OWFiY2RlZjAxMjM0NTY3ODlhYmNkZWY="
})
@Testcontainers
public class DepositarServiceIntegrationTest {

  @Container
  @ServiceConnection
  static PostgreSQLContainer postgres =
      new PostgreSQLContainer("postgres:18-alpine");

  @Autowired
  private ContaRepository contaRepository;

  @Autowired
  private DepositarService depositarService;

  @Autowired
  private JdbcTemplate jdbcTemplate;

  @Test
  void devePersistirSaldoELancamentoDeDeposito() {

    // Arrange: criar uma conta com saldo zero
    Conta conta = new Conta(
        "CR-TESTE-DEP-001",
        "Maria Silva",
        "hash-senha-teste"
    );

    conta = contaRepository.saveAndFlush(conta);

    Long contaId = conta.getId();

    // Act: executar o serviço real
    depositarService.depositar(contaId, "150.00");

    // Assert: consultar diretamente o PostgreSQL
    BigDecimal saldoPersistido = jdbcTemplate.queryForObject(
        "SELECT saldo FROM contas WHERE id = ?",
        BigDecimal.class,
        contaId
    );

    assertNotNull(saldoPersistido);
    assertEquals(
        0,
        new BigDecimal("150.00").compareTo(saldoPersistido)
    );

    Integer quantidadeLancamentos = jdbcTemplate.queryForObject(
        """
        SELECT COUNT(*)
        FROM lancamentos
        WHERE conta_id = ?
        """,
        Integer.class,
        contaId
    );

    assertEquals(1, quantidadeLancamentos);

    String tipo = jdbcTemplate.queryForObject(
        """
        SELECT tipo
        FROM lancamentos
        WHERE conta_id = ?
        """,
        String.class,
        contaId
    );

    assertEquals("DEPOSITO", tipo);

    BigDecimal valorLancamento = jdbcTemplate.queryForObject(
        """
        SELECT valor
        FROM lancamentos
        WHERE conta_id = ?
        """,
        BigDecimal.class,
        contaId
    );

    assertNotNull(valorLancamento);
    assertEquals(
        0,
        new BigDecimal("150.00").compareTo(valorLancamento)
    );

    // RN-03: saldo = soma dos lançamentos
    assertEquals(
        0,
        saldoPersistido.compareTo(valorLancamento)
    );
  }
}
