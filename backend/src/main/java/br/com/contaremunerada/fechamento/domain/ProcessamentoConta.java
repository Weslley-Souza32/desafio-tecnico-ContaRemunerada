package br.com.contaremunerada.fechamento.domain;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import java.time.LocalDate;
import java.time.OffsetDateTime;

@Entity
@Table(name = "processamentos_conta")
public class ProcessamentoConta {

  @EmbeddedId
  private ProcessamentoContaId id;

  @Column(name = "processado_em", nullable = false, insertable = false, updatable = false)
  private OffsetDateTime processadoEm;

  protected ProcessamentoConta() {}

  public ProcessamentoConta(Long id, LocalDate dataReferencia) {

    this.id = new ProcessamentoContaId(id, dataReferencia);
  }

  public ProcessamentoContaId getId(){
    return id;
  }

  public OffsetDateTime getProcessadoEm() {
    return processadoEm;
  }
}
