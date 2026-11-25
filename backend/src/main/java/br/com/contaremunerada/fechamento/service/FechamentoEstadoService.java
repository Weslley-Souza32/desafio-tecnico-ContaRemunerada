
package br.com.contaremunerada.fechamento.service;

import br.com.contaremunerada.fechamento.domain.Fechamento;
import br.com.contaremunerada.fechamento.domain.StatusFechamento;
import br.com.contaremunerada.fechamento.repository.FechamentoRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
public class FechamentoEstadoService {

  private final FechamentoRepository fechamentoRepository;

  public FechamentoEstadoService(
      FechamentoRepository fechamentoRepository
  ) {
    this.fechamentoRepository = fechamentoRepository;
  }

  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public void iniciar(LocalDate dataReferencia) {

    int registrosAfetados =
        fechamentoRepository.tentarIniciar(dataReferencia);

    if (registrosAfetados == 0) {
      throw new IllegalStateException(
          "Já existe um fechamento em andamento para esta data."
      );
    }

    if (registrosAfetados != 1) {
      throw new IllegalStateException(
          "Resultado inesperado ao iniciar o fechamento."
      );
    }
  }


  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public void atualizarProgresso(LocalDate dataReferencia, int totalProcessadas) {

    Fechamento fechamento = fechamentoRepository
        .findById(dataReferencia)
        .orElseThrow(() -> new IllegalStateException(
            "Fechamento não encontrado."
        ));

    fechamento.atualizarProgresso(totalProcessadas);
  }


  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public void concluir(LocalDate dataReferencia) {

    Fechamento fechamento = fechamentoRepository
        .findById(dataReferencia)
        .orElseThrow(() -> new IllegalStateException(
            "Fechamento não encontrado."
        ));

    if (fechamento.getStatus()
        != StatusFechamento.EM_ANDAMENTO) {
      throw new IllegalStateException(
          "Somente um fechamento em andamento pode ser concluído."
      );
    }

    fechamento.concluir();
  }


  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public void registrarErro(LocalDate dataReferencia) {

    Fechamento fechamento = fechamentoRepository
        .findById(dataReferencia)
        .orElseThrow(() -> new IllegalStateException(
            "Fechamento não encontrado."
        ));

    if (fechamento.getStatus()
        != StatusFechamento.EM_ANDAMENTO) {
      throw new IllegalStateException(
          "Somente um fechamento em andamento pode registrar erro."
      );
    }

    fechamento.registrarErro();
  }
}
