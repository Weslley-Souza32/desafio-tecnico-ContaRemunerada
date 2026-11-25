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
public class SacarService {

  private final ContaRepository contaRepository;
  private final LancamentoRepository lancamentoRepository;
  private final ValidadorValorMonetario validadorValorMonetario;

  public SacarService(ContaRepository contaRepository, LancamentoRepository lancamentoRepository, ValidadorValorMonetario validadorValorMonetario) {
    this.contaRepository = contaRepository;
    this.lancamentoRepository = lancamentoRepository;
    this.validadorValorMonetario = validadorValorMonetario;
  }

  @Transactional
  public void sacar(Long contaId, String valorInformado){

    BigDecimal valor = validadorValorMonetario.validar(valorInformado);

    Conta conta = contaRepository.buscarPorIdComBloqueio(contaId)
        .orElseThrow(() -> new ResponseStatusException(
            HttpStatus.NOT_FOUND,
            "Conta não encontrada."
        ));

    conta.debitar(valor);

    Lancamento lancamento = new Lancamento(
        conta,
        TipoLancamento.SAQUE,
        valor.negate()
    );

    lancamentoRepository.save(lancamento);
  }
}
