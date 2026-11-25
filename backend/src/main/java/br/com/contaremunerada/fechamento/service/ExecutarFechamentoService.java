package br.com.contaremunerada.fechamento.service;

import br.com.contaremunerada.conta.repository.ContaRepository;
import br.com.contaremunerada.fechamento.dto.FechamentoResponse;
import br.com.contaremunerada.fechamento.repository.FechamentoRepository;
import br.com.contaremunerada.taxa.domain.TaxaDiaria;
import br.com.contaremunerada.taxa.repository.TaxaDiariaRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDate;

@Service
public class ExecutarFechamentoService {

  private static final short TAXA_ID = 1;
  private static final int TAMANHO_LOTE = 100;

  private final TaxaDiariaRepository taxaDiariaRepository;
  private final FechamentoEstadoService fechamentoEstadoService;
  private final ContaRepository contaRepository;
  private final ProcessarContaService processarContaService;
  private final FechamentoRepository fechamentoRepository;

  public ExecutarFechamentoService(TaxaDiariaRepository taxaDiariaRepository, FechamentoEstadoService fechamentoEstadoService, ContaRepository contaRepository, ProcessarContaService processarContaService, FechamentoRepository fechamentoRepository) {
    this.taxaDiariaRepository = taxaDiariaRepository;
    this.fechamentoEstadoService = fechamentoEstadoService;
    this.contaRepository = contaRepository;
    this.processarContaService = processarContaService;
    this.fechamentoRepository = fechamentoRepository;
  }

  public FechamentoResponse executar(LocalDate dataReferencia) {

    BigDecimal taxaVigente = prepararExecucao(dataReferencia);

    try {
      processarConta(dataReferencia, taxaVigente);

      fechamentoEstadoService.concluir(dataReferencia);
    } catch (RuntimeException exception) {

      try {
        fechamentoEstadoService.registrarErro(dataReferencia);
      } catch (RuntimeException erroAoRegistrarEstado) {
        exception.addSuppressed(erroAoRegistrarEstado);
      }

      throw exception;
    }

    return fechamentoRepository.findById(dataReferencia)
        .map(FechamentoResponse::from)
        .orElseThrow(() -> new IllegalStateException(
            "Fechamento não encontrado após a execução."
        ));
  }

  private void validarData(LocalDate dateReferencia) {

    if (dateReferencia == null) {
      throw new IllegalArgumentException("A data de referência é obrigatória.");
    }

    if (dateReferencia.isAfter(LocalDate.now())) {
      throw new ResponseStatusException(
          HttpStatus.UNPROCESSABLE_ENTITY,
          "Não é permitido executar fechamento para uma data futura."
      );
    }
  }

  private BigDecimal consultarTaxaAtual() {
    return taxaDiariaRepository.findById(TAXA_ID)
        .map(TaxaDiaria::getTaxaDiaria)
        .orElse(null);
  }


  private BigDecimal prepararExecucao(LocalDate dataReferencia) {

    validarData(dataReferencia);

    BigDecimal taxaVigente = consultarTaxaAtual();

    fechamentoEstadoService.iniciar(dataReferencia);

    return taxaVigente;
  }

  private void processarConta(LocalDate dataReferencia, BigDecimal taxaVigente) {
    Long maiorId = contaRepository.buscarMaiorId();

    if (maiorId == null) {
      return;
    }

    long ultimoId = 0L;
    int totalProcessadas = 0;

    Pageable lote = PageRequest.of(0, TAMANHO_LOTE);

    while (true) {
      Slice<Long> contas = contaRepository.listarIdsParaFechamento(
          ultimoId, maiorId, lote
      );

      if (contas.isEmpty()) {
        break;
      }

      for (Long contaId : contas.getContent()) {

        try {
          processarContaService.processar(contaId, dataReferencia, taxaVigente);

          ultimoId = contaId;
          totalProcessadas++;
        } catch (RuntimeException exception) {

          try {
            fechamentoEstadoService.atualizarProgresso(dataReferencia, totalProcessadas);
          } catch (RuntimeException erroAoAtualizarProgresso) {
            exception.addSuppressed(erroAoAtualizarProgresso);
          }

          throw exception;
        }
      }

      fechamentoEstadoService.atualizarProgresso(dataReferencia, totalProcessadas);

      if (!contas.hasNext()) {
        break;
      }
    }
  }
}
