
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
public class DepositarRollbackIntegrationTest {

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
  void deveReverterSaldoQuandoPersistenciaDoLancamentoFalhar() {

    // Arrange: conta sem movimentações
    Conta conta = contaRepository.saveAndFlush(
        new Conta(
            "CR-TESTE-ROLLBACK",
            "Maria Silva",
            "hash-senha-teste"
        )
    );

    Long contaId = conta.getId();

    // Restringe depósitos somente no banco temporário
    jdbcTemplate.execute("""
                ALTER TABLE lancamentos
                ADD CONSTRAINT ck_teste_rollback
                CHECK (tipo <> 'DEPOSITO')
                """);

    try {
      // Act: a inserção do lançamento deverá falhar
      assertThrows(
          RuntimeException.class,
          () -> depositarService.depositar(
              contaId,
              "150.00"
          )
      );

      // Assert: o saldo não pode ter sido persistido
      BigDecimal saldoPersistido =
          jdbcTemplate.queryForObject(
              """
              SELECT saldo
              FROM contas
              WHERE id = ?
              """,
              BigDecimal.class,
              contaId
          );

      assertNotNull(saldoPersistido);

      assertEquals(
          0,
          BigDecimal.ZERO.compareTo(saldoPersistido)
      );

      // Assert: nenhum lançamento pode ter sido criado
      Integer quantidadeLancamentos =
          jdbcTemplate.queryForObject(
              """
              SELECT COUNT(*)
              FROM lancamentos
              WHERE conta_id = ?
              """,
              Integer.class,
              contaId
          );

      assertEquals(0, quantidadeLancamentos);

    } finally {
      // Remove a restrição temporária
      jdbcTemplate.execute("""
                    ALTER TABLE lancamentos
                    DROP CONSTRAINT ck_teste_rollback
                    """);
    }
  }
}
