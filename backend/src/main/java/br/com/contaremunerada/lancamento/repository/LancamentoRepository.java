package br.com.contaremunerada.lancamento.repository;

import br.com.contaremunerada.lancamento.domain.Lancamento;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LancamentoRepository extends JpaRepository<Lancamento, Long> {

  Page<Lancamento> findByContaId(Long contaId, Pageable pageable);
}
