
package br.com.contaremunerada.integration;

import br.com.contaremunerada.conta.domain.Conta;
import br.com.contaremunerada.conta.repository.ContaRepository;
import br.com.contaremunerada.conta.service.DepositarService;
import br.com.contaremunerada.conta.service.SacarService;
import br.com.contaremunerada.fechamento.dto.FechamentoResponse;
import br.com.contaremunerada.fechamento.service.ExecutarFechamentoService;
import br.com.contaremunerada.fechamento.service.FechamentoEstadoService;
import br.com.contaremunerada.taxa.service.TaxaDiariaService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.transaction.PlatformTransactionManager;

import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.concurrent.*;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(properties = {
    "DB_URL=jdbc:postgresql://127.0.0.1:1/nao_usar",
    "DB_USERNAME=nao_usar",
    "DB_PASSWORD=nao_usar",
    "JWT_SECRET_BASE64=MDEyMzQ1Njc4OWFiY2RlZjAxMjM0NTY3ODlhYmNkZWY="
})
@Testcontainers
class FechamentoIdempotenciaIntegrationTest {

  @Container
  @ServiceConnection
  static PostgreSQLContainer postgres =
      new PostgreSQLContainer("postgres:18-alpine");

  @Autowired
  private ContaRepository contaRepository;

  @Autowired
  private DepositarService depositarService;

  @Autowired
  private TaxaDiariaService taxaDiariaService;

  @Autowired
  private ExecutarFechamentoService executarFechamentoService;

  @Autowired
  private JdbcTemplate jdbcTemplate;

  @Autowired
  private PlatformTransactionManager transactionManager;

  @Autowired
  private SacarService sacarService;

  @Autowired
  private FechamentoEstadoService fechamentoEstadoService;

  @BeforeEach
  void limparDadosAntesDeCadaTeste() {

    jdbcTemplate.execute("""
        TRUNCATE TABLE
            processamentos_conta,
            fechamentos,
            lancamentos,
            contas,
            taxa_diaria
        RESTART IDENTITY CASCADE
        """);
  }


  @Test
  void deveEvitarJurosDuplicadosEPermitirNovaData() {

    // Arrange
    Conta conta = contaRepository.saveAndFlush(
        new Conta(
            "CR-IDEMP-001",
            "Cliente Teste",
            "hash-senha-teste"
        )
    );

    Long contaId = conta.getId();

    depositarService.depositar(contaId, "1000.00");
    taxaDiariaService.configurar("0.0003");

    LocalDate primeiraData = LocalDate.now().minusDays(2);
    LocalDate segundaData = LocalDate.now().minusDays(1);

    // Act 1: primeiro fechamento
    var primeiro = executarFechamentoService.executar(primeiraData);

    assertEquals("CONCLUIDO", primeiro.status().name());
    assertEquals(1, primeiro.contasProcessadas());

    verificarSaldo(contaId, "1000.30");

    // Act 2: repetir a mesma data
    var repetido = executarFechamentoService.executar(primeiraData);

    assertEquals("CONCLUIDO", repetido.status().name());
    assertEquals(1, repetido.contasProcessadas());

    verificarSaldo(contaId, "1000.30");

    // Act 3: fechar outra data
    var segundo = executarFechamentoService.executar(segundaData);

    assertEquals("CONCLUIDO", segundo.status().name());
    assertEquals(1, segundo.contasProcessadas());

    verificarSaldo(contaId, "1000.60");

    // Assert: somente dois lançamentos de juros
    Integer quantidadeJuros = jdbcTemplate.queryForObject(
        """
        SELECT COUNT(*)
        FROM lancamentos
        WHERE conta_id = ?
          AND tipo = 'JUROS'
        """,
        Integer.class,
        contaId
    );

    assertEquals(2, quantidadeJuros);

    // Assert: um processamento por data
    Integer quantidadeProcessamentos =
        jdbcTemplate.queryForObject(
            """
            SELECT COUNT(*)
            FROM processamentos_conta
            WHERE conta_id = ?
            """,
            Integer.class,
            contaId
        );

    assertEquals(2, quantidadeProcessamentos);

    // Assert: RN-03
    BigDecimal somaLancamentos = jdbcTemplate.queryForObject(
        """
        SELECT COALESCE(SUM(valor), 0)
        FROM lancamentos
        WHERE conta_id = ?
        """,
        BigDecimal.class,
        contaId
    );

    assertNotNull(somaLancamentos);
    assertEquals(
        0,
        new BigDecimal("1000.60")
            .compareTo(somaLancamentos)
    );
  }


