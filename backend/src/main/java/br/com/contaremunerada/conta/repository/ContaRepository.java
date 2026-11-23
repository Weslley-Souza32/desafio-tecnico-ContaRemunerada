package br.com.contaremunerada.conta.repository;

import br.com.contaremunerada.conta.domain.Conta;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ContaRepository extends JpaRepository<Conta, Long> {
  Optional<Conta> findByNumero(String numero);
}
