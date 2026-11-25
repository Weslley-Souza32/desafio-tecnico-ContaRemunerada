package br.com.contaremunerada.lancamento.repository;

import br.com.contaremunerada.lancamento.domain.Lancamento;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LancamentoRepository extends JpaRepository<Lancamento, Long> {

}