  @Test
  void naoDeveDuplicarJurosEmFechamentosConcorrentes()
      throws Exception {

    // Arrange
    Conta conta = contaRepository.saveAndFlush(
        new Conta(
            "CR-CONCORRENTE-001",
            "Cliente Concorrente",
            "hash-senha-teste"
        )
    );

    Long contaId = conta.getId();

    depositarService.depositar(contaId, "1000.00");
    taxaDiariaService.configurar("0.0003");

    LocalDate data = LocalDate.now().minusDays(3);

    ExecutorService executor =
        Executors.newFixedThreadPool(2);

    CountDownLatch inicio = new CountDownLatch(1);

    try {
      // Act: preparar duas execuções
      Future<?> primeira = executor.submit(() -> {
        inicio.await();
        return executarFechamentoService.executar(data);
      });

      Future<?> segunda = executor.submit(() -> {
        inicio.await();
        return executarFechamentoService.executar(data);
      });

      // Liberar as duas threads juntas
      inicio.countDown();

      int execucoesConcluidas = 0;

      for (Future<?> execucao : new Future<?>[]{
          primeira, segunda
      }) {
        try {
          execucao.get(30, TimeUnit.SECONDS);
          execucoesConcluidas++;

        } catch (ExecutionException exception) {

          // A outra execução pode encontrar
          // um fechamento já EM_ANDAMENTO.
          assertInstanceOf(
              IllegalStateException.class,
              exception.getCause()
          );
        }
      }

      assertTrue(execucoesConcluidas >= 1);

      // Assert: saldo recebeu juros uma única vez
      BigDecimal saldo = jdbcTemplate.queryForObject(
          "SELECT saldo FROM contas WHERE id = ?",
          BigDecimal.class,
          contaId
      );

      assertNotNull(saldo);

      assertEquals(
          0,
          new BigDecimal("1000.30").compareTo(saldo)
      );

      // Assert: somente um lançamento JUROS
      Integer totalJuros = jdbcTemplate.queryForObject(
          """
          SELECT COUNT(*)
          FROM lancamentos
          WHERE conta_id = ?
            AND tipo = 'JUROS'
          """,
          Integer.class,
          contaId
      );

      assertEquals(1, totalJuros);

      // Assert: somente um processamento conta/data
      Integer totalProcessamentos = jdbcTemplate.queryForObject(
          """
          SELECT COUNT(*)
          FROM processamentos_conta
          WHERE conta_id = ?
            AND data_referencia = ?
          """,
          Integer.class,
          contaId,
          data
      );

      assertEquals(1, totalProcessamentos);

      // Assert: RN-03
      BigDecimal somaLancamentos =
          jdbcTemplate.queryForObject(
              """
              SELECT COALESCE(SUM(valor), 0)
              FROM lancamentos
              WHERE conta_id = ?
              """,
              BigDecimal.class,
              contaId
          );

      assertNotNull(somaLancamentos);
      assertEquals(0, saldo.compareTo(somaLancamentos));

    } finally {
      executor.shutdownNow();
    }
  }


