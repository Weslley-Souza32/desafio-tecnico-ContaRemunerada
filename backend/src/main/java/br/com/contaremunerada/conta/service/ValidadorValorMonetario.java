package br.com.contaremunerada.conta.service;

import br.com.contaremunerada.conta.exception.ValorInvalidoException;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.regex.Pattern;

@Component
public class ValidadorValorMonetario {

  private static final Pattern FORMATO_VALOR =
      Pattern.compile("[0-9]+(\\.[0-9]{1,2})?");

  public BigDecimal validar(String valor) {
    if (valor == null || !FORMATO_VALOR.matcher(valor).matches()) {
      throw new ValorInvalidoException();
    }

    BigDecimal valorMonetario = new BigDecimal(valor);

    if (valorMonetario.signum() <= 0) {
      throw new ValorInvalidoException();
    }

    return valorMonetario.setScale(2);
  }
}