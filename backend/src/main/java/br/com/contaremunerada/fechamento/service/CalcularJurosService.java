package br.com.contaremunerada.fechamento.service;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
public class CalcularJurosService {

  public BigDecimal calcularJuros(BigDecimal saldo, BigDecimal taxaDiaria) {

    if (saldo == null || taxaDiaria == null) {
      return BigDecimal.ZERO.setScale(2);
    }

    if (saldo.signum() <= 0 || taxaDiaria.signum() <= 0) {
      return BigDecimal.ZERO.setScale(2);
    }

    return saldo.multiply(taxaDiaria)
        .setScale(2, RoundingMode.HALF_EVEN);
  }
}
