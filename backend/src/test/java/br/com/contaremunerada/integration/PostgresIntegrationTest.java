package br.com.contaremunerada.integration;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.jdbc.core.JdbcTemplate;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(properties = {
    "DB_URL=jdbc:postgresql://127.0.0.1:1/nao_usar",
    "DB_USERNAME=nao_usar",
    "DB_PASSWORD=nao_usar",
    "JWT_SECRET_BASE64=MDEyMzQ1Njc4OWFiY2RlZjAxMjM0NTY3ODlhYmNkZWY="
})

@Testcontainers
public class PostgresIntegrationTest {

  @Container
  @ServiceConnection
  static PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:18-alpine");

  @Autowired
  private DataSource dataSource;

  @Autowired
  private JdbcTemplate jdbcTemplate;

  @Test
  void deveUtilizarPostgresTemporario() throws SQLException{
    assertTrue(postgres.isRunning());

    try (Connection connection = dataSource.getConnection()) {
      assertEquals(
          postgres.getJdbcUrl(),
          connection.getMetaData().getURL()
      );
    }
  }

  @Test
  void deveExecutarTodasMigrations() {
    Integer quantidade = jdbcTemplate.queryForObject(
        """
            SELECT COUNT(*)
            FROM flyway_schema_history
            WHERE success = true
            """,
        Integer.class
    );

    assertEquals(5, quantidade);
  }

  @Test
  void deveCriarTabelasFinanceiras() {
    Integer quantidade = jdbcTemplate.queryForObject(
        """
        SELECT COUNT(*)
        FROM information_schema.tables
        WHERE table_schema = 'public'
          AND table_name IN ('contas', 'lancamentos')
        """,
        Integer.class
    );

    assertEquals(2, quantidade);
  }
}
