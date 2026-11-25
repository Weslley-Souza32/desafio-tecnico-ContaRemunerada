package br.com.contaremunerada.taxa.domain;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Entity
@Table(name = "taxa_diaria")
public class TaxaDiaria {

  @Id
  private short id;

  @Column(name = "taxa_diaria", nullable = false, precision = 18, scale = 10)
  private BigDecimal taxaDiaria;

  @Column(name = "atualizado_em", nullable = false)
  private OffsetDateTime atualizadoEm;

  protected TaxaDiaria() {}

  public TaxaDiaria(BigDecimal taxaDiaria) {
    this.id = (short) 1;
    this.taxaDiaria = taxaDiaria;
  }

  public void atualizarTaxa(BigDecimal novaTaxa) {
    this.taxaDiaria = novaTaxa;
  }

  @PrePersist
  @PreUpdate
  public void atualizarDataHora() {
    this.atualizadoEm = OffsetDateTime.now();
  }

  public short getId() {
    return id;
  }

  public BigDecimal getTaxaDiaria() {
    return taxaDiaria;
  }

  public OffsetDateTime getAtualizadoEm() {
    return atualizadoEm;
  }
}
