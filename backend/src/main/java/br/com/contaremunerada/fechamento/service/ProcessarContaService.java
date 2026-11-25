package br.com.contaremunerada.fechamento.service;

import br.com.contaremunerada.conta.domain.Conta;
import br.com.contaremunerada.conta.repository.ContaRepository;
import br.com.contaremunerada.fechamento.domain.ProcessamentoConta;
import br.com.contaremunerada.fechamento.domain.ProcessamentoContaId;
import br.com.contaremunerada.fechamento.repository.ProcessamentoContaRepository;
import br.com.contaremunerada.lancamento.domain.Lancamento;
import br.com.contaremunerada.lancamento.domain.TipoLancamento;
import br.com.contaremunerada.lancamento.repository.LancamentoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;

@Service
public class ProcessarContaService {

  private final ContaRepository contaRepository;
  private final ProcessamentoContaRepository processamentoContaRepository;
  private final LancamentoRepository lancamentoRepository;
  private final CalcularJurosService calcularJurosService;

  public ProcessarContaService(ContaRepository contaRepository, ProcessamentoContaRepository processamentoContaRepository, LancamentoRepository lancamentoRepository, CalcularJurosService calcularJurosService) {
    this.contaRepository = contaRepository;
    this.processamentoContaRepository = processamentoContaRepository;
    this.lancamentoRepository = lancamentoRepository;
    this.calcularJurosService = calcularJurosService;
  }

  @Transactional
  public void processar(Long contaId, LocalDate dataReferencia, BigDecimal taxaDiaria) {

    Conta conta = contaRepository.buscarPorIdComBloqueio(contaId)
        .orElseThrow(() -> new IllegalStateException(
            "Conta não encontrada durante o fechamento."
        ));

    ProcessamentoContaId processamentoId =   new ProcessamentoContaId(contaId, dataReferencia);

    if (processamentoContaRepository.existsById(processamentoId)) {
      return;
    }

    BigDecimal juros = calcularJurosService.calcularJuros(conta.getSaldo(), taxaDiaria);

    processamentoContaRepository.save(new ProcessamentoConta(contaId, dataReferencia));

    if (juros.signum() > 0){
      conta.creditar(juros);

      lancamentoRepository.save(
          new Lancamento(conta, TipoLancamento.JUROS, juros)
      );
    }
  }
}
