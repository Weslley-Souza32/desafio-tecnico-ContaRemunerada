
package br.com.contaremunerada.lancamento.service;

import br.com.contaremunerada.lancamento.domain.Lancamento;
import br.com.contaremunerada.lancamento.dto.ExtratoResponse;
import br.com.contaremunerada.lancamento.dto.LancamentoResponse;
import br.com.contaremunerada.lancamento.repository.LancamentoRepository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.RoundingMode;
import java.util.List;

@Service
public class ConsultarExtratoService {

  private final LancamentoRepository lancamentoRepository;

  public ConsultarExtratoService(
      LancamentoRepository lancamentoRepository
  ) {
    this.lancamentoRepository = lancamentoRepository;
  }

  @Transactional(readOnly = true)
  public ExtratoResponse consultar(
      Long contaId,
      int page,
      int size
  ) {
    if (page < 0 || size < 1 || size > 100) {
      throw new IllegalArgumentException(
          "Os parâmetros de paginação são inválidos."
      );
    }

    Pageable pageable = PageRequest.of(
        page,
        size,
        Sort.by(
            Sort.Order.desc("criadoEm"),
            Sort.Order.desc("id")
        )
    );

    Page<Lancamento> resultado =
        lancamentoRepository.findByContaId(
            contaId,
            pageable
        );

    List<LancamentoResponse> lancamentos =
        resultado.getContent()
            .stream()
            .map(this::converterParaResponse)
            .toList();

    return new ExtratoResponse(
        lancamentos,
        resultado.getNumber(),
        resultado.getSize(),
        resultado.getTotalElements(),
        resultado.getTotalPages()
    );
  }

  private LancamentoResponse converterParaResponse(Lancamento lancamento) {

    String valorFormatado = lancamento.getValor()
        .setScale(2, RoundingMode.UNNECESSARY)
        .toPlainString();

    return new LancamentoResponse(
        lancamento.getTipo(),
        lancamento.getCriadoEm(),
        valorFormatado
    );
  }
}