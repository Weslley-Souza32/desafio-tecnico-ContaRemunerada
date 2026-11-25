package br.com.contaremunerada.conta.repository;

import br.com.contaremunerada.conta.domain.Conta;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ContaRepository extends JpaRepository<Conta, Long> {
  Optional<Conta> findByNumero(String numero);

  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("SELECT c FROM Conta c WHERE c.id = :id")
  Optional<Conta> buscarPorIdComBloqueio(@Param("id") Long id);
}
