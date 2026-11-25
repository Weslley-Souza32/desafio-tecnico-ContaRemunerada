
package br.com.contaremunerada.integration;

import br.com.contaremunerada.fechamento.service.FechamentoEstadoService;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.jdbc.core.JdbcTemplate;

import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(properties = {
    "DB_URL=jdbc:postgresql://127.0.0.1:1/nao_usar",
    "DB_USERNAME=nao_usar",
    "DB_PASSWORD=nao_usar",
    "JWT_SECRET_BASE64=MDEyMzQ1Njc4OWFiY2RlZjAxMjM0NTY3ODlhYmNkZWY="
})
@Testcontainers
public class FechamentoEstadoServiceIntegrationTest {

  @Container
  @ServiceConnection
  static PostgreSQLContainer postgres =
      new PostgreSQLContainer("postgres:18-alpine");

  @Autowired
  private FechamentoEstadoService fechamentoEstadoService;

  @Autowired
  private JdbcTemplate jdbcTemplate;

  @Test
  void devePersistirErroEProgressoDoFechamento() {

    LocalDate dataReferencia = LocalDate.of(2026, 11, 20);

    // Inicia e confirma o fechamento
    fechamentoEstadoService.iniciar(dataReferencia);

    // Simula uma conta processada com sucesso
    fechamentoEstadoService.atualizarProgresso(
        dataReferencia, 1
    );

    // Simula a transição após uma falha
    fechamentoEstadoService.registrarErro(dataReferencia);

    String status = jdbcTemplate.queryForObject(
        """
        SELECT status
        FROM fechamentos
        WHERE data_referencia = ?
        """,
        String.class,
        dataReferencia
    );

    Integer contasProcessadas = jdbcTemplate.queryForObject(
        """
        SELECT contas_processadas
        FROM fechamentos
        WHERE data_referencia = ?
        """,
        Integer.class,
        dataReferencia
    );

    Integer finalizados = jdbcTemplate.queryForObject(
        """
        SELECT COUNT(*)
        FROM fechamentos
        WHERE data_referencia = ?
          AND finalizado_em IS NOT NULL
        """,
        Integer.class,
        dataReferencia
    );

    assertEquals("ERRO", status);
    assertEquals(1, contasProcessadas);
    assertEquals(1, finalizados);
  }


  @Test
  void devePermitirReiniciarFechamentoComErro() {

    LocalDate dataReferencia = LocalDate.of(2026, 11, 19);

    // Primeira execução
    fechamentoEstadoService.iniciar(dataReferencia);

    fechamentoEstadoService.atualizarProgresso(
        dataReferencia, 2
    );

    fechamentoEstadoService.registrarErro(dataReferencia);

    // Reinicia a mesma data
    fechamentoEstadoService.iniciar(dataReferencia);

    String status = jdbcTemplate.queryForObject(
        """
        SELECT status
        FROM fechamentos
        WHERE data_referencia = ?
        """,
        String.class,
        dataReferencia
    );

    Integer contasProcessadas = jdbcTemplate.queryForObject(
        """
        SELECT contas_processadas
        FROM fechamentos
        WHERE data_referencia = ?
        """,
        Integer.class,
        dataReferencia
    );

    Integer finalizados = jdbcTemplate.queryForObject(
        """
        SELECT COUNT(*)
        FROM fechamentos
        WHERE data_referencia = ?
          AND finalizado_em IS NOT NULL
        """,
        Integer.class,
        dataReferencia
    );

    assertEquals("EM_ANDAMENTO", status);
    assertEquals(0, contasProcessadas);
    assertEquals(0, finalizados);
  }

}
