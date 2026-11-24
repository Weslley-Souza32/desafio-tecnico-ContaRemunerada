package br.com.contaremunerada.conta.service;

import br.com.contaremunerada.conta.domain.Conta;
import br.com.contaremunerada.conta.dto.SaldoResponse;
import br.com.contaremunerada.conta.repository.ContaRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.RoundingMode;

@Service
public class ConsultarSaldoService {

  private final ContaRepository contaRepository;

  public ConsultarSaldoService(ContaRepository contaRepository) {
    this.contaRepository = contaRepository;
  }

  @Transactional(readOnly = true)
  public SaldoResponse consultarSaldo(Long contaId){

    Conta conta = contaRepository.findById(contaId)
        .orElseThrow(() -> new ResponseStatusException(
            HttpStatus.NOT_FOUND,
            "Conta não encontrada."
        ));

    String saldoFormatado = conta.getSaldo()
        .setScale(2, RoundingMode.UNNECESSARY)
        .toPlainString();

    return new SaldoResponse(
        conta.getId(),
        conta.getNumero(),
        conta.getTitular(),
        saldoFormatado
    );
  }
}
