package br.com.contaremunerada.fechamento.domain;

import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.OffsetDateTime;

@Entity
@Table(name = "fechamentos")
public class Fechamento {

  @Id
  @Column(name = "data_referencia")
  private LocalDate dataReferencia;

  @Enumerated(EnumType.STRING)
  @Column(name = "status", nullable = false, length = 20)
  private StatusFechamento status;

  @Column(name = "contas_processadas", nullable = false)
  private int contasProcessadas;

  @Column(name = "iniciado_em", nullable = false)
  private OffsetDateTime iniciadoEm;

  @Column(name = "finalizado_em")
  private OffsetDateTime finalizadoEm;

  protected Fechamento() {}

  public Fechamento(LocalDate dataReferencia){
    this.dataReferencia = dataReferencia;
    this.status = StatusFechamento.EM_ANDAMENTO;
    this.contasProcessadas = 0;
    this.iniciadoEm = OffsetDateTime.now();
  }

  public void registrarContaProcessada(){

    this.contasProcessadas++;
  }

  public void atualizarProgresso(int totalProcessadas) {
    if (this.status != StatusFechamento.EM_ANDAMENTO) {
      throw new IllegalStateException("Não é possível atualizar um fecehamento que não está em andamento.");
    }

    if (totalProcessadas < this.contasProcessadas) {
      throw new IllegalArgumentException("A quantidade de contas processadas não pode diminuir.");
    }

    this.contasProcessadas = totalProcessadas;
  }

  public void concluir() {
    this.status = StatusFechamento.CONCLUIDO;
    this.finalizadoEm = OffsetDateTime.now();
  }

  public void registrarErro() {
    this.status = StatusFechamento.ERRO;
    this.finalizadoEm = OffsetDateTime.now();
  }

  public void reiniciar() {
    this.status = StatusFechamento.EM_ANDAMENTO;
    this.contasProcessadas = 0;
    this.iniciadoEm = OffsetDateTime.now();
    this.finalizadoEm = null;
  }

  public  LocalDate getDataReferencia() {
    return dataReferencia;
  }

  public StatusFechamento getStatus() {
    return status;
  }

  public int getContasProcessadas() {
    return contasProcessadas;
  }

  public OffsetDateTime getIniciadoEm() {
    return iniciadoEm;
  }

  public OffsetDateTime getFinalizadoEm() {
    return finalizadoEm;
  }
}
