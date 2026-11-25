package br.com.contaremunerada.taxa.service;

import br.com.contaremunerada.taxa.domain.TaxaDiaria;
import br.com.contaremunerada.taxa.dto.TaxaDiariaResponse;
import br.com.contaremunerada.taxa.repository.TaxaDiariaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Optional;

@Service
public class TaxaDiariaService {

  private static final short TAXA_ID = 1;

  private final TaxaDiariaRepository taxaDiariaRepository;

  public TaxaDiariaService(TaxaDiariaRepository taxaDiariaRepository) {
    this.taxaDiariaRepository = taxaDiariaRepository;
  }

  @Transactional
  public TaxaDiariaResponse configurar(String taxaComoTexto) {

    BigDecimal novaTaxa =validarTaxa(taxaComoTexto);

    TaxaDiaria taxaDiaria = taxaDiariaRepository.findById(TAXA_ID)
        .orElseGet(() -> new TaxaDiaria(novaTaxa));

    taxaDiaria.atualizarTaxa(novaTaxa);

    TaxaDiaria taxaSalva = taxaDiariaRepository.save(taxaDiaria);

    return converterParaResponse(taxaSalva);
  }

  @Transactional(readOnly = true)
  public Optional<TaxaDiariaResponse> consultarTaxa() {

    return taxaDiariaRepository.findById(TAXA_ID)
        .map(this::converterParaResponse);
  }

  private BigDecimal validarTaxa(String valor) {

    if (valor == null || !valor.matches("[0-9]+(\\.[0-9]+)?")) {

      throw new IllegalArgumentException("A taxa diária informada é inválida.");
    }

    BigDecimal taxa = new BigDecimal(valor);

    try {
      taxa = taxa.setScale(10, RoundingMode.UNNECESSARY);
    } catch (ArithmeticException exception) {
      throw new IllegalArgumentException("A taxa diária excede a precisão permitida.",  exception);
    }

    return taxa;
  }

  private TaxaDiariaResponse converterParaResponse(TaxaDiaria taxaDiaria) {

    String valor = taxaDiaria.getTaxaDiaria()
        .stripTrailingZeros()
        .toPlainString();

    return new TaxaDiariaResponse(valor);
  }
}
