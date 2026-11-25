package br.com.contaremunerada.lancamento.domain;

import br.com.contaremunerada.conta.domain.Conta;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Entity
@Table(name = "lancamentos")
public class Lancamento {

  @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "conta_id", nullable = false)
  private Conta conta;

  @Enumerated(EnumType.STRING)
  @Column(name = "tipo", nullable = false, length = 20)
  private TipoLancamento tipo;

  @Column(name = "valor", nullable = false, precision = 19, scale = 2)
  private BigDecimal valor;

  @Column(name = "criado_em", nullable = false, insertable = false, updatable = false)
  private OffsetDateTime criadoEm;

  protected Lancamento() {}

  public Lancamento(Conta conta, TipoLancamento tipo, BigDecimal valor){
    this.conta = conta;
    this.tipo = tipo;
    this.valor = valor;
  }

  public Long getId() {
    return id;
  }

  public Conta getConta() {
    return conta;
  }

  public TipoLancamento getTipo() {
    return tipo;
  }

  public BigDecimal getValor() {
    return valor;
  }

  public OffsetDateTime getCriadoEm() {
    return criadoEm;
  }
}
