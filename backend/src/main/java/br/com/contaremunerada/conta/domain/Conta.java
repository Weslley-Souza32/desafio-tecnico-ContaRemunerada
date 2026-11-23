package br.com.contaremunerada.conta.domain;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Entity
@Table(name="contas")
public class Conta {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "numero", nullable = false, length = 30)
  private String numero;

  @Column(name = "titular", nullable = false, length = 150)
  private String titular;

  @Column(name = "senha_hash", nullable = false, length = 255)
  private String senhaHash;

  @Column(name = "saldo", nullable = false, precision = 19, scale = 2)
  private BigDecimal saldo;

  @Column(name = "criado_em", nullable = false, insertable = false, updatable = false)
  private OffsetDateTime criadoEm;

  protected Conta() {}

  public Conta(String numero, String titular, String senhaHash) {
    this.numero = numero;
    this.titular = titular;
    this.senhaHash = senhaHash;
    this.saldo = BigDecimal.ZERO;
  }

  public Long getId() {
    return id;
  }

  public String getNumero() {
    return numero;
  }

  public String getTitular() {
    return titular;
  }

  public String getSenhaHash() {
    return senhaHash;
  }

  public BigDecimal getSaldo() {
    return saldo;
  }

  public OffsetDateTime getCriadoEm() {
    return criadoEm;
  }
}
