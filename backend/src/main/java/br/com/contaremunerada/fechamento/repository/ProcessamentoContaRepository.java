package br.com.contaremunerada.fechamento.repository;

import br.com.contaremunerada.fechamento.domain.ProcessamentoConta;
import br.com.contaremunerada.fechamento.domain.ProcessamentoContaId;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProcessamentoContaRepository extends JpaRepository<ProcessamentoConta, ProcessamentoContaId> {
}
