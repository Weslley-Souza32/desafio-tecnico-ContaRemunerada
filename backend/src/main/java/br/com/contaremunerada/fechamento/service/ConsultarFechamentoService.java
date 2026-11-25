
package br.com.contaremunerada.fechamento.service;

import br.com.contaremunerada.fechamento.dto.FechamentoResponse;
import br.com.contaremunerada.fechamento.repository.FechamentoRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Optional;

@Service
public class ConsultarFechamentoService {

  private final FechamentoRepository fechamentoRepository;

  public ConsultarFechamentoService(FechamentoRepository fechamentoRepository) {
    this.fechamentoRepository = fechamentoRepository;
  }

  @Transactional(readOnly = true)
  public Optional<FechamentoResponse> consultar(
      LocalDate dataReferencia
  ) {
    return fechamentoRepository.findById(dataReferencia)
        .map(FechamentoResponse::from);
  }
}
