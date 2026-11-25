
package br.com.contaremunerada.integration;

import br.com.contaremunerada.conta.domain.Conta;
import br.com.contaremunerada.conta.exception.SaldoInsuficienteException;
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
import java.util.concurrent.*;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(properties = {
    "DB_URL=jdbc:postgresql://127.0.0.1:1/nao_usar",
    "DB_USERNAME=nao_usar",
    "DB_PASSWORD=nao_usar",
    "JWT_SECRET_BASE64=MDEyMzQ1Njc4OWFiY2RlZjAxMjM0NTY3ODlhYmNkZWY="
})
@Testcontainers
public class SacarConcorrenciaIntegrationTest {

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
  void deveImpedirDoisSaquesSobreOMesmoSaldo()
      throws Exception {

    // Arrange: conta com R$ 100,00
    Conta conta = contaRepository.saveAndFlush(
        new Conta(
            "CR-TESTE-CONCORRENCIA",
            "Maria Silva",
            "hash-senha-teste"
        )
    );

    Long contaId = conta.getId();

    depositarService.depositar(contaId, "100.00");

    ExecutorService executor =
        Executors.newFixedThreadPool(2);

    CountDownLatch preparadas = new CountDownLatch(2);
    CountDownLatch iniciar = new CountDownLatch(1);

    Callable<Boolean> tentarSacar = () -> {
      preparadas.countDown();

      if (!iniciar.await(10, TimeUnit.SECONDS)) {
        throw new IllegalStateException(
            "Tempo de espera excedido."
        );
      }

      try {
        sacarService.sacar(contaId, "80.00");
        return true;
      } catch (SaldoInsuficienteException exception) {
        return false;
      }
    };

    try {
      Future<Boolean> saqueA =
          executor.submit(tentarSacar);

      Future<Boolean> saqueB =
          executor.submit(tentarSacar);

      // Aguarda as duas threads ficarem prontas
      assertTrue(
          preparadas.await(10, TimeUnit.SECONDS)
      );

      // Libera as duas operações
      iniciar.countDown();

      boolean resultadoA =
          saqueA.get(20, TimeUnit.SECONDS);

      boolean resultadoB =
          saqueB.get(20, TimeUnit.SECONDS);

      int sucessos =
          (resultadoA ? 1 : 0) +
              (resultadoB ? 1 : 0);

      // Exatamente um saque deve ser aprovado
      assertEquals(1, sucessos);

    } finally {
      iniciar.countDown();
      executor.shutdownNow();
    }

    // Assert: consultar o estado persistido
    BigDecimal saldo = jdbcTemplate.queryForObject(
        "SELECT saldo FROM contas WHERE id = ?",
        BigDecimal.class,
        contaId
    );

    assertNotNull(saldo);

    assertEquals(
        0,
        new BigDecimal("20.00").compareTo(saldo)
    );

    Integer quantidadeSaques =
        jdbcTemplate.queryForObject(
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

    BigDecimal somaLancamentos =
        jdbcTemplate.queryForObject(
            """
            SELECT COALESCE(SUM(valor), 0.00)
            FROM lancamentos
            WHERE conta_id = ?
            """,
            BigDecimal.class,
            contaId
        );

    assertNotNull(somaLancamentos);

    // RN-03: saldo = soma dos lançamentos
    assertEquals(
        0,
        saldo.compareTo(somaLancamentos)
    );
  }
}
