package br.com.contaremunerada.taxa.repository;

import br.com.contaremunerada.taxa.domain.TaxaDiaria;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TaxaDiariaRepository extends JpaRepository<TaxaDiaria, Short> {
}
