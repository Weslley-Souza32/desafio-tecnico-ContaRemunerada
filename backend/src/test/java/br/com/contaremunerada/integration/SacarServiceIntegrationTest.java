
package br.com.contaremunerada.integration;

import br.com.contaremunerada.conta.domain.Conta;
import br.com.contaremunerada.conta.repository.ContaRepository;
import br.com.contaremunerada.conta.service.DepositarService;
import br.com.contaremunerada.conta.service.SacarService;

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
public class SacarServiceIntegrationTest {

  @Container
  @ServiceConnection
  static PostgreSQLContainer postgres =
      new PostgreSQLContainer("postgres:18-alpine");

  @Autowired
  private ContaRepository contaRepository;

  @Autowired
  private DepositarService depositarService;

  @Autowired
  private SacarService sacarService;

  @Autowired
  private JdbcTemplate jdbcTemplate;

  @Test
  void devePersistirSaqueEManterConsistenciaFinanceira() {

    // Arrange
    Conta conta = contaRepository.saveAndFlush(
        new Conta(
            "CR-TESTE-SAQ-001",
            "Maria Silva",
            "hash-senha-teste"
        )
    );

    Long contaId = conta.getId();

    depositarService.depositar(contaId, "200.00");

    // Act
    sacarService.sacar(contaId, "75.50");

    // Assert: saldo persistido
    BigDecimal saldo = jdbcTemplate.queryForObject(
        "SELECT saldo FROM contas WHERE id = ?",
        BigDecimal.class,
        contaId
    );

    assertNotNull(saldo);

    assertEquals(
        0,
        new BigDecimal("124.50").compareTo(saldo)
    );

    // Assert: exatamente um lançamento de saque
    Integer quantidadeSaques = jdbcTemplate.queryForObject(
        """
        SELECT COUNT(*)
        FROM lancamentos
        WHERE conta_id = ?
          AND tipo = 'SAQUE'
        """,
        Integer.class,
        contaId
    );

    assertEquals(1, quantidadeSaques);

    // Assert: valor negativo
    BigDecimal valorSaque = jdbcTemplate.queryForObject(
        """
        SELECT valor
        FROM lancamentos
        WHERE conta_id = ?
          AND tipo = 'SAQUE'
        """,
        BigDecimal.class,
        contaId
    );

    assertNotNull(valorSaque);

    assertEquals(
        0,
        new BigDecimal("-75.50").compareTo(valorSaque)
    );

    // Assert: RN-03
    BigDecimal somaLancamentos = jdbcTemplate.queryForObject(
        """
        SELECT COALESCE(SUM(valor), 0.00)
        FROM lancamentos
        WHERE conta_id = ?
        """,
        BigDecimal.class,
        contaId
    );

    assertNotNull(somaLancamentos);

    assertEquals(
        0,
        saldo.compareTo(somaLancamentos)
    );
  }


  @Test
  void deveManterConsistenciaAposTresMovimentacoes() {

    Conta conta = contaRepository.saveAndFlush(
        new Conta(
            "CR-TESTE-SEQUENCIA-001",
            "Maria Silva",
            "hash-senha-teste"
        )
    );

    Long contaId = conta.getId();

    depositarService.depositar(contaId, "200.00");
    sacarService.sacar(contaId, "75.50");
    depositarService.depositar(contaId, "10.25");

    BigDecimal saldo = jdbcTemplate.queryForObject(
        "SELECT saldo FROM contas WHERE id = ?",
        BigDecimal.class,
        contaId
    );

    BigDecimal somaLancamentos = jdbcTemplate.queryForObject(
        """
        SELECT COALESCE(SUM(valor), 0.00)
        FROM lancamentos
        WHERE conta_id = ?
        """,
        BigDecimal.class,
        contaId
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

    assertNotNull(saldo);
    assertNotNull(somaLancamentos);

    assertEquals(
        0,
        new BigDecimal("134.75").compareTo(saldo)
    );

    assertEquals(3, quantidadeLancamentos);

    assertEquals(
        0,
        saldo.compareTo(somaLancamentos)
    );
  }
}
