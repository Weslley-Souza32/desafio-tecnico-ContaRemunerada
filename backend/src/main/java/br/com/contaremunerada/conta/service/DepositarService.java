package br.com.contaremunerada.conta.service;

import br.com.contaremunerada.conta.domain.Conta;
import br.com.contaremunerada.conta.repository.ContaRepository;
import br.com.contaremunerada.lancamento.domain.Lancamento;
import br.com.contaremunerada.lancamento.domain.TipoLancamento;
import br.com.contaremunerada.lancamento.repository.LancamentoRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;

@Service
public class DepositarService {

  private final ContaRepository contaRepository;
  private final LancamentoRepository lancamentoRepository;
  private final ValidadorValorMonetario validadorValorMonetario;

  public DepositarService(ContaRepository contaRepository, LancamentoRepository lancamentoRepository, ValidadorValorMonetario validadorValorMonetario) {
    this.contaRepository = contaRepository;
    this.lancamentoRepository = lancamentoRepository;
    this.validadorValorMonetario = validadorValorMonetario;
  }

  @Transactional
  public void depositar(Long contaId, String valorInformado) {

    BigDecimal valor = validadorValorMonetario.validar(valorInformado);

    Conta conta = contaRepository.buscarPorIdComBloqueio(contaId)
        .orElseThrow(() -> new ResponseStatusException(
            HttpStatus.NOT_FOUND,
            "Conta não encontrada."
        ));

    conta.creditar(valor);

    Lancamento lancamento = new Lancamento(
        conta,
        TipoLancamento.DEPOSITO,
        valor
    );

    lancamentoRepository.save(lancamento);
  }
}