  @Test
  void deveImpedirSegundoFechamentoEnquantoPrimeiroEstaEmAndamento()
      throws Exception {

    // Arrange
    Conta conta = contaRepository.saveAndFlush(
        new Conta(
            "CR-BLOQUEIO-001",
            "Cliente Bloqueio",
            "hash-senha-teste"
        )
    );

    Long contaId = conta.getId();

    depositarService.depositar(contaId, "1000.00");
    taxaDiariaService.configurar("0.0003");

    LocalDate data = LocalDate.now().minusDays(4);

    CountDownLatch bloqueioAdquirido = new CountDownLatch(1);
    CountDownLatch liberarBloqueio = new CountDownLatch(1);

    ExecutorService executor = Executors.newFixedThreadPool(3);

    try {
      // Thread 1: mantém a conta bloqueada
      Future<?> bloqueio = executor.submit(() ->
          bloquearContaAteLiberacao(
              contaId,
              bloqueioAdquirido,
              liberarBloqueio
          )
      );

      assertTrue(
          bloqueioAdquirido.await(10, TimeUnit.SECONDS),
          "O bloqueio da conta não foi adquirido."
      );

      // Thread 2: primeiro fechamento
      Future<FechamentoResponse> primeiro = executor.submit(() ->
          executarFechamentoService.executar(data)
      );

      // Aguarda o fechamento ficar EM_ANDAMENTO
      boolean emAndamento = false;

      long limite = System.nanoTime()
          + TimeUnit.SECONDS.toNanos(10);

      while (System.nanoTime() < limite) {

        var estados = jdbcTemplate.queryForList(
            """
            SELECT status
            FROM fechamentos
            WHERE data_referencia = ?
            """,
            String.class,
            data
        );

        if (estados.contains("EM_ANDAMENTO")) {
          emAndamento = true;
          break;
        }

        Thread.sleep(50);
      }

      assertTrue(
          emAndamento,
          "O primeiro fechamento não entrou em EM_ANDAMENTO."
      );

      // Thread 3: tenta iniciar a mesma data
      Future<?> segundo = executor.submit(() ->
          executarFechamentoService.executar(data)
      );

      ExecutionException falha = assertThrows(
          ExecutionException.class,
          () -> segundo.get(10, TimeUnit.SECONDS)
      );

      assertInstanceOf(
          IllegalStateException.class,
          falha.getCause()
      );

      // A segunda execução não pode mudar o estado da primeira
      String estadoDuranteProcessamento =
          jdbcTemplate.queryForObject(
              """
              SELECT status
              FROM fechamentos
              WHERE data_referencia = ?
              """,
              String.class,
              data
          );

      assertEquals("EM_ANDAMENTO", estadoDuranteProcessamento);

      // Libera a conta para o primeiro fechamento terminar
      liberarBloqueio.countDown();

      bloqueio.get(10, TimeUnit.SECONDS);

      var resposta = primeiro.get(10, TimeUnit.SECONDS);

      assertEquals("CONCLUIDO", resposta.status().name());
      assertEquals(1, resposta.contasProcessadas());

      // RN-03: saldo esperado
      verificarSaldo(contaId, "1000.30");

      // RN-06: um único lançamento JUROS
      Integer totalJuros = jdbcTemplate.queryForObject(
          """
          SELECT COUNT(*)
          FROM lancamentos
          WHERE conta_id = ?
            AND tipo = 'JUROS'
          """,
          Integer.class,
          contaId
      );

      assertEquals(1, totalJuros);

      // RN-06: um processamento por conta/data
      Integer totalProcessamentos = jdbcTemplate.queryForObject(
          """
          SELECT COUNT(*)
          FROM processamentos_conta
          WHERE conta_id = ?
            AND data_referencia = ?
          """,
          Integer.class,
          contaId,
          data
      );

      assertEquals(1, totalProcessamentos);

      // RN-03: saldo igual à soma dos lançamentos
      BigDecimal saldo = jdbcTemplate.queryForObject(
          "SELECT saldo FROM contas WHERE id = ?",
          BigDecimal.class,
          contaId
      );

      BigDecimal somaLancamentos = jdbcTemplate.queryForObject(
          """
          SELECT COALESCE(SUM(valor), 0)
          FROM lancamentos
          WHERE conta_id = ?
          """,
          BigDecimal.class,
          contaId
      );

      assertNotNull(saldo);
      assertNotNull(somaLancamentos);
      assertEquals(0, saldo.compareTo(somaLancamentos));

    } finally {
      // Evita deixar o teste bloqueado em caso de falha
      liberarBloqueio.countDown();
      executor.shutdownNow();
    }
  }


