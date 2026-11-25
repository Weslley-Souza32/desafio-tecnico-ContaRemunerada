package br.com.contaremunerada.fechamento.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.Objects;

@Embeddable
public class ProcessamentoContaId implements Serializable {

  @Column(name = "conta_id", nullable = false)
  private Long contaId;

  @Column(name = "data_referencia", nullable = false)
  private LocalDate dataReferencia;

  protected ProcessamentoContaId() {}

  public ProcessamentoContaId(Long contaId, LocalDate dataReferencia) {
    this.contaId = Objects.requireNonNull(contaId);
    this.dataReferencia = Objects.requireNonNull(dataReferencia);
  }

  public Long getContaId() {
    return contaId;
  }

  public LocalDate getDataReferencia() {
    return dataReferencia;
  }

  @Override
  public boolean equals(Object object) {

    if (this == object) {
      return true;
    }

    if(!(object instanceof ProcessamentoContaId outro)) {
      return false;
    }

    return Objects.equals(contaId, outro.contaId)
            && Objects.equals(
                dataReferencia,
                outro.dataReferencia);

  }

  @Override
  public int hashCode() {
    return Objects.hash(contaId, dataReferencia);
  }
}