  @Test
  void deveReverterOperacaoFinanceiraQuandoLancamentoFalhar() {

    // Arrange
    Conta conta = contaRepository.saveAndFlush(
        new Conta(
            "CR-ROLLBACK-001",
            "Cliente Rollback",
            "hash-senha-teste"
        )
    );

    Long contaId = conta.getId();

    depositarService.depositar(contaId, "1000.00");
    taxaDiariaService.configurar("0.0003");

    LocalDate data = LocalDate.now().minusDays(5);

    // Cria uma falha controlada no PostgreSQL.
    jdbcTemplate.execute("""
        CREATE OR REPLACE FUNCTION rejeitar_juros_teste()
        RETURNS trigger
        LANGUAGE plpgsql
        AS $$
        BEGIN
            IF NEW.tipo = 'JUROS' THEN
                RAISE EXCEPTION 'Falha simulada ao gravar juros';
            END IF;

            RETURN NEW;
        END;
        $$
        """);

    jdbcTemplate.execute("""
        CREATE TRIGGER trigger_rejeitar_juros_teste
        BEFORE INSERT ON lancamentos
        FOR EACH ROW
        EXECUTE FUNCTION rejeitar_juros_teste()
        """);

    try {
      // Act: o banco rejeita o lançamento de juros.
      assertThrows(
          RuntimeException.class,
          () -> executarFechamentoService.executar(data)
      );

      // Assert: saldo não foi creditado.
      verificarSaldo(contaId, "1000.00");

      Integer juros = jdbcTemplate.queryForObject(
          """
          SELECT COUNT(*)
          FROM lancamentos
          WHERE conta_id = ?
            AND tipo = 'JUROS'
          """,
          Integer.class,
          contaId
      );

      assertEquals(0, juros);

      // O registro de processamento também deve
      // ser revertido pela mesma transação.
      Integer processamentos = jdbcTemplate.queryForObject(
          """
          SELECT COUNT(*)
          FROM processamentos_conta
          WHERE conta_id = ?
            AND data_referencia = ?
          """,
          Integer.class,
          contaId,
          data
      );

      assertEquals(0, processamentos);

      String status = jdbcTemplate.queryForObject(
          """
          SELECT status
          FROM fechamentos
          WHERE data_referencia = ?
          """,
          String.class,
          data
      );

      assertEquals("ERRO", status);

    } finally {
      // Remove a falha simulada mesmo se o teste falhar.
      jdbcTemplate.execute("""
            DROP TRIGGER IF EXISTS
                trigger_rejeitar_juros_teste
            ON lancamentos
            """);

      jdbcTemplate.execute("""
            DROP FUNCTION IF EXISTS rejeitar_juros_teste()
            """);
    }

    // Act: agora o banco permite o lançamento.
    var resposta = executarFechamentoService.executar(data);

    // Assert: a reexecução recupera a conta.
    assertEquals("CONCLUIDO", resposta.status().name());
    assertEquals(1, resposta.contasProcessadas());

    verificarSaldo(contaId, "1000.30");

    Integer jurosAposRecuperacao = jdbcTemplate.queryForObject(
        """
        SELECT COUNT(*)
        FROM lancamentos
        WHERE conta_id = ?
          AND tipo = 'JUROS'
        """,
        Integer.class,
        contaId
    );

    Integer processamentosAposRecuperacao =
        jdbcTemplate.queryForObject(
            """
            SELECT COUNT(*)
            FROM processamentos_conta
            WHERE conta_id = ?
              AND data_referencia = ?
            """,
            Integer.class,
            contaId,
            data
        );

    assertEquals(1, jurosAposRecuperacao);
    assertEquals(1, processamentosAposRecuperacao);
  }


  @Test
  void deveManterConsistenciaEntreFechamentoESaque()
      throws Exception {

    // Arrange
    Conta conta = contaRepository.saveAndFlush(
        new Conta(
            "CR-SAQUE-CONCORRENTE-001",
            "Cliente Concorrente",
            "hash-senha-teste"
        )
    );

    Long contaId = conta.getId();

    depositarService.depositar(contaId, "1000.00");
    taxaDiariaService.configurar("0.0003");

    LocalDate data = LocalDate.now().minusDays(6);

    CountDownLatch inicio = new CountDownLatch(1);

    ExecutorService executor =
        Executors.newFixedThreadPool(2);

    try {
      // Thread 1: fechamento diário
      Future<FechamentoResponse> fechamento =
          executor.submit(() -> {
            inicio.await();

            return executarFechamentoService.executar(data);
          });

      // Thread 2: saque de R$ 200,00
      Future<?> saque = executor.submit(() -> {
        inicio.await();

        sacarService.sacar(contaId, "200.00");

        return null;
      });

      // Libera as duas operações
      inicio.countDown();

      FechamentoResponse resposta =
          fechamento.get(30, TimeUnit.SECONDS);

      saque.get(30, TimeUnit.SECONDS);

      // Assert: fechamento concluído
      assertEquals(
          "CONCLUIDO",
          resposta.status().name()
      );

      assertEquals(1, resposta.contasProcessadas());

      BigDecimal saldo = jdbcTemplate.queryForObject(
          "SELECT saldo FROM contas WHERE id = ?",
          BigDecimal.class,
          contaId
      );

      assertNotNull(saldo);

      // Ambos os resultados são financeiramente válidos.
      boolean saldoValido =
          saldo.compareTo(new BigDecimal("800.30")) == 0
              || saldo.compareTo(new BigDecimal("800.24")) == 0;

      assertTrue(
          saldoValido,
          "Saldo inesperado após operações concorrentes: "
              + saldo
      );

      // Assert: somente um saque
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

      // Assert: somente um crédito de juros
      Integer quantidadeJuros = jdbcTemplate.queryForObject(
          """
          SELECT COUNT(*)
          FROM lancamentos
          WHERE conta_id = ?
            AND tipo = 'JUROS'
          """,
          Integer.class,
          contaId
      );

      assertEquals(1, quantidadeJuros);

      // Assert: um processamento para a data
      Integer processamentos = jdbcTemplate.queryForObject(
          """
          SELECT COUNT(*)
          FROM processamentos_conta
          WHERE conta_id = ?
            AND data_referencia = ?
          """,
          Integer.class,
          contaId,
          data
      );

      assertEquals(1, processamentos);

      // RN-03: saldo = soma dos lançamentos
      BigDecimal somaLancamentos = jdbcTemplate.queryForObject(
          """
          SELECT COALESCE(SUM(valor), 0)
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

    } finally {
      inicio.countDown();
      executor.shutdownNow();
    }
  }

  @Test
  void deveIdentificarFechamentoQuePermaneceuEmAndamento() {

    // Arrange: simula um estado persistido cuja
    // execução original não está mais ocorrendo.
    LocalDate data = LocalDate.now().minusDays(7);

    fechamentoEstadoService.iniciar(data);

    // Act: tenta recuperar a mesma data.
    IllegalStateException exception = assertThrows(
        IllegalStateException.class,
        () -> executarFechamentoService.executar(data)
    );

    // Assert: comportamento atual do sistema.
    assertEquals(
        "Já existe um fechamento em andamento para esta data.",
        exception.getMessage()
    );

    String status = jdbcTemplate.queryForObject(
        """
        SELECT status
        FROM fechamentos
        WHERE data_referencia = ?
        """,
        String.class,
        data
    );

    assertEquals("EM_ANDAMENTO", status);
  }

  @Test
  void naoDeveCreditarJurosAposContaSemSaldoSerProcessada() {

    // Arrange
    Conta conta = contaRepository.saveAndFlush(
        new Conta(
            "CR-SEM-SALDO-001",
            "Cliente Sem Saldo",
            "hash-senha-teste"
        )
    );

    Long contaId = conta.getId();

    taxaDiariaService.configurar("0.0003");

    LocalDate primeiraData = LocalDate.now().minusDays(8);
    LocalDate segundaData = LocalDate.now().minusDays(7);

    // Act 1: processar conta com saldo zero
    var primeiro = executarFechamentoService.executar(primeiraData);

    assertEquals("CONCLUIDO", primeiro.status().name());
    assertEquals(1, primeiro.contasProcessadas());

    verificarSaldo(contaId, "0.00");

    // Act 2: depositar após o primeiro processamento
    depositarService.depositar(contaId, "1000.00");

    // Repetir a primeira data
    var repetido = executarFechamentoService.executar(primeiraData);

    assertEquals("CONCLUIDO", repetido.status().name());
    assertEquals(1, repetido.contasProcessadas());

    // O depósito permanece, mas não há juros retroativos
    verificarSaldo(contaId, "1000.00");

    Integer processamentosPrimeiraData = jdbcTemplate.queryForObject(
        """
        SELECT COUNT(*)
        FROM processamentos_conta
        WHERE conta_id = ?
          AND data_referencia = ?
        """,
        Integer.class,
        contaId,
        primeiraData
    );

    assertEquals(1, processamentosPrimeiraData);

    Integer quantidadeJuros = jdbcTemplate.queryForObject(
        """
        SELECT COUNT(*)
        FROM lancamentos
        WHERE conta_id = ?
          AND tipo = 'JUROS'
        """,
        Integer.class,
        contaId
    );

    assertEquals(0, quantidadeJuros);

    // Act 3: nova data permite juros
    var segundo = executarFechamentoService.executar(segundaData);

    assertEquals("CONCLUIDO", segundo.status().name());
    assertEquals(1, segundo.contasProcessadas());

    verificarSaldo(contaId, "1000.30");

    Integer totalProcessamentos = jdbcTemplate.queryForObject(
        """
        SELECT COUNT(*)
        FROM processamentos_conta
        WHERE conta_id = ?
        """,
        Integer.class,
        contaId
    );

    assertEquals(2, totalProcessamentos);

    Integer totalJuros = jdbcTemplate.queryForObject(
        """
        SELECT COUNT(*)
        FROM lancamentos
        WHERE conta_id = ?
          AND tipo = 'JUROS'
        """,
        Integer.class,
        contaId
    );

    assertEquals(1, totalJuros);
  }

  @Test
  void naoDeveCreditarJurosAposTaxaSerConfigurada() {

    // Arrange
    Conta conta = contaRepository.saveAndFlush(
        new Conta(
            "CR-SEM-TAXA-001",
            "Cliente Sem Taxa",
            "hash-senha-teste"
        )
    );

    Long contaId = conta.getId();

    depositarService.depositar(contaId, "1000.00");

    LocalDate primeiraData = LocalDate.now().minusDays(10);
    LocalDate segundaData = LocalDate.now().minusDays(9);

    // Garante ausência de taxa no primeiro fechamento
    jdbcTemplate.update(
        "DELETE FROM taxa_diaria WHERE id = 1"
    );

    // Act 1: fechamento sem taxa
    var primeiro = executarFechamentoService.executar(primeiraData);

    assertEquals("CONCLUIDO", primeiro.status().name());
    assertEquals(1, primeiro.contasProcessadas());

    verificarSaldo(contaId, "1000.00");

    // A conta deve estar marcada como processada.
    Integer processamentosPrimeiraData =
        jdbcTemplate.queryForObject(
            """
            SELECT COUNT(*)
            FROM processamentos_conta
            WHERE conta_id = ?
              AND data_referencia = ?
            """,
            Integer.class,
            contaId,
            primeiraData
        );

    assertEquals(1, processamentosPrimeiraData);

    // Nenhum lançamento de juros foi criado.
    Integer jurosAntesDaTaxa = jdbcTemplate.queryForObject(
        """
        SELECT COUNT(*)
        FROM lancamentos
        WHERE conta_id = ?
          AND tipo = 'JUROS'
        """,
        Integer.class,
        contaId
    );

    assertEquals(0, jurosAntesDaTaxa);

    // Act 2: configurar taxa posteriormente
    taxaDiariaService.configurar("0.0003");

    // Repetir o fechamento da mesma data
    var repetido = executarFechamentoService.executar(primeiraData);

    assertEquals("CONCLUIDO", repetido.status().name());
    assertEquals(1, repetido.contasProcessadas());

    // Não deve creditar juros retroativos
    verificarSaldo(contaId, "1000.00");

    Integer jurosAposRepeticao = jdbcTemplate.queryForObject(
        """
        SELECT COUNT(*)
        FROM lancamentos
        WHERE conta_id = ?
          AND tipo = 'JUROS'
        """,
        Integer.class,
        contaId
    );

    assertEquals(0, jurosAposRepeticao);

    // Act 3: nova data pode receber juros
    var segundo = executarFechamentoService.executar(segundaData);

    assertEquals("CONCLUIDO", segundo.status().name());
    assertEquals(1, segundo.contasProcessadas());

    verificarSaldo(contaId, "1000.30");

    // Uma conta, duas datas de processamento
    Integer totalProcessamentos =
        jdbcTemplate.queryForObject(
            """
            SELECT COUNT(*)
            FROM processamentos_conta
            WHERE conta_id = ?
            """,
            Integer.class,
            contaId
        );

    assertEquals(2, totalProcessamentos);

    Integer totalJuros = jdbcTemplate.queryForObject(
        """
        SELECT COUNT(*)
        FROM lancamentos
        WHERE conta_id = ?
          AND tipo = 'JUROS'
        """,
        Integer.class,
        contaId
    );

    assertEquals(1, totalJuros);
  }

  @Test
  void naoDeveCreditarJurosAposArredondamentoParaZero() {

    // Arrange
    Conta conta = contaRepository.saveAndFlush(
        new Conta(
            "CR-ARRED-ZERO-001",
            "Cliente Arredondamento",
            "hash-senha-teste"
        )
    );

    Long contaId = conta.getId();

    depositarService.depositar(contaId, "1.00");
    taxaDiariaService.configurar("0.0003");

    LocalDate primeiraData = LocalDate.now().minusDays(12);
    LocalDate segundaData = LocalDate.now().minusDays(11);

    // Act 1: juros calculados arredondam para zero
    var primeiro = executarFechamentoService.executar(primeiraData);

    assertEquals("CONCLUIDO", primeiro.status().name());
    assertEquals(1, primeiro.contasProcessadas());

    verificarSaldo(contaId, "1.00");

    Integer processamentosPrimeiraData =
        jdbcTemplate.queryForObject(
            """
            SELECT COUNT(*)
            FROM processamentos_conta
            WHERE conta_id = ?
              AND data_referencia = ?
            """,
            Integer.class,
            contaId,
            primeiraData
        );

    assertEquals(1, processamentosPrimeiraData);

    Integer jurosPrimeiraData = jdbcTemplate.queryForObject(
        """
        SELECT COUNT(*)
        FROM lancamentos
        WHERE conta_id = ?
          AND tipo = 'JUROS'
        """,
        Integer.class,
        contaId
    );

    assertEquals(0, jurosPrimeiraData);

    // Act 2: aumenta o saldo após o processamento
    depositarService.depositar(contaId, "999.00");

    var repetido = executarFechamentoService.executar(primeiraData);

    assertEquals("CONCLUIDO", repetido.status().name());
    assertEquals(1, repetido.contasProcessadas());

    verificarSaldo(contaId, "1000.00");

    Integer jurosAposRepeticao = jdbcTemplate.queryForObject(
        """
        SELECT COUNT(*)
        FROM lancamentos
        WHERE conta_id = ?
          AND tipo = 'JUROS'
        """,
        Integer.class,
        contaId
    );

    assertEquals(0, jurosAposRepeticao);

    // Act 3: a nova data permite juros
    var segundo = executarFechamentoService.executar(segundaData);

    assertEquals("CONCLUIDO", segundo.status().name());
    assertEquals(1, segundo.contasProcessadas());

    verificarSaldo(contaId, "1000.30");

    Integer totalProcessamentos = jdbcTemplate.queryForObject(
        """
        SELECT COUNT(*)
        FROM processamentos_conta
        WHERE conta_id = ?
        """,
        Integer.class,
        contaId
    );

    Integer totalJuros = jdbcTemplate.queryForObject(
        """
        SELECT COUNT(*)
        FROM lancamentos
        WHERE conta_id = ?
          AND tipo = 'JUROS'
        """,
        Integer.class,
        contaId
    );

    assertEquals(2, totalProcessamentos);
    assertEquals(1, totalJuros);

    // RN-03: saldo deve corresponder aos lançamentos.
    BigDecimal somaLancamentos = jdbcTemplate.queryForObject(
        """
        SELECT COALESCE(SUM(valor), 0)
        FROM lancamentos
        WHERE conta_id = ?
        """,
        BigDecimal.class,
        contaId
    );

    assertNotNull(somaLancamentos);
    assertEquals(
        0,
        new BigDecimal("1000.30").compareTo(somaLancamentos)
    );
  }

  private void verificarSaldo(Long contaId, String esperado) {

    BigDecimal saldo = jdbcTemplate.queryForObject(
        "SELECT saldo FROM contas WHERE id = ?",
        BigDecimal.class,
        contaId
    );

    assertNotNull(saldo);

    assertEquals(
        0,
        new BigDecimal(esperado).compareTo(saldo)
    );
  }


  private void bloquearContaAteLiberacao(
      Long contaId,
      CountDownLatch bloqueioAdquirido,
      CountDownLatch liberarBloqueio
  ) {
    TransactionTemplate transactionTemplate =
        new TransactionTemplate(transactionManager);

    transactionTemplate.executeWithoutResult(status -> {

      jdbcTemplate.queryForObject(
          """
          SELECT id
          FROM contas
          WHERE id = ?
          FOR UPDATE
          """,
          Long.class,
          contaId
      );

      bloqueioAdquirido.countDown();

      try {
        boolean liberado = liberarBloqueio.await(
            20,
            TimeUnit.SECONDS
        );

        if (!liberado) {
          throw new IllegalStateException(
              "Tempo limite para liberar o bloqueio."
          );
        }

      } catch (InterruptedException exception) {
        Thread.currentThread().interrupt();

        throw new IllegalStateException(
            "Teste interrompido.",
            exception
        );
      }
    });
  }
}
